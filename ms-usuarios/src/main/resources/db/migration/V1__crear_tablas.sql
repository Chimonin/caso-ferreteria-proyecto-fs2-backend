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
