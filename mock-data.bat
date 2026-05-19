@echo off
echo ==========================================
echo   Generador de Mock Data - Api Pedidos
echo ==========================================
echo.
echo Asegurate de que TODOS los microservicios esten corriendo usando start-all.bat
echo Presiona cualquier tecla para comenzar a inyectar datos...
pause >nul

echo.
echo [1/6] Creando Clientes (cliente-service :8082)...
curl -s -X POST http://localhost:8082/api/clientes -H "Content-Type: application/json" -d "{\"nombre\":\"Tech Solutions S.A.\",\"rut\":\"76.543.210-K\",\"email\":\"contacto@techsolutions.cl\",\"telefono\":\"+56912345678\",\"direccion\":\"Av. Apoquindo 4500, Las Condes\"}" >nul
curl -s -X POST http://localhost:8082/api/clientes -H "Content-Type: application/json" -d "{\"nombre\":\"Juan Perez\",\"rut\":\"15.123.456-7\",\"email\":\"juan.perez@gmail.com\",\"telefono\":\"+56987654321\",\"direccion\":\"Pasaje Los Pinos 123, Maipu\"}" >nul
curl -s -X POST http://localhost:8082/api/clientes -H "Content-Type: application/json" -d "{\"nombre\":\"Muebles de Lujo SPA\",\"rut\":\"77.111.222-3\",\"email\":\"ventas@muebleslujo.cl\",\"telefono\":\"+56999887766\",\"direccion\":\"Av. Providencia 1000\"}" >nul
echo OK.

echo.
echo [2/6] Creando Productos (producto-service :8083)...
curl -s -X POST http://localhost:8083/api/productos -H "Content-Type: application/json" -d "{\"nombre\":\"Silla Ergonomica Pro\",\"descripcion\":\"Silla de oficina ajustable\",\"precio\":149990.0,\"stock\":50,\"sku\":\"SIL-PRO-01\"}" >nul
curl -s -X POST http://localhost:8083/api/productos -H "Content-Type: application/json" -d "{\"nombre\":\"Escritorio Gamer LED\",\"descripcion\":\"Escritorio con luces RGB 120cm\",\"precio\":219990.0,\"stock\":15,\"sku\":\"ESC-GAM-02\"}" >nul
curl -s -X POST http://localhost:8083/api/productos -H "Content-Type: application/json" -d "{\"nombre\":\"Mesa de Comedor Roble\",\"descripcion\":\"Mesa de madera maciza para 6 personas\",\"precio\":350000.0,\"stock\":5,\"sku\":\"MES-ROB-03\"}" >nul
curl -s -X POST http://localhost:8083/api/productos -H "Content-Type: application/json" -d "{\"nombre\":\"Estante Minimalista\",\"descripcion\":\"Estanteria de acero y madera\",\"precio\":85000.0,\"stock\":30,\"sku\":\"EST-MIN-04\"}" >nul
echo OK.

echo.
echo [3/6] Creando Pedidos (pedido-service :8081)...
:: Pedido 1 - Tech Solutions (Sillas y Escritorios)
curl -s -X POST http://localhost:8081/api/pedidos -H "Content-Type: application/json" -d "{\"numeroPedido\":\"PED-2026-001\",\"clienteId\":1,\"tipoDespacho\":\"RM\",\"items\":[{\"productoId\":1,\"cantidad\":10,\"precioUnitario\":149990.0},{\"productoId\":2,\"cantidad\":5,\"precioUnitario\":219990.0}]}" >nul

:: Pedido 2 - Juan Perez (Mesa de comedor)
curl -s -X POST http://localhost:8081/api/pedidos -H "Content-Type: application/json" -d "{\"numeroPedido\":\"PED-2026-002\",\"clienteId\":2,\"tipoDespacho\":\"REGION\",\"items\":[{\"productoId\":3,\"cantidad\":1,\"precioUnitario\":350000.0}]}" >nul

:: Pedido 3 - Muebles de Lujo (Estantes y Sillas)
curl -s -X POST http://localhost:8081/api/pedidos -H "Content-Type: application/json" -d "{\"numeroPedido\":\"PED-2026-003\",\"clienteId\":3,\"tipoDespacho\":\"RETIRO\",\"items\":[{\"productoId\":4,\"cantidad\":20,\"precioUnitario\":85000.0},{\"productoId\":1,\"cantidad\":20,\"precioUnitario\":149990.0}]}" >nul

:: Pedido 4 - Juan Perez (Silla extra)
curl -s -X POST http://localhost:8081/api/pedidos -H "Content-Type: application/json" -d "{\"numeroPedido\":\"PED-2026-004\",\"clienteId\":2,\"tipoDespacho\":\"RM\",\"items\":[{\"productoId\":1,\"cantidad\":1,\"precioUnitario\":149990.0}]}" >nul
echo OK.

echo.
echo [4/6] Simulando Fabricacion (fabricacion-service :8086)...
:: Iniciar fabricacion para el Pedido 1
curl -s -X POST http://localhost:8086/api/fabricacion -H "Content-Type: application/json" -d "{\"numeroPedido\":1,\"usuarioResponsable\":\"Operador Pedro\"}" >nul
:: Terminar fabricacion Pedido 1 (Pasa a LISTO)
curl -s -X PATCH http://localhost:8086/api/fabricacion/1/estado -H "Content-Type: application/json" -d "{\"nuevoEstado\":\"TERMINADO\"}" >nul

:: Iniciar fabricacion para el Pedido 2
curl -s -X POST http://localhost:8086/api/fabricacion -H "Content-Type: application/json" -d "{\"numeroPedido\":2,\"usuarioResponsable\":\"Operador Luis\"}" >nul
:: Este se queda EN_PROCESO

:: Iniciar fabricacion para el Pedido 3
curl -s -X POST http://localhost:8086/api/fabricacion -H "Content-Type: application/json" -d "{\"numeroPedido\":3,\"usuarioResponsable\":\"Operador Pedro\"}" >nul
:: Terminar fabricacion Pedido 3 (Pasa a LISTO)
curl -s -X PATCH http://localhost:8086/api/fabricacion/3/estado -H "Content-Type: application/json" -d "{\"nuevoEstado\":\"TERMINADO\"}" >nul
echo OK.

echo.
echo [5/6] Despachando pedidos LISTOS (despacho-service :8084)...
:: Despachamos Pedido 1 (A RM)
curl -s -X POST http://localhost:8084/api/despachos -H "Content-Type: application/json" -d "{\"pedidoId\":1,\"tipoDespacho\":\"RM\",\"fechaDespacho\":\"2026-05-20\"}" >nul

:: Despachamos Pedido 3 (RETIRO)
curl -s -X POST http://localhost:8084/api/despachos -H "Content-Type: application/json" -d "{\"pedidoId\":3,\"tipoDespacho\":\"RETIRO\",\"fechaDespacho\":\"2026-05-22\"}" >nul
echo OK.

echo.
echo [6/6] Sincronizando estado (estado-service :8085)...
:: Los estados ya fueron capturados automaticamente via Feign gracias a nuestra arquitectura.
echo OK.

echo.
echo ==========================================
echo  MOCK FINALIZADO CON EXITO
echo ==========================================
echo Ahora puedes consultar metrica-service (Puerto 8087)
echo Ejemplo Ranking: http://localhost:8087/api/metricas/clientes/ranking
echo Ejemplo Top Ventas: http://localhost:8087/api/metricas/productos/top
echo.
pause
