INSERT INTO transportistas (nombre, codigo_interno, contacto, regiones_cobertura, activo)
SELECT 'Starken', 'STK', '+56800123456', 'I,II,III,IV,V,VI,VII,VIII,IX,X,XI,XII,RM,XIV,XV,XVI', true 
WHERE NOT EXISTS (SELECT 1 FROM transportistas WHERE codigo_interno = 'STK');

INSERT INTO transportistas (nombre, codigo_interno, contacto, regiones_cobertura, activo)
SELECT 'Paket', 'PKT', '+56800654321', 'RM,V,VI,VII,VIII', true 
WHERE NOT EXISTS (SELECT 1 FROM transportistas WHERE codigo_interno = 'PKT');
