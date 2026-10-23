-- V2: datos iniciales, copiados de los mocks JSON del frontend (src/mocks)

-- usuarios.json
INSERT INTO usuario (id, rut, nombre, apellidos, correo, clave, rol, region, comuna, direccion) VALUES
(1, '111111111', 'Carla', 'Rojas Muñoz', 'admin@duoc.cl', '1234', 'admin', 'Coquimbo', 'La Serena', 'Av. Francisco de Aguirre 100'),
(2, '222222222', 'Pedro', 'Soto Vega', 'vendedor@duoc.cl', '1234', 'vendedor', 'Coquimbo', 'La Serena', 'Calle Balmaceda 250'),
(3, '123456785', 'Juan', 'Pérez Díaz', 'juan.perez@gmail.com', '1234', 'cliente', 'Coquimbo', 'Coquimbo', 'Pasaje Los Aromos 45'),
(4, '98765432K', 'Ana', 'Torres Fuentes', 'ana.torres@gmail.com', '1234', 'cliente', 'Coquimbo', 'La Serena', 'Av. del Mar 3200'),
(5, '156789012', 'Luis', 'Fuentes Araya', 'luis.fuentes@duoc.cl', '1234', 'cliente', 'Coquimbo', 'Ovalle', 'Calle Vicuña Mackenna 780');

-- clientes.json
INSERT INTO cliente (usuario_id, tipo_cliente, cuenta_corriente_habilitada, saldo_adeudado) VALUES
(3, 'particular', FALSE, 0),
(4, 'contratista', TRUE, 32000),
(5, 'contratista', FALSE, 0);

-- productos.json
INSERT INTO producto (codigo, categoria, subcategoria, nombre, marca, unidad, precio, stock, stock_minimo) VALUES
('MC001', 'Mat. Construcción', 'Cementos', 'Cemento Polpaico gris 25 kg', 'Polpaico', 'Saco', 5990, 80, 20),
('MC002', 'Mat. Construcción', 'Cementos', 'Cemento Melón blanco 25 kg', 'Melón', 'Saco', 7490, 40, 10),
('MC003', 'Mat. Construcción', 'Morteros', 'Mortero cola cerámica 25 kg', 'Volcán', 'Saco', 5200, 50, 15),
('MC004', 'Mat. Construcción', 'Morteros', 'Mortero nivelador piso 25 kg', 'Weber', 'Saco', 6490, 30, 10),
('MC005', 'Mat. Construcción', 'Áridos', 'Arena fina construcción 25 kg', 'Granel', 'Saco', 1800, 60, 20),
('MC006', 'Mat. Construcción', 'Áridos', 'Ripio 25 kg', 'Granel', 'Saco', 1500, 60, 20),
('MC007', 'Mat. Construcción', 'Ladrillos', 'Ladrillo fiscal N°5', 'Local', 'Unidad', 380, 500, 100),
('MC008', 'Mat. Construcción', 'Ladrillos', 'Ladrillo prensado 6x14x29 cm', 'Melón', 'Unidad', 550, 300, 80),
('MC009', 'Mat. Construcción', 'Bloques', 'Bloque de hormigón 19x19x39 cm', 'Volcán', 'Unidad', 1200, 200, 50),
('MC010', 'Mat. Construcción', 'Bloques', 'Bloque liviano 10x20x40 cm', 'Ytong', 'Unidad', 1690, 100, 30),
('PT001', 'Pinturas', 'Látex', 'Pintura látex interior 1 galón blanco', 'Sipa', 'Galón', 9990, 40, 10),
('PT002', 'Pinturas', 'Látex', 'Pintura látex interior 4 litros (varios col.)', 'Sipa', 'Envase 4L', 12990, 30, 8),
('PT003', 'Pinturas', 'Látex', 'Pintura látex exterior 1 galón blanco', 'Kömex', 'Galón', 13990, 25, 8),
('PT004', 'Pinturas', 'Esmalte', 'Esmalte sintético 1/4 litro (varios col.)', 'Sipa', '1/4 L', 4290, 50, 15),
('PT005', 'Pinturas', 'Esmalte', 'Esmalte sintético 1 litro (varios col.)', 'Sipa', '1 litro', 9490, 30, 10),
('PT006', 'Pinturas', 'Antihumedad', 'Pintura antihumedad 1 galón blanco', 'Kömex', 'Galón', 17990, 15, 5),
('PT007', 'Pinturas', 'Accesorios', 'Rodillo lana 23 cm con mango', 'Wurth', 'Unidad', 3990, 30, 10),
('PT008', 'Pinturas', 'Accesorios', 'Brocha cerda natural 3"', 'Sipa', 'Unidad', 1690, 40, 10),
('PT009', 'Pinturas', 'Accesorios', 'Cinta de enmascarar 24mm x 50m', '3M', 'Rollo', 2490, 60, 20),
('HM001', 'Herramientas', 'Manuales', 'Martillo carpintero 500g', 'Stanley', 'Unidad', 7990, 20, 5),
('HM002', 'Herramientas', 'Manuales', 'Alicate universal 8"', 'Stanley', 'Unidad', 7290, 15, 5),
('HM003', 'Herramientas', 'Manuales', 'Destornillador plano 6x100mm', 'Stanley', 'Unidad', 1990, 30, 10),
('HM004', 'Herramientas', 'Manuales', 'Destornillador Phillips PH2 6x100mm', 'Stanley', 'Unidad', 1990, 30, 10),
('HM005', 'Herramientas', 'Manuales', 'Llave ajustable 10"', 'Bahco', 'Unidad', 8490, 12, 4),
('HM006', 'Herramientas', 'Manuales', 'Juego llaves hexagonales métrico x9', 'Stanley', 'Set', 5490, 15, 5),
('HM007', 'Herramientas', 'Manuales', 'Serrucho 20" 7 dientes por pulgada', 'Stanley', 'Unidad', 9490, 10, 3),
('HM008', 'Herramientas', 'Manuales', 'Nivel de burbuja 60 cm', 'Stanley', 'Unidad', 10490, 8, 3),
('HM009', 'Herramientas', 'Manuales', 'Metro de tela 5m', 'Stanley', 'Unidad', 4290, 25, 8),
('HM010', 'Herramientas', 'Manuales', 'Cinta métrica 8m autoblocante', 'Stanley', 'Unidad', 6490, 20, 6),
('HE001', 'Herramientas', 'Eléctricas', 'Taladro percutor 650W 13mm', 'Makita', 'Unidad', 79990, 8, 2),
('HE002', 'Herramientas', 'Eléctricas', 'Atornillador inalámbrico 12V (kit)', 'Makita', 'Unidad', 104990, 5, 2),
('HE003', 'Herramientas', 'Eléctricas', 'Amoladora angular 4.5" 800W', 'Makita', 'Unidad', 54990, 8, 2),
('HE004', 'Herramientas', 'Eléctricas', 'Sierra circular 7-1/4" 1200W', 'Skil', 'Unidad', 72990, 4, 1),
('HE005', 'Herramientas', 'Eléctricas', 'Lijadora orbital 180W', 'Black+Decker', 'Unidad', 34990, 6, 2),
('HE006', 'Herramientas', 'Eléctricas', 'Caladora 500W', 'Skil', 'Unidad', 42990, 4, 1),
('GS001', 'Gasfitería', 'Tuberías', 'Cañería PVC 1/2" x 6m', 'Tigre', 'Unidad', 5490, 30, 10),
('GS002', 'Gasfitería', 'Tuberías', 'Cañería PVC 3/4" x 6m', 'Tigre', 'Unidad', 7490, 25, 8),
('GS003', 'Gasfitería', 'Tuberías', 'Cañería cobre 1/2" x 5m', 'Codelco', 'Unidad', 17990, 15, 5),
('GS004', 'Gasfitería', 'Uniones', 'Codo PVC 1/2" 90°', 'Tigre', 'Unidad', 390, 100, 30),
('GS005', 'Gasfitería', 'Uniones', 'Te PVC 1/2"', 'Tigre', 'Unidad', 450, 80, 25),
('GS006', 'Gasfitería', 'Uniones', 'Unión doble PVC 1/2"', 'Tigre', 'Unidad', 320, 80, 25),
('GS007', 'Gasfitería', 'Llaves', 'Llave de paso esfera 1/2" latón', 'Emmeti', 'Unidad', 3490, 30, 10),
('GS008', 'Gasfitería', 'Llaves', 'Llave de paso esfera 3/4" latón', 'Emmeti', 'Unidad', 4990, 20, 8),
('GS009', 'Gasfitería', 'Grifería', 'Grifería lavamanos monocomando cromo', 'Corona', 'Unidad', 22990, 10, 3),
('GS010', 'Gasfitería', 'Grifería', 'Grifería cocina monocomando cuello alto', 'Corona', 'Unidad', 28990, 8, 2),
('GS011', 'Gasfitería', 'Sellos', 'Teflón 3/4" x 12m', '3M', 'Rollo', 790, 80, 25),
('GS012', 'Gasfitería', 'Sellos', 'Silicona transparente 280ml', 'Wacker', 'Cartucho', 5490, 25, 8),
('EL001', 'Electricidad', 'Conductores', 'Cable unipolar 1.5mm² (por metro)', 'Condulac', 'Metro', 590, 100, 30),
('EL002', 'Electricidad', 'Conductores', 'Cable unipolar 2.5mm² (por metro)', 'Condulac', 'Metro', 790, 100, 30),
('EL003', 'Electricidad', 'Conductores', 'Cable dúplex paralelo 2x1.5mm² (por metro)', 'Condulac', 'Metro', 990, 80, 25),
('EL004', 'Electricidad', 'Enchufes', 'Enchufe empotrar 16A c/tierra (schuko)', 'Bticino', 'Unidad', 3690, 50, 15),
('EL005', 'Electricidad', 'Enchufes', 'Enchufe doble empotrar 16A c/tierra', 'Bticino', 'Unidad', 5490, 40, 12),
('EL006', 'Electricidad', 'Interruptores', 'Interruptor simple empotrar', 'Bticino', 'Unidad', 3190, 50, 15),
('EL007', 'Electricidad', 'Interruptores', 'Interruptor doble empotrar', 'Bticino', 'Unidad', 4290, 35, 10),
('EL008', 'Electricidad', 'Tableros', 'Tablero eléctrico 4 espacios DIN', 'Legrand', 'Unidad', 22990, 8, 2),
('EL009', 'Electricidad', 'Tableros', 'Disyuntor termomagnético 16A unipolar', 'Schneider', 'Unidad', 6490, 20, 5),
('EL010', 'Electricidad', 'Tableros', 'Disyuntor termomagnético 25A unipolar', 'Schneider', 'Unidad', 6990, 20, 5),
('EL011', 'Electricidad', 'Iluminación', 'Ampolleta LED 9W E27 luz fría', 'Philips', 'Unidad', 3990, 60, 20),
('EL012', 'Electricidad', 'Iluminación', 'Ampolleta LED 12W E27 luz cálida', 'Philips', 'Unidad', 4490, 50, 15),
('EL013', 'Electricidad', 'Iluminación', 'Panel LED empotrar 18W 22cm', 'Ledvance', 'Unidad', 11490, 20, 6),
('TR001', 'Tornillería', 'Tornillos', 'Tornillo autoperf. 8x1" caja 100 unid.', 'Hilti', 'Caja', 2990, 40, 12),
('TR002', 'Tornillería', 'Tornillos', 'Tornillo madera 4x40mm caja 100 unid.', 'Hilti', 'Caja', 2490, 40, 12),
('TR003', 'Tornillería', 'Tornillos', 'Tornillo volcanita 3.5x25mm caja 200 unid.', 'Hilti', 'Caja', 3490, 30, 10),
('TR004', 'Tornillería', 'Tacos', 'Taco fisher S6 bolsa 100 unid.', 'Fischer', 'Bolsa', 3190, 35, 10),
('TR005', 'Tornillería', 'Tacos', 'Taco fisher S8 bolsa 50 unid.', 'Fischer', 'Bolsa', 2890, 30, 10),
('TR006', 'Tornillería', 'Pernos', 'Perno hex. 3/8" x 2" c/tuerca y golilla', 'Granel', 'Unidad', 290, 200, 50),
('TR007', 'Tornillería', 'Anclajes', 'Perno de anclaje 10x100mm', 'Hilti', 'Unidad', 990, 60, 15),
('TR008', 'Tornillería', 'Anclajes', 'Anclaje químico epoxi 300ml', 'Fischer', 'Cartucho', 15990, 8, 2),
('MD001', 'Madera', 'Pino cepillado', 'Pino cepillado 1x3" x 3m', 'Local', 'Unidad', 4290, 40, 12),
('MD002', 'Madera', 'Pino cepillado', 'Pino cepillado 2x4" x 3m', 'Local', 'Unidad', 7490, 30, 10),
('MD003', 'Madera', 'Tableros', 'Terciado estructural 18mm 1.22x2.44m', 'Arauco', 'Plancha', 34990, 20, 5),
('MD004', 'Madera', 'Tableros', 'MDF 15mm 1.22x2.44m', 'Arauco', 'Plancha', 27990, 15, 4),
('MD005', 'Madera', 'Tableros', 'OSB 9mm 1.22x2.44m', 'Arauco', 'Plancha', 18990, 18, 5),
('MD006', 'Madera', 'Tableros', 'Volcanita estándar 10mm 1.2x2.4m', 'Volcán', 'Plancha', 8990, 30, 8),
('MD007', 'Madera', 'Tableros', 'Volcanita resistente humedad 10mm', 'Volcán', 'Plancha', 11990, 15, 4),
('SE001', 'Seguridad', 'EPP', 'Casco seguridad blanco', '3M', 'Unidad', 6990, 15, 5),
('SE002', 'Seguridad', 'EPP', 'Guantes de cuero talla L', '3M', 'Par', 3690, 20, 6),
('SE003', 'Seguridad', 'EPP', 'Antiparras de seguridad clear', '3M', 'Unidad', 2490, 25, 8),
('SE004', 'Seguridad', 'EPP', 'Mascarilla respirador N95 (caja x10)', '3M', 'Caja', 10990, 10, 3),
('SE005', 'Seguridad', 'EPP', 'Arnés de seguridad 1 punto', '3M', 'Unidad', 34990, 4, 1),
('JA001', 'Jardín', 'Mangueras', 'Manguera riego 3/4" x 25m', 'Tigre', 'Unidad', 22990, 6, 2),
('JA002', 'Jardín', 'Mangueras', 'Manguera expandible 30m', 'Bestway', 'Unidad', 28990, 5, 2),
('JA003', 'Jardín', 'Mangueras', 'Pistola de riego 8 modos', 'Bestway', 'Unidad', 5490, 10, 3),
('JA004', 'Jardín', 'Herramientas', 'Pala punta redonda #2 con mango', 'Corona', 'Unidad', 10990, 8, 2),
('JA005', 'Jardín', 'Herramientas', 'Rastrillo 16 dientes con mango', 'Corona', 'Unidad', 9490, 6, 2);

-- pedidos.json
INSERT INTO pedido (id, cliente_id, fecha, estado, medio_pago, total) VALUES
(1, 3, '2026-09-15', 'Pendiente', 'contado', 34760),
(2, 4, '2026-09-17', 'En preparación', 'cuenta_corriente', 102000),
(3, 3, '2026-09-20', 'Entregado', 'contado', 19980);

-- items de cada pedido
INSERT INTO detalle_pedido (pedido_id, producto_codigo, cantidad, precio_unitario) VALUES
(1, 'MC001', 4, 5990),
(1, 'MC005', 6, 1800),
(2, 'MC007', 200, 380),
(2, 'MC003', 5, 5200),
(3, 'PT001', 2, 9990);

-- pagos.json
INSERT INTO pago (id, cliente_id, fecha, monto) VALUES
(1, 4, '2026-09-22', 40000),
(2, 4, '2026-09-26', 30000);
