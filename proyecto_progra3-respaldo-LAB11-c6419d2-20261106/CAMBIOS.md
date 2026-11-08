# Correcciones entregadas

El paquete corresponde a los puntos técnicos 1–5 y 8 solicitados. Los puntos 6 y 7, sobre decisiones visuales, continúan a cargo del grupo. Las páginas de compra y Ventas conservan su estructura y reciben los enlaces funcionales necesarios para operar con datos persistidos.

| Punto | Cambio entregado | Evidencia principal |
|---|---|---|
| SQL | Nombre canónico reserva_butaca, 68 procedimientos consolidados, migración de datos y catálogo de contratos DAO | Backend/sql y docs/catalogo-sp.md |
| Compra | Disponibilidad real, creación de reserva y navegación al pago por ID de reserva | SeleccionButacas, ReservaPago, CompraRestService y VentasResource |
| Disponibilidad | Validación de sala activa, cruce de horarios y concurrencia al programar | FuncionBLImpl, FuncionDAOImpl y procedimientos SQL |
| Reservas y pagos | Butacas numeradas y aforo general, precio persistido, vencimiento automático y emisión atómica e idempotente | ReservaBLImpl, PagoBLImpl, ControlOperacionesDAO y temporizador EJB |
| Word | Modelo vigente, 19 pantallas, ficha P17 y coherencia entre catálogo y programación | Informe_TareaAcademica_LAB11.docx |
| Recursos y Bloqueos | Retiro de entidades, DAO, BL, páginas, menús y tablas; limpieza por migración | Código y SQL vigentes |

Los bloqueos técnicos de filas de la BD se mantienen para evitar ventas simultáneas de una misma butaca o cupo; son un mecanismo de transacciones, distinto del módulo Bloqueos eliminado.

## Para integrar

Leer INTEGRACION.md antes de cargar SQL o desplegar. Indica el orden de instalación o migración, la configuración de ambos servidores, los diez contratos REST, las rutas directas para probar la compra y el trabajo pendiente de los demás módulos. VERIFICACION.md documenta los resultados y el alcance de las pruebas.

El cambio del catálogo de RF se presenta como propuesta para el JP. Las capturas históricas del LAB09 están identificadas como tales; el equipo actualizará la interfaz y sus capturas.

## Resultado comprobado

24 pruebas JUnit correctas; compilación de los fuentes Java y del frontend C# correcta; instalación y migración comprobadas en MariaDB aislado. No se aplicaron cambios a la BD compartida ni al repositorio remoto. El empaquetado Maven y el despliegue final de GlassFish siguen pendientes de comprobación.

El archivo diferencias.patch permite revisar los cambios de texto respecto al ZIP recibido. Excluye artefactos binarios y esta documentación de entrega. No incluye los archivos temporales usados para preparar y comprobar el paquete.
