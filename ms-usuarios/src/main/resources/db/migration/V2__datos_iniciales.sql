-- V2 ms-usuarios: datos iniciales, copiados de usuarios.json y clientes.json

INSERT INTO usuario (id, rut, nombre, apellidos, correo, clave, rol, region, comuna, direccion) VALUES
(1, '111111111', 'Carla', 'Rojas Muñoz', 'admin@duoc.cl', '1234', 'admin', 'Coquimbo', 'La Serena', 'Av. Francisco de Aguirre 100'),
(2, '222222222', 'Pedro', 'Soto Vega', 'vendedor@duoc.cl', '1234', 'vendedor', 'Coquimbo', 'La Serena', 'Calle Balmaceda 250'),
(3, '123456785', 'Juan', 'Pérez Díaz', 'juan.perez@gmail.com', '1234', 'cliente', 'Coquimbo', 'Coquimbo', 'Pasaje Los Aromos 45'),
(4, '98765432K', 'Ana', 'Torres Fuentes', 'ana.torres@gmail.com', '1234', 'cliente', 'Coquimbo', 'La Serena', 'Av. del Mar 3200'),
(5, '156789012', 'Luis', 'Fuentes Araya', 'luis.fuentes@duoc.cl', '1234', 'cliente', 'Coquimbo', 'Ovalle', 'Calle Vicuña Mackenna 780');

INSERT INTO cliente (usuario_id, tipo_cliente, cuenta_corriente_habilitada, saldo_adeudado) VALUES
(3, 'particular', FALSE, 0),
(4, 'contratista', TRUE, 32000),
(5, 'contratista', FALSE, 0);
