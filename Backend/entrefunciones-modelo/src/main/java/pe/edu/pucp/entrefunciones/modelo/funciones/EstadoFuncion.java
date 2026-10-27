package pe.edu.pucp.entrefunciones.modelo.funciones;
/**
 * Estado del ciclo de vida de una funcion (pelicula, obra de teatro o
 * actividad cultural programada en una sala y horario). Reemplaza el
 * eliminado fisico de una funcion: una funcion nunca se borra, se cancela.
 */
public enum EstadoFuncion{
    PROGRAMADA ,
    CONFIRMADA ,
    CANCELADA,
}
