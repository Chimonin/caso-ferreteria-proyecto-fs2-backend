"""Impide que CI quede verde cuando las pruebas HTTP se omitieron por falta de BACKEND_URL."""
from pathlib import Path
import xml.etree.ElementTree as ET

esperados = {
    'ms-usuarios': ['UsuarioTest', 'ClienteTest'],
    'ms-productos': ['ProductoTest'],
    'ms-pedidos': ['PedidoTest'],
    'ms-pagos': ['PagoTest'],
}
total = 0
for modulo, clases in esperados.items():
    for clase in clases:
        archivos = list(Path(modulo, 'target', 'surefire-reports').glob('TEST-*.' + clase + '.xml'))
        if len(archivos) != 1:
            raise SystemExit('Falta el reporte de ' + modulo + '/' + clase)
        reporte = ET.parse(archivos[0]).getroot()
        pruebas = int(reporte.get('tests', '0'))
        if pruebas == 0 or any(int(reporte.get(campo, '0')) for campo in ['skipped', 'errors', 'failures']):
            raise SystemExit('Hay pruebas omitidas o fallidas en ' + clase)
        total += pruebas
        print(clase + ': ' + str(pruebas) + ' pruebas ejecutadas sin errores ni omisiones')
print('Total: ' + str(total) + ' pruebas HTTP correctas')
