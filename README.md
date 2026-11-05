# Backend Ferretería Los Maestros (microservicios)

Backend en Spring Boot 4.1 + MySQL, dividido en 4 microservicios y un API Gateway.
Cada microservicio es una aplicación independiente, con su propia base de datos.

## Estructura

| Carpeta        | Puerto | Base de datos          | Qué hace                                   | Responsable |
|----------------|--------|------------------------|--------------------------------------------|-------------|
| `api-gateway`  | 8080   | —                      | Único punto de entrada para React. Reenvía según la ruta | Aisleen |
| `ms-usuarios`  | 8081   | `ferreteria_usuarios`  | Usuarios, login, registro, clientes y cuenta corriente | Compañero |
| `ms-productos` | 8082   | `ferreteria_productos` | Catálogo, inventario y stock               | Compañero |
| `ms-pedidos`   | 8083   | `ferreteria_pedidos`   | Compras y estados de pedido                | Aisleen |
| `ms-pagos`     | 8084   | `ferreteria_pagos`     | Abonos a la cuenta corriente               | Aisleen |

```
React (5173) ──> api-gateway (8080) ──> /api/usuarios/**, /api/clientes/** ──> ms-usuarios
                                    ├─> /api/productos/**                   ──> ms-productos
                                    ├─> /api/pedidos/**                     ──> ms-pedidos ──┐ HTTP
                                    └─> /api/pagos/**                       ──> ms-pagos  ───┤
                                                                ms-usuarios / ms-productos <─┘
```

React sigue llamando a `http://localhost:8080`, igual que con el backend de una sola aplicación.

## Cómo levantarlo

### Opción A: con Docker (todo de una vez)

1. Instala Docker Desktop.
2. Copia `.env.example` como `.env`, escribe una clave en `DB_PASSWORD` y genera claves distintas para `JWT_SECRET` e `INTERNAL_API_KEY` con `openssl rand -base64 32`.
3. En la raíz del repo:
   ```powershell
   docker compose up --build
   ```
4. La primera vez se demora porque descarga y compila todo.

El MySQL de Docker queda en el puerto **3307** para no chocar con tu MySQL local (3306).

### Opción B: sin Docker (cada servicio en su terminal, con tu MySQL local)

En cada terminal de VS Code, desde la raíz del repo:
```powershell
$env:DB_PASSWORD="tuClave"
.\mvnw -pl ms-pedidos spring-boot:run
```
Cambia `ms-pedidos` por `ms-usuarios`, `ms-productos`, `ms-pagos` o `api-gateway`.
Las bases de datos se crean solas con Flyway.

Los cuatro servicios necesitan el mismo `JWT_SECRET` y el mismo `INTERNAL_API_KEY`.
En macOS/Linux puedes arrancarlo así (genera una clave nueva para esa ejecución):

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
export INTERNAL_API_KEY="$(openssl rand -base64 32)"
sh mvnw -pl ms-usuarios spring-boot:run
```

En PowerShell, define `$env:JWT_SECRET="tuClaveBase64"` y `$env:INTERNAL_API_KEY="otraClaveDe32CaracteresOMas"` antes del comando Maven.
Para conservar los tokens entre reinicios, reutiliza la misma clave privada de entorno.
Al abrir otras terminales configura las mismas claves; no generes valores distintos para cada servicio.
El archivo `.env` lo usa Docker Compose; Maven no lo carga automáticamente.

### Swagger de cada servicio
`http://localhost:8083/swagger-ui.html` (cambia el puerto según el servicio).

## Login con JWT en usuarios

Basado en el ejemplo del profesor: https://github.com/donkiwicl/fs2_jwt_backend.
El login y los permisos se implementaron en `ms-usuarios`. React todavía usa la sesión simulada.

- `POST /api/auth/login`: público, recibe `{ "correo": "admin@duoc.cl", "clave": "1234" }`.
- Devuelve `token`, `tipoToken`, `expiresIn` (segundos) y `usuario` sin contraseña.
- `POST /api/auth/registro`: público, crea solo clientes sin cuenta corriente habilitada y entrega JWT.
- `GET /api/usuarios` y `GET /api/usuarios/{id}`: solo administrador.
- `GET /api/clientes`: solo administrador.
- `GET /api/clientes/{id}`: administrador o el propio cliente.
- Sin token válido: 401. Con token válido pero sin permiso: 403.

Las cuentas iniciales mantienen la clave `1234`: `admin@duoc.cl`, `vendedor@duoc.cl`,
`juan.perez@gmail.com`, `ana.torres@gmail.com` y `luis.fuentes@duoc.cl`.
La migración Java `V3__encriptar_claves` convierte las claves existentes a hashes BCrypt una sola vez.
El secreto JWT se configura con `JWT_SECRET` (Base64 de al menos 32 bytes) y el vencimiento
con `JWT_EXPIRATION_MS` (por defecto, una hora). El secreto nunca se envía a React.

### Probar en Swagger

1. Arranca MySQL y `ms-usuarios`.
2. Abre http://localhost:8081/swagger-ui/index.html.
3. Ejecuta el login con una cuenta inicial.
4. Copia el campo `token`, presiona **Authorize** y pega solo el token, sin escribir `Bearer`.
5. Prueba las consultas. Cambia a una cuenta cliente para comprobar los permisos.

El gateway reenvía `/api/auth/**` a usuarios. Si lo arrancas, puedes hacer login por
http://localhost:8080/api/auth/login y enviar `Authorization: Bearer <token>` en las consultas.
La firma, el vencimiento y el usuario activo se validan en cada petición; el rol se vuelve a leer de la base.
Cerrar sesión en React consistirá en borrar el token; una copia seguirá siendo válida hasta vencer.
No hay refresh token ni revocación de tokens implementados en este avance.
Productos también valida firma y vencimiento del JWT; allí los roles firmados duran hasta el vencimiento.
Pedidos y pagos también validan el JWT y la propiedad de las operaciones. Sus llamadas internas envían la clave de servicio.

### Estructura agregada

```text
ms-usuarios/src/main/java/Fullstackeando/Aura/usuarios/
  config/     SecurityConfig (permisos), OpenApiConfig (Authorize)
  security/   JwtService, JwtAuthFilter, UsuarioAutenticado
  service/    AuthService, UsuarioDetailsService
  controller/ AuthController
  dto/        LoginRequest, AuthResponse
```

Las pruebas de JWT usan H2 en memoria, sin modificar el MySQL local:

```bash
sh mvnw -pl ms-usuarios test
```

## Gestión de usuarios, clientes y productos

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/usuarios` | Administrador |
| PUT | `/api/usuarios/{id}` | Administrador |
| DELETE | `/api/usuarios/{id}` | Administrador |
| POST | `/api/clientes` | Administrador |
| PUT | `/api/clientes/{id}` | Administrador |
| DELETE | `/api/clientes/{id}` | Administrador |
| GET | `/api/productos` y `/api/productos/{codigo}` | Público |
| POST | `/api/productos` | Administrador |
| PUT | `/api/productos/{codigo}` | Administrador, datos completos |
| PATCH | `/api/productos/{codigo}` | Administrador, precio y/o stock |
| PATCH | `/api/productos/{codigo}/stock` | Administrador o vendedor, solo stock |
| DELETE | `/api/productos/{codigo}` | Administrador |
| GET | `/api/pedidos` | Administrador o vendedor |
| GET | `/api/pedidos/cliente/{clienteId}` | Administrador o el cliente dueño |
| POST | `/api/pedidos` | Cliente, solo su propio id |
| PATCH | `/api/pedidos/{id}/estado` | Administrador o vendedor |
| GET | `/api/pagos` | Administrador |
| GET | `/api/pagos/cliente/{clienteId}` | Administrador o el cliente dueño |
| POST | `/api/pagos` | Cliente, solo su propio id |

Los usuarios se crean con `rut`, `nombre`, `apellidos`, `correo`, `clave`, `rol`, `region`,
`comuna`, `direccion`, y para clientes también `tipoCliente` y `cuentaCorrienteHabilitada`.
La edición recibe los mismos datos, pero conserva la clave actual. El registro público recibe
esos datos sin `rol` ni `cuentaCorrienteHabilitada`: ambos se fijan en el servidor.
Al crear un usuario cliente, se crea su registro de cliente en la misma transacción.

`POST /api/clientes` recibe `{ "id": 3, "tipoCliente": "particular", "cuentaCorrienteHabilitada": false }`
y vincula un usuario existente con rol cliente que todavía no tenga ese registro.
`PUT /api/clientes/{id}` recibe solo `tipoCliente` y `cuentaCorrienteHabilitada`.
Solo contratistas pueden tener cuenta corriente. No se quita una cuenta con deuda ni se da de baja
un cliente endeudado. Tampoco se elimina ni se cambia el rol del último administrador activo.
Las bajas de usuario/cliente desactivan el usuario y conservan su registro para el historial.

Los productos se crean o reemplazan con `codigo`, `nombre`, `categoria`, `subcategoria`, `marca`,
`unidad`, `precio`, `stock` y `stockMinimo`. Precio debe ser entero positivo y stock/mínimo enteros no negativos.
La baja deja `activo = false`; ya no aparece en el catálogo pero permanece en MySQL.
El PATCH de stock rechaza campos adicionales como `precio`, incluso si lo llama un vendedor.

### Llamadas internas

Usuarios y productos exigen `X-Internal-Key` en sus rutas `/internal/**`.
Un JWT de administrador no sustituye esa clave. Pedidos y pagos la obtienen de `INTERNAL_API_KEY`
y la agregan a sus clientes HTTP. El gateway no publica estas rutas.
Swagger muestra un esquema `internalKey` para probarlas localmente; no compartas esa clave con React.
Las deudas se actualizan con bloqueo de filas y validación de saldo.
El descuento de stock agrupa códigos repetidos, bloquea en orden y revierte toda la operación si un item falla.
La reposición permite compensar un producto dado de baja después del descuento.

Pruebas de ambos servicios, con H2 aislado del MySQL local:

```bash
sh mvnw -pl ms-usuarios,ms-productos,ms-pedidos,ms-pagos test
```

## Endpoints públicos (los llama React a través del gateway)

**ms-pedidos**
- `GET /api/pedidos`: todos los pedidos (vendedor y admin)
- `GET /api/pedidos/cliente/{clienteId}`: pedidos de un cliente
- `POST /api/pedidos`: crear pedido `{ clienteId, medioPago, entrega: { modalidad, comuna, direccion }, items: [ { codigo, cantidad } ] }`
- `PATCH /api/pedidos/{id}/estado`: `{ estado }`

**ms-pagos**
- `GET /api/pagos`: todos los pagos (AdminCredito)
- `GET /api/pagos/cliente/{clienteId}`: pagos de un cliente
- `POST /api/pagos`: `{ clienteId, monto }`

## Contrato entre microservicios (endpoints internos)

Pedidos y pagos necesitan datos de usuarios y productos. Como están en otras bases,
los piden por HTTP a estos endpoints. Empiezan con `/internal` y **el gateway no los expone**:
solo los usan los microservicios entre ellos.

**ms-productos implementa:**

| Endpoint | Body | Respuesta OK | Errores |
|---|---|---|---|
| `POST /internal/productos/descontar-stock` | `[ { "codigo": "MC001", "cantidad": 4 } ]` | `200` `[ { "codigo", "nombre", "precio", "cantidad" } ]` | `409 { "mensaje": "Uno de los productos ya no existe." }` o `409 { "mensaje": "No hay stock suficiente de <nombre>." }` |
| `POST /internal/productos/reponer-stock` | `[ { "codigo": "MC001", "cantidad": 4 } ]` | `200` o `204` | — |

`descontar-stock` debe ser `@Transactional`: revisa que **todos** existan, estén activos y
tengan stock, y descuenta todo junto. Si uno falla, no descuenta ninguno.

**ms-usuarios implementa:**

| Endpoint | Body | Respuesta OK | Errores |
|---|---|---|---|
| `GET /internal/clientes/{id}` | — | `200 { "id", "cuentaCorrienteHabilitada", "saldoAdeudado" }` | `404 { "mensaje": "Cliente no encontrado" }` |
| `PATCH /internal/clientes/{id}/sumar-deuda` | `{ "monto": 38750 }` | `200` o `204` | `404`, `409 { "mensaje": "La cuenta corriente no está habilitada." }` |
| `PATCH /internal/clientes/{id}/restar-deuda` | `{ "monto": 10000 }` | `200` o `204` | `404`, `409 { "mensaje": "El cliente no tiene cuenta corriente habilitada" }` o `409 { "mensaje": "El monto no puede ser mayor a la deuda" }` |

Los errores deben responder JSON con la llave `"mensaje"` (igual que el `GlobalExceptionHandler`),
porque pedidos y pagos reenvían ese texto al frontend.

## Si algo falla a mitad de camino (compensación)

Cada servicio solo puede deshacer (`@Transactional`) los cambios de **su propia** base.
Por eso, si un pedido falla después de descontar el stock, `ms-pedidos` llama a
`reponer-stock` (y a `restar-deuda` si era a crédito) para dejar todo como estaba.
`ms-pagos` hace lo mismo con `sumar-deuda` si el pago no se pudo guardar.
