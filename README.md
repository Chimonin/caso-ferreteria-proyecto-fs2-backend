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
2. Copia `.env.example` como `.env` y escribe una clave en `DB_PASSWORD`.
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

### Swagger de cada servicio
`http://localhost:8083/swagger-ui.html` (cambia el puerto según el servicio).

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

**ms-productos debe tener:**

| Endpoint | Body | Respuesta OK | Errores |
|---|---|---|---|
| `POST /internal/productos/descontar-stock` | `[ { "codigo": "MC001", "cantidad": 4 } ]` | `200` `[ { "codigo", "nombre", "precio", "cantidad" } ]` | `409 { "mensaje": "Uno de los productos ya no existe." }` o `409 { "mensaje": "No hay stock suficiente de <nombre>." }` |
| `POST /internal/productos/reponer-stock` | `[ { "codigo": "MC001", "cantidad": 4 } ]` | `200` o `204` | — |

`descontar-stock` debe ser `@Transactional`: revisa que **todos** existan, estén activos y
tengan stock, y descuenta todo junto. Si uno falla, no descuenta ninguno.

**ms-usuarios debe tener:**

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
