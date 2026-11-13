"""Comprueba el login y las consultas reales antes de ejecutar tests o dar un deploy por bueno."""
import json
import os
import time
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError


def comprobar(base):
    datos = json.dumps({'correo': 'admin@duoc.cl', 'clave': '1234'}).encode()
    with urlopen(Request(base + '/api/auth/login', data=datos,
                         headers={'Content-Type': 'application/json'}), timeout=5) as respuesta:
        token = json.load(respuesta)['token']
    for ruta in ['/api/productos', '/api/usuarios', '/api/clientes', '/api/pedidos', '/api/pagos']:
        with urlopen(Request(base + ruta, headers={'Authorization': 'Bearer ' + token}), timeout=5) as respuesta:
            if respuesta.status != 200 or not isinstance(json.load(respuesta), list):
                raise ValueError('Respuesta inesperada en ' + ruta)


if __name__ == '__main__':
    base = os.environ.get('BACKEND_URL', 'http://127.0.0.1:18080').rstrip('/')
    for intento in range(60):
        try:
            comprobar(base)
            print('Backend listo: login y consultas de los cuatro microservicios responden correctamente.')
            break
        except (HTTPError, URLError, TimeoutError, KeyError, ValueError):
            if intento == 59:
                raise SystemExit('El backend no quedó disponible. Revisa los logs de los contenedores.')
            time.sleep(3)
