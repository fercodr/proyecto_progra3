# EntreFunciones: instrucciones de arquitectura y persistencia

## Objetivo y alcance
Desarrollar y corregir EntreFunciones conservando la arquitectura por capas del curso PROG03. Adaptar los patrones del profesor a nuestras entidades; no copiar el dominio comercial de SoftProg. Respetar el alcance de cada tarea, los cambios del equipo y las instrucciones explícitas del usuario.

## Fuentes y decisiones
- Referencia de código: `referencias/profesor/softprog/`.
- Origen: https://github.com/erichuizapucp/programacion-3/tree/289ff0c44b79f7c39f98a735fc78f6dffb994604/2026-2/sesion-de-clase-09/softprog
- Snapshot consultado el 29-09-2026. No cambiar esta referencia al implementar EntreFunciones.
- Antes de editar, leer el POM raíz, POM de los módulos afectados, modelo, DAO, BL, SQL y pruebas relacionados del repositorio de EntreFunciones.
- El informe adjunto conserva Recurso, Bloqueo, Auditoria y otras decisiones que difieren del Markdown adjunto. No asumir que el nombre o fecha de un documento resuelve la contradicción. Mantener el modelo existente para tareas de sintaxis o capas; pedir una decisión específica solo si la implementación solicitada depende del alcance contradictorio. No eliminar ni renombrar entidades automáticamente.
- El informe indica que las relaciones del modelo conservan identidad compartida: no introducir copias defensivas en sus getters/setters sin revisar el grafo bidireccional y sus pruebas.
- Las interfaces de servicios por módulo del Markdown no sustituyen automáticamente las interfaces DAO/BL genéricas del curso.

## Patrones comprobados del profesor
Consultar estos archivos relativos a la referencia:
- `softprog-dbmanager/src/main/java/pe/edu/pucp/softprog/db/DBManager.java`: Singleton con acceso sincronizado; getConnection crea una conexión JDBC.
- `softprog-accesodatos/src/main/java/pe/edu/pucp/softprog/dao/DAO.java`: contrato DAO genérico.
- `softprog-accesodatos/src/main/java/pe/edu/pucp/softprog/dao/impl/RegistroDAOImpl.java`: mapeo de atributos comunes.
- `softprog-accesodatos/src/main/java/pe/edu/pucp/softprog/dao/transacciones/TransactionsManager.java`: clase final, constructor privado, métodos estáticos y ThreadLocal<Connection>.
- `softprog-negocio/src/main/java/pe/edu/pucp/softprog/bl/RegistroBL.java`, `BLException.java` y `impl/AreaBLImpl.java`: contratos BL, validaciones y traducción de SQLException conservando la causa.
- `softprog-negocio/src/main/java/pe/edu/pucp/softprog/bl/impl/OrdenVentaBLImpl.java`: BL como propietaria del inicio, commit y rollback de la operación compuesta.
- `softprog-accesodatos/src/main/java/pe/edu/pucp/softprog/dao/impl/ventas/OrdenVentaDAOImpl.java` y `LineaOrdenVentaDAOImpl.java`: cabecera y detalles escritos con la misma conexión transaccional; statements cerrados localmente.

La referencia contiene ejemplos didácticos; las reglas de robustez y concurrencia de abajo son adaptaciones necesarias para nuestro proyecto, no afirmaciones de que el profesor ya las implementó todas.

## Responsabilidades de cada capa
- Modelo: entidades, estados, relaciones y validaciones intrínsecas. Sin SQL, JDBC ni dependencia de BL.
- DBManager: configuración y apertura de conexiones. No conservar una Connection global compartida entre usuarios.
- Acceso a datos: contratos DAO e implementaciones, parámetros SQL, ejecución de procedimientos y mapeo ResultSet. Propagar SQLException; no convertir un fallo en null, lista vacía o éxito.
- Negocio: reglas entre entidades, autorización, cálculos y coordinación de operaciones. Consumir interfaces DAO, conservar las interfaces BL y lanzar BLException con causa cuando corresponda. No ejecutar SQL ni crear conexiones directamente.
- App/presentación: consumir BL o servicios; no acceder directamente a DAO o DBManager para operaciones del negocio.
- Mantener los módulos Maven existentes: modelo, dbmanager, accesodatos, negocio y app. No introducir ciclos ni tecnologías que sustituyan la arquitectura del curso.
- Reutilizar bases genéricas y métodos de mapeo existentes cuando correspondan; no inventar una jerarquía nueva para homogeneizar nombres.

## Transacciones: reglas obligatorias
1. Definir la unidad atómica por caso de uso. Si varias escrituras deben completarse juntas, la BL coordinadora es la única propietaria de iniciar, commit y rollback.
2. Mantener TransactionsManager con conexión por hilo, constructor privado y rechazo de un inicio cuando ya existe una transacción activa. No reemplazarlo por una Connection estática global.
3. Iniciar: abrir conexión, desactivar autoCommit y publicarla en ThreadLocal después de configurarla correctamente. Si falla el inicio, cerrar la conexión creada.
4. Todos los DAO participantes, incluidos detalles, auditoría y consultas necesarias para decidir una escritura, deben usar la misma conexión. No abrir una conexión independiente dentro de ese flujo.
5. Una Connection prestada por TransactionsManager no va en try-with-resources ni se cierra desde el DAO. El DAO sí cierra sus propios CallableStatement/PreparedStatement y ResultSet.
6. Fuera de una transacción, quien abre una conexión local debe cerrarla con try-with-resources. Revisar si un DAO admite ambos modos o exige transacción; mantener ese contrato explícito y consistente en sus llamadas.
7. Confirmar una sola vez, después de completar todas las escrituras. Los DAO y procedimientos participantes no hacen commits internos, rollbacks propios ni cambios de autoCommit que fragmenten la unidad atómica. Revisar también los SQL.
8. Si falla cualquier paso después del inicio, incluidos SQLException, BLException o RuntimeException relevantes, intentar rollback mientras siga activa la transacción. No limitar la protección a SQLException.
9. Si iniciar falla, no ejecutar rollback de una transacción inexistente. Si commit ya limpió la conexión, no llamar rollback incondicionalmente.
10. Preservar la excepción original: los fallos de rollback o limpieza se agregan como excepciones suprimidas o se registran de forma segura; no deben ocultar la causa inicial.
11. Quitar el ThreadLocal y cerrar la conexión en todos los caminos de terminación. Un fallo al restaurar autoCommit no debe impedir intentar close. No restaurar autoCommit con escrituras pendientes sin resolver: puede confirmar cambios.
12. No permitir que una BL secundaria confirme o revierta una transacción que inició la BL coordinadora. Separar operación pública y operación interna participante cuando haga falta.
13. Ejecutar los DAO participantes en el mismo hilo. ThreadLocal no propaga contexto a tareas asíncronas.
14. Respetar dependencias: crear padre antes de detalles y obtener su identificador; retirar dependientes antes del padre cuando corresponda al SQL y al eliminado lógico. No aplicar DELETE físico a entidades que conservan historial.
15. No anunciar éxito antes de commit. Si hubo rollback, no presentar identificadores o estados mutados en memoria como persistidos.

## Concurrencia e integridad: adaptaciones para EntreFunciones
- ThreadLocal y synchronized en el Singleton no bloquean reservas concurrentes en la base de datos. Tampoco basta con validar disponibilidad y luego insertar sin protección.
- Validar en memoria formato, nulos y rangos antes de abrir la transacción. Revalidar dentro de ella disponibilidad, estado, vencimiento y demás datos mutables que determinan la escritura.
- Para aforo, butacas y horarios, elegir y documentar un mecanismo compatible con el esquema real: bloqueo de una fila estable con SELECT ... FOR UPDATE, actualización condicional con comprobación del resultado u otra estrategia verificable. Usar motor transaccional y revisar aislamiento, índices y restricciones.
- La ocupación se determina por función y butaca, no por un booleano global en Butaca.
- Ordenar de forma consistente los bloqueos de varias filas (por ejemplo, identificadores ascendentes). Si hay reintentos por deadlock, deben ser acotados y repetir la unidad completa con protección frente a duplicados.
- Hacer idempotente la confirmación de pago: repetirla no debe emitir nuevas entradas ni duplicar movimientos. Estados, restricciones y cambios condicionales deben impedir carreras; no confiar solo en una consulta previa.
- Al confirmar una compra, actualizar los estados y emitir entradas dentro de la unidad atómica definida; incluir su auditoría obligatoria. La creación de una reserva temporal es otra operación: no mantener una transacción abierta mientras una persona realiza el pago simulado.
- No exceder aforo, duplicar reservas vigentes de la misma butaca y función ni emitir entradas para un pago no confirmado.
- No agregar constraints incompatibles con el historial: una butaca debe poder volver a reservarse cuando una reserva anterior expire o se cancele.

## Seguridad y contratos SQL
- Parametrizar valores; no concatenar entrada del usuario al SQL. Verificar nombres, orden/tipos de parámetros IN/OUT, alias de columnas, nulos con Types/wasNull y forma de resultados contra los procedimientos reales.
- Verificar el efecto esperado de la operación con el contrato del procedimiento; no copiar executeUpdate() == 0 como regla universal para todos los procedimientos o drivers.
- Validar autorización y alcance en backend, usando el contexto autenticado; no confiar en roles enviados por la pantalla.
- No copiar credenciales de referencia ni publicar db.properties real. Usar configuración externa y ejemplos sin secretos. Evitar registrar contraseñas, datos bancarios o datos personales innecesarios.
- Conservar eliminado lógico, trazabilidad y ciclos de estados del modelo vigente. No modificar DDL ni ejecutar scripts sobre la base compartida como consecuencia automática de una corrección Java.

## Verificación y entrega
- Ejecutar compilación y pruebas pertinentes con los POM reales; normalmente desde el agregador Maven: mvn test. Si el entorno no permite completar una verificación, explicar qué falta.
- Para cambios de transacciones, verificar éxito completo, fallo del segundo paso sin escrituras parciales, fallo en validación dentro de la transacción, conexión prestada sin cierre prematuro y ausencia de ThreadLocal residual.
- Probar que errores de commit/rollback/close no oculten la causa original y que la siguiente operación pueda iniciar normalmente.
- Para reservas o pagos, añadir pruebas con conexiones independientes: competencia por la misma butaca, límite de aforo y confirmación repetida/concurrente sin entradas duplicadas. Los mocks por sí solos no demuestran integridad concurrente.
- Usar base de pruebas y datos aislados para integración; no afirmar pruebas de BD ejecutadas si no hubo conexión.
- Entregar qué cambió, patrón del profesor usado, correcciones de robustez añadidas, pruebas realmente ejecutadas y pendientes concretos. No afirmar que AGENTS.md por sí solo garantiza seguridad o sincronización.
