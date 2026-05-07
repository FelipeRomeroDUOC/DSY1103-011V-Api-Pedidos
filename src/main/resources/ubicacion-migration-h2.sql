CREATE TABLE IF NOT EXISTS provincias (
    id_provincia VARCHAR(3) NOT NULL,
    nombre_provincia VARCHAR(120) NOT NULL,
    id_region VARCHAR(2) NOT NULL,
    PRIMARY KEY (id_provincia)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_provincias_nombre_provincia ON provincias(nombre_provincia);

-- Ensure the new column exists on comunas (nullable for now)
ALTER TABLE IF EXISTS comunas
    ADD COLUMN IF NOT EXISTS id_provincia VARCHAR(3);

-- Populate provincias (idempotent) using the known catalogue
MERGE INTO provincias (id_provincia, nombre_provincia, id_region) KEY(id_provincia) VALUES
('011','Iquique','15'),
('014','Tamarugal','15'),
('021','Antofagasta','02'),
('022','El Loa','02'),
('023','Tocopilla','02'),
('031','Copiapó','03'),
('032','Chañaral','03'),
('033','Huasco','03'),
('041','Elqui','04'),
('042','Choapa','04'),
('043','Limarí','04'),
('051','Valparaíso','05'),
('052','Isla de Pascua','05'),
('053','Los Andes','05'),
('054','Petorca','05'),
('055','Quillota','05'),
('056','San Antonio','05'),
('057','San Felipe de Aconcagua','05'),
('058','Marga Marga','05'),
('061','Cachapoal','06'),
('062','Cardenal Caro','06'),
('063','Colchagua','06'),
('071','Talca','07'),
('072','Curicó','07'),
('073','Cauquenes','07'),
('074','Linares','07'),
('081','Concepción','08'),
('082','Arauco','08'),
('083','Biobío','08'),
('091','Cautín','09'),
('092','Malleco','09'),
('101','Llanquihue','10'),
('102','Chiloé','10'),
('103','Osorno','10'),
('104','Palena','10'),
('111','Coyhaique','11'),
('112','Aysén','11'),
('113','General Carrera','11'),
('114','Capitán Prat','11'),
('121','Magallanes','12'),
('122','Antártica Chilena','12'),
('123','Tierra del Fuego','12'),
('124','Última Esperanza','12'),
('131','Santiago','13'),
('132','Cordillera','13'),
('133','Chacabuco','13'),
('134','Maipo','13'),
('135','Melipilla','13'),
('136','Talagante','13'),
('141','Valdivia','14'),
('142','Ranco','14'),
('151','Arica','15'),
('152','Parinacota','15'),
('161','Diguillín','16'),
('162','Itata','16'),
('163','Punilla','16');

-- Fill id_provincia for existing comunas derived from their id_comuna
UPDATE comunas SET id_provincia = SUBSTRING(id_comuna, 1, 3) WHERE id_provincia IS NULL;

-- Now that values exist, enforce NOT NULL and foreign key
ALTER TABLE IF EXISTS comunas ALTER COLUMN id_provincia SET NOT NULL;

ALTER TABLE IF EXISTS comunas
    ADD CONSTRAINT IF NOT EXISTS fk_comunas_provincia FOREIGN KEY (id_provincia) REFERENCES provincias(id_provincia);
