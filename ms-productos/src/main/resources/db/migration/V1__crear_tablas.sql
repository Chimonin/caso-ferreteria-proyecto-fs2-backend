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
