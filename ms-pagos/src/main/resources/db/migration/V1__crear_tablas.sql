CREATE TABLE pago (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id  BIGINT NOT NULL,
    fecha       DATE   NOT NULL,
    monto       INT    NOT NULL,
    CONSTRAINT pk_pago PRIMARY KEY (id),
    CONSTRAINT ck_pago_monto CHECK (monto > 0)
);
