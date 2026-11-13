#!/usr/bin/env bash
set -euo pipefail

# Systems Manager ejecuta este archivo en EC2, con el rol de la instancia.
directorio=$1
proyecto=$2
region=$3
registro=$4
version=$5
release="$directorio/releases/$version"

test -f "$directorio/.env" || { echo 'Falta el .env privado del servidor'; exit 1; }
test -f "$release/docker-compose.prod.yml"
command -v docker >/dev/null
command -v aws >/dev/null
command -v python3 >/dev/null
docker compose version >/dev/null

aws ecr get-login-password --region "$region" | docker login --username AWS --password-stdin "$registro"
umask 077
printf 'ECR_REGISTRO=%s\nIMAGE_TAG=%s\n' "$registro" "$version" > "$release/release.env"

compose=(docker compose --project-directory "$directorio" -p "$proyecto"
  --env-file "$directorio/.env" --env-file "$release/release.env" -f "$release/docker-compose.prod.yml")
"${compose[@]}" config --quiet
"${compose[@]}" pull api-gateway ms-usuarios ms-productos ms-pedidos ms-pagos
# no se usa down ni se eliminan los volumenes de MySQL
"${compose[@]}" up -d --no-build

python3 - <<'PY'
import json
import time
from urllib.request import urlopen
from urllib.error import HTTPError, URLError
from http.client import HTTPException

for intento in range(60):
    try:
        with urlopen('http://127.0.0.1:8080/api/productos', timeout=5) as respuesta:
            if respuesta.status != 200 or not isinstance(json.load(respuesta), list):
                raise ValueError('Catálogo no disponible')
        #las rutas protegidas deben responder 401, no 404 ni 502/503
        for ruta in ['usuarios', 'clientes', 'pedidos', 'pagos']:
            try:
                with urlopen('http://127.0.0.1:8080/api/' + ruta, timeout=5):
                    raise ValueError('Ruta protegida sin autenticación')
            except HTTPError as error:
                if error.code != 401:
                    raise ValueError('Servicio no disponible') from None
        print('Deploy verificado: catálogo público y rutas protegidas disponibles.')
        break
    except (OSError, HTTPException, ValueError):
        if intento == 59:
            raise SystemExit('Falló la comprobación del despliegue. Revisa los contenedores en EC2.')
        time.sleep(3)
PY

printf '%s\n' "$version" > "$directorio/ultima-version-correcta"
echo "Versión desplegada: $version"
