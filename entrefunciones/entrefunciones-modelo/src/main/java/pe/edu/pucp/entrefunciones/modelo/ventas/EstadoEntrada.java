package pe.edu.pucp.entrefunciones.modelo.ventas;
/**
 * Estado de una entrada dentro del historial de compras de un cliente
 * (ver RF-M5-03). Tambien es el mecanismo de eliminado logico de Entrada:
 * una entrada cancelada no se elimina, cambia de estado.
 */
public enum EstadoEntrada{
    VIGENTE ,
    UTILIZADA ,
    CANCELADA,
}
