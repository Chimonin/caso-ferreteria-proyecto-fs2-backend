CREATE TABLE pedido (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    cliente_id         BIGINT       NOT NULL,
    fecha              DATE         NOT NULL,
    estado             VARCHAR(20)  NOT NULL DEFAULT 'Pendiente',
    medio_pago         VARCHAR(20)  NOT NULL,
    modalidad_entrega  VARCHAR(10)  NOT NULL DEFAULT 'retiro',
    comuna_entrega     VARCHAR(60),
    direccion_entrega  VARCHAR(300),
    costo_envio        INT          NOT NULL DEFAULT 0,
    total              INT          NOT NULL,
    CONSTRAINT pk_pedido PRIMARY KEY (id),
    CONSTRAINT ck_pedido_estado CHECK (estado IN ('Pendiente', 'En preparación', 'Despachado', 'Entregado')),
    CONSTRAINT ck_pedido_medio_pago CHECK (medio_pago IN ('contado', 'cuenta_corriente')),
    CONSTRAINT ck_pedido_modalidad CHECK (modalidad_entrega IN ('retiro', 'despacho'))
);

CREATE TABLE detalle_pedido (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    pedido_id        BIGINT       NOT NULL,
    producto_codigo  VARCHAR(10)  NOT NULL,
    nombre           VARCHAR(150) NOT NULL,
    cantidad         INT          NOT NULL,
    precio_unitario  INT          NOT NULL,
    CONSTRAINT pk_detalle_pedido PRIMARY KEY (id),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0)
);
