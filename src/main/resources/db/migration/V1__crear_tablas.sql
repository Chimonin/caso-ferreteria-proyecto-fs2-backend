CREATE TABLE usuario (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    rut             VARCHAR(9)   NOT NULL,
    nombre          VARCHAR(50)  NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    correo          VARCHAR(100) NOT NULL,
    clave           VARCHAR(100) NOT NULL,
    rol             VARCHAR(20)  NOT NULL,
    region          VARCHAR(60)  NOT NULL,
    comuna          VARCHAR(60)  NOT NULL,
    direccion       VARCHAR(300),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_rut UNIQUE (rut),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('admin', 'vendedor', 'cliente'))
);

CREATE TABLE cliente (
    usuario_id                   BIGINT      NOT NULL,
    tipo_cliente                 VARCHAR(20) NOT NULL,
    cuenta_corriente_habilitada  BOOLEAN     NOT NULL DEFAULT FALSE,
    saldo_adeudado               INT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_cliente PRIMARY KEY (usuario_id),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_cliente_tipo CHECK (tipo_cliente IN ('particular', 'contratista')),
    CONSTRAINT ck_cliente_saldo CHECK (saldo_adeudado >= 0)
);

CREATE TABLE producto (
    codigo        VARCHAR(10)  NOT NULL,
    categoria     VARCHAR(50)  NOT NULL,
    subcategoria  VARCHAR(50)  NOT NULL,
    nombre        VARCHAR(150) NOT NULL,
    marca         VARCHAR(50)  NOT NULL,
    unidad        VARCHAR(30)  NOT NULL,
    precio        INT          NOT NULL,
    stock         INT          NOT NULL DEFAULT 0,
    stock_minimo  INT          NOT NULL DEFAULT 0,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_producto PRIMARY KEY (codigo),
    CONSTRAINT ck_producto_precio CHECK (precio > 0),
    CONSTRAINT ck_producto_stock CHECK (stock >= 0 AND stock_minimo >= 0)
);

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
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES usuario (id),
    CONSTRAINT ck_pedido_estado CHECK (estado IN ('Pendiente', 'En preparación', 'Despachado', 'Entregado')),
    CONSTRAINT ck_pedido_medio_pago CHECK (medio_pago IN ('contado', 'cuenta_corriente')),
    CONSTRAINT ck_pedido_modalidad CHECK (modalidad_entrega IN ('retiro', 'despacho'))
);

CREATE TABLE detalle_pedido (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    pedido_id        BIGINT       NOT NULL,
    producto_codigo  VARCHAR(10)  NOT NULL,
    cantidad         INT          NOT NULL,
    precio_unitario  INT          NOT NULL,
    CONSTRAINT pk_detalle_pedido PRIMARY KEY (id),
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_codigo) REFERENCES producto (codigo),
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0)
);

CREATE TABLE pago (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id  BIGINT NOT NULL,
    fecha       DATE   NOT NULL,
    monto       INT    NOT NULL,
    CONSTRAINT pk_pago PRIMARY KEY (id),
    CONSTRAINT fk_pago_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (usuario_id),
    CONSTRAINT ck_pago_monto CHECK (monto > 0)
);
