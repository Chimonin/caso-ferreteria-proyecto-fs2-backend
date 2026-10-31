INSERT INTO pedido (id, cliente_id, fecha, estado, medio_pago, total) VALUES
(1, 3, '2026-09-15', 'Pendiente', 'contado', 34760),
(2, 4, '2026-09-17', 'En preparación', 'cuenta_corriente', 102000),
(3, 3, '2026-09-20', 'Entregado', 'contado', 19980);

INSERT INTO detalle_pedido (pedido_id, producto_codigo, nombre, cantidad, precio_unitario) VALUES
(1, 'MC001', 'Cemento Polpaico gris 25 kg', 4, 5990),
(1, 'MC005', 'Arena fina construcción 25 kg', 6, 1800),
(2, 'MC007', 'Ladrillo fiscal N°5', 200, 380),
(2, 'MC003', 'Mortero cola cerámica 25 kg', 5, 5200),
(3, 'PT001', 'Pintura látex interior 1 galón blanco', 2, 9990);
