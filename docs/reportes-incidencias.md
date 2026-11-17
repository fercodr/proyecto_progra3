# Reportes e incidencias

Este documento resume el alcance implementado para RF-M6-02, RF-M6-03 y
RF-M6-04. El [catálogo funcional vigente](CATALOGO.md) es la referencia de
alcance.

## Reportes

- RF-M6-02 calcula ocupación como entradas vigentes emitidas entre aforo de la
  función. Incluye entradas pagadas y gratuitas y excluye las canceladas.
- RF-M6-03 informa ingresos de pagos validados, entradas emitidas y entradas
  canceladas. Las entradas gratuitas no producen ingreso monetario.
- Los filtros admiten sede, sala y tipo de evento según el reporte. `desde` y
  `hasta` son independientes: pueden omitirse ambos, informarse uno solo o
  enviarse juntos. Cuando existen ambos, `desde` no puede ser posterior a
  `hasta`; los límites son inclusivos.
- El periodo sigue usando la fecha de la función. La definición uniforme del
  periodo continúa pendiente de conformidad de la JP.

## Incidencias

RF-M6-04 registra una incidencia asociada a una función y permite actualizar
descripción, responsable y estado de atención sobre el mismo registro. Se
conservan su identificador, fecha y función histórica. El origen es la
comunicación de una sede o cliente; no se presupone detección automática.

## Migración de otro esquema existente

Antes de ejecutar SQL se debe respaldar el esquema, seleccionar explícitamente
el destino y comprobarlo con `SELECT DATABASE()`. Ningún paso cambia `DB_NAME`
ni copia datos de prueba.

Para un esquema que todavía no contiene los cambios de LAB12:

1. Aplicar la estructura de distrito, accesibilidad, temática, público objetivo
   y bloqueos. Puede usarse `20261115_disponibilidad_cartelera.sql`. El script
   `20261116_actualizar_bd_compartida.sql` cubre la misma estructura para la BD
   compartida, pero contiene `USE entrefunciones`; no debe ejecutarse sobre otro
   esquema sin adaptar y revisar explícitamente ese destino.
2. Aplicar `20261115_reporte_tipo_evento.sql` para instalar el contrato de
   reportes con filtro por tipo de evento.
3. Aplicar `20261115_incidencias_edicion.sql` para instalar la actualización de
   incidencias.
4. Aplicar `20261116_reportes_fechas_opcionales.sql` para admitir límites de
   fecha opcionales.

Los scripts están en `Backend/sql/migraciones/` y no incluyen fixtures. Si el
esquema se crea desde los SQL canónicos actuales, su estado final ya está
incluido y no se deben repetir migraciones incrementalmente sin comparar antes
las definiciones.

## Reversión

`ROLLBACK_20261116_reportes_fechas_opcionales.sql` restaura únicamente
`listar_reportes_funciones` al contrato anterior, que exige ambas fechas. Debe
ejecutarse sobre el mismo esquema autorizado y después de respaldar el
procedimiento vigente. Las migraciones de estructura e incidencias no tienen
un rollback destructivo automático porque deben preservar datos históricos.
