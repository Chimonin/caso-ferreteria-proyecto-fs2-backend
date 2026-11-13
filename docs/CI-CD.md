# CI/CD del backend

El workflow es `.github/workflows/backend.yml`, con dos trabajos:

1. **Verificar:** levanta MySQL, los cuatro microservicios y el gateway con Docker, comprueba que respondan y ejecuta las pruebas HTTP.
2. **Desplegar:** solo después de pasar las pruebas, en `main` y con AWS habilitado. Publica las cinco imágenes en ECR y actualiza una EC2 con Docker Compose mediante Systems Manager.

## CI: no requiere una cuenta AWS

Se ejecuta al abrir o actualizar un pull request hacia `main`, al subir a `main` y desde **Actions → Backend CI-CD → Run workflow**.
Genera claves temporales para ese run y utiliza `docker-compose.ci.yml` con un volumen propio.
No utiliza `.env` del equipo ni los datos de producción. Solo expone el gateway en `127.0.0.1:18080`.

Las pruebas usan `BACKEND_URL=http://127.0.0.1:18080`. Sin esa variable se omitirían;
por eso `scripts/verificar-reportes.py` exige los cinco informes y rechaza cualquier prueba omitida, fallida o vacía.
La base contiene las cuentas iniciales, con clave `1234`, que necesitan las pruebas del equipo.
Al terminar, incluso si hay un fallo, se eliminan los contenedores y el volumen exclusivo del CI.

En el run puedes abrir el paso Maven y descargar el artefacto **backend-reportes** con los informes Surefire y logs.
La publicación de reportes de prueba no es un reporte de cobertura: este workflow no calcula porcentaje de cobertura Java.

Para reproducirlo localmente, con Docker Desktop, Java 21 y Python 3 disponibles:

```bash
export COMPOSE_PROJECT_NAME=ferreteria-ci
export DB_PASSWORD="$(openssl rand -hex 24)"
export JWT_SECRET="$(openssl rand -base64 32)"
export INTERNAL_API_KEY="$(openssl rand -hex 32)"
export BACKEND_URL=http://127.0.0.1:18080
docker compose --env-file /dev/null -f docker-compose.ci.yml up --build -d
python3 scripts/esperar-backend.py
sh mvnw -B clean verify
python3 scripts/verificar-reportes.py
docker compose --env-file /dev/null -f docker-compose.ci.yml down -v
```

El último comando borra únicamente los datos de ese proyecto de CI. No lo sustituyas por el Compose habitual.

## CD: configuración inicial en AWS

El despliegue queda omitido mientras la variable de repositorio `AWS_DEPLOY_ENABLED` no sea exactamente `true`.
No se han creado recursos AWS ni configurado permisos automáticamente.

Necesitamos:

- Cinco repositorios ECR en la misma región/cuenta: `ferreteria/api-gateway`, `ferreteria/ms-usuarios`, `ferreteria/ms-productos`, `ferreteria/ms-pedidos`, `ferreteria/ms-pagos`.
- Una EC2 Linux con espacio y memoria para cinco procesos Java más MySQL, Docker Compose v2 o superior, AWS CLI y Python 3.
- El agente SSM activo y la EC2 visible como nodo administrado en Systems Manager. Necesita conectividad a SSM y a ECR.
- Un rol de instancia EC2 con permisos de lectura de ECR y de Systems Manager (por ejemplo `AmazonEC2ContainerRegistryReadOnly` y `AmazonSSMManagedInstanceCore`).
- Un directorio del servidor, por ejemplo `/opt/ferreteria`, con el `.env` privado y el nombre del proyecto Compose que ya usa la instalación.

El `.env` de EC2 debe contener `DB_PASSWORD`, `JWT_SECRET`, `INTERNAL_API_KEY` y `FRONTEND_URL`.
La contraseña debe coincidir con la del volumen MySQL existente. GitHub Actions no recibe ni cambia esos secretos.
El servidor solo expone el gateway en 8080; el acceso público HTTPS debe estar configurado aparte.

### Acceso de GitHub a AWS

Se usa OIDC, sin claves permanentes de AWS en el repositorio.
Crea un proveedor IAM `https://token.actions.githubusercontent.com` con audiencia `sts.amazonaws.com` y un rol asumible por GitHub.
Limita su confianza al repositorio backend y al environment **production**, usando el `sub` real que emite GitHub.
El formato puede incluir los IDs inmutables del propietario y repositorio; no copies una política para otra cuenta o un comodín global.
Configura el environment de GitHub para permitir desplegar únicamente desde `main`.

El rol de GitHub necesita:

- `ecr:GetAuthorizationToken` y permisos para subir capas y `ecr:PutImage` sobre los cinco repositorios indicados.
- `ssm:SendCommand` para `AWS-RunShellScript` y la EC2 elegida, y `ssm:GetCommandInvocation` para confirmar el resultado.

No necesita permisos para crear instancias ni bases de datos.
En AWS Academy puede que no tengas permisos para crear el proveedor OIDC o roles: verifica eso antes de habilitar CD.
Si el laboratorio lo impide, el CI puede seguir funcionando y el deploy se realiza manualmente hasta disponer de esos permisos.

Referencias oficiales:

- [GitHub OIDC con AWS](https://docs.github.com/en/actions/how-tos/secure-your-work/security-harden-deployments/oidc-in-aws)
- [AWS Systems Manager Run Command](https://docs.aws.amazon.com/systems-manager/latest/userguide/run-command.html)

### Variables en GitHub

En **Settings → Secrets and variables → Actions → Variables** configura primero los datos y al final habilita el despliegue:

| Variable | Valor |
|---|---|
| `AWS_REGION` | Región donde están ECR y EC2 |
| `AWS_ROLE_ARN` | ARN del rol OIDC autorizado |
| `EC2_INSTANCE_ID` | ID de la instancia administrada por SSM |
| `AWS_DEPLOY_DIR` | Directorio absoluto con el `.env` del servidor, por ejemplo `/opt/ferreteria` |
| `AWS_COMPOSE_PROJECT` | Nombre exacto del proyecto Compose existente, para conservar el volumen de MySQL |
| `AWS_DOCKER_PLATFORM` | `linux/amd64` (por defecto) o `linux/arm64` si EC2 usa Graviton |
| `AWS_DEPLOY_ENABLED` | `true` solo cuando esté todo preparado |

Los primeros datos también pueden configurarse como variables del environment **production**.
`AWS_DEPLOY_ENABLED` debe ser variable del repositorio, porque se evalúa antes de entrar al environment.
El workflow no usa un archivo PEM ni abre SSH; Systems Manager ejecuta los comandos con el rol de la instancia.

## Qué sucede en un deploy

Las imágenes se publican con el SHA del commit probado. No se actualiza `latest`.
Antes de publicar y antes de enviar a EC2 se comprueba que ese commit siga siendo el actual de `main`.
El workflow no despliega pull requests ni ramas distintas de `main`.

`scripts/enviar-deploy.py` envía los archivos por SSM y espera el resultado del comando remoto.
En EC2 crea `releases/<SHA>/` con el Compose y `release.env`, que solo contiene registro ECR y versión.
`scripts/deploy-ec2.sh` inicia sesión en ECR con el rol EC2, descarga las cinco imágenes y ejecuta `up -d --no-build`.
Mantiene el mismo nombre de proyecto Compose y su volumen de MySQL; nunca ejecuta `down -v` en el servidor.

Después comprueba el catálogo público y que usuarios, clientes, pedidos y pagos respondan 401 sin token.
Solo entonces registra el SHA en `ultima-version-correcta` y el deploy queda verde.
Si falla, Actions lo marca en rojo; los detalles están en la invocación SSM. No hay rollback automático.
Compose puede producir una interrupción breve y estados parciales durante la actualización; no es un despliegue sin interrupciones.

Para volver a una versión anterior conservada:

```bash
cd /opt/ferreteria
# reemplaza <SHA_ANTERIOR> y <PROYECTO_EXISTENTE> por tus valores
docker compose --project-directory /opt/ferreteria -p <PROYECTO_EXISTENTE> \
  --env-file .env --env-file releases/<SHA_ANTERIOR>/release.env \
  -f releases/<SHA_ANTERIOR>/docker-compose.prod.yml up -d --no-build
```

Esto vuelve a las imágenes anteriores, pero no revierte migraciones ni datos de MySQL. Los cambios de esquema requieren su propia planificación.
