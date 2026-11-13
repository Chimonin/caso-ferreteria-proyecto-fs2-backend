"""Envía Compose y el script al servidor con SSM; el .env privado permanece en EC2."""
import base64
import json
import os
from pathlib import Path
import re
import shlex
import subprocess
import tempfile
import time


def aws(*argumentos):
    resultado = subprocess.run(['aws', *argumentos, '--output', 'json'], capture_output=True, text=True)
    if resultado.returncode:
        if 'InvocationDoesNotExist' in resultado.stderr:
            raise LookupError('La invocación todavía no está disponible')
        raise RuntimeError(resultado.stderr.strip())
    return json.loads(resultado.stdout)


def preparar_comando(entorno):
    directorio = entorno['AWS_DEPLOY_DIR']
    proyecto = entorno['AWS_COMPOSE_PROJECT']
    region = entorno['AWS_REGION']
    registro = entorno['ECR_REGISTRO']
    version = entorno['GITHUB_SHA']
    instancia = entorno['EC2_INSTANCE_ID']
    comprobaciones = [
        (directorio, r'/[A-Za-z0-9_/-]+'),
        (proyecto, r'[a-z0-9][a-z0-9_-]*'),
        (region, r'[a-z0-9-]+'),
        (registro, r'[0-9]{12}\.dkr\.ecr\.[a-z0-9-]+\.amazonaws\.com'),
        (version, r'[a-f0-9]{40}'),
        (instancia, r'i-[a-f0-9]+'),
    ]
    if any(not re.fullmatch(patron, valor) for valor, patron in comprobaciones) or '..' in directorio:
        raise ValueError('Configuración AWS no válida; revisa las variables de Actions')

    release = directorio + '/releases/' + version
    comandos = ['set -eu', 'umask 077', 'mkdir -p ' + shlex.quote(release)]
    for origen, destino in [('docker-compose.prod.yml', 'docker-compose.prod.yml'),
                            ('scripts/deploy-ec2.sh', 'deploy-ec2.sh')]:
        contenido = base64.b64encode(Path(origen).read_bytes()).decode()
        comandos.append("printf '%s' " + shlex.quote(contenido) + ' | base64 --decode > '
                        + shlex.quote(release + '/' + destino))
    comandos.append('bash ' + shlex.quote(release + '/deploy-ec2.sh') + ' '
                    + ' '.join(shlex.quote(v) for v in [directorio, proyecto, region, registro, version]))
    return '\n'.join(comandos)


if __name__ == '__main__':
    comando = preparar_comando(os.environ)
    region = os.environ['AWS_REGION']
    instancia = os.environ['EC2_INSTANCE_ID']
    with tempfile.TemporaryDirectory() as temporal:
        ruta = Path(temporal, 'parametros.json')
        ruta.write_text(json.dumps({'commands': [comando], 'executionTimeout': ['1200']}))
        resultado = aws('ssm', 'send-command', '--region', region, '--instance-ids', instancia,
                        '--document-name', 'AWS-RunShellScript', '--timeout-seconds', '120',
                        '--parameters', 'file://' + str(ruta), '--comment', 'Deploy backend ' + os.environ['GITHUB_SHA'])
    command_id = resultado['Command']['CommandId']
    print('Despliegue enviado a Systems Manager: ' + command_id, flush=True)
    for _ in range(150):
        try:
            resultado = aws('ssm', 'get-command-invocation', '--region', region,
                            '--command-id', command_id, '--instance-id', instancia)
        except LookupError:
            #SSM puede tardar unos segundos en hacer visible la invocación
            time.sleep(10)
            continue
        estado = resultado['Status']
        if estado == 'Success':
            print(resultado.get('StandardOutputContent', 'Deploy completado'))
            break
        if estado in ['Failed', 'Cancelled', 'TimedOut', 'Cancelling']:
            print(resultado.get('StandardOutputContent', ''))
            print(resultado.get('StandardErrorContent', ''))
            raise SystemExit('Falló el despliegue en EC2: ' + estado)
        time.sleep(10)
    else:
        raise SystemExit('No se pudo confirmar el deploy; consulta el comando en Systems Manager: ' + command_id)
