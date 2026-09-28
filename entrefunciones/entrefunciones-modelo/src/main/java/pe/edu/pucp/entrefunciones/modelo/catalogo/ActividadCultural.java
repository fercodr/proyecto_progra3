/* package pe.edu.pucp.entrefunciones.modelo.catalogo;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import java.time.LocalDate;
import java.time.LocalTime;
/**
 * Una funcion de actividad cultural concreta: hereda de Funcion (sala,
 * fecha, horario, precio, idioma, estado) y agrega los datos propios de la
 * actividad (nombre, expositor, tipo). Si la misma actividad se programa en
 * varios horarios, cada horario es un objeto ActividadCultural distinto que
 * repite estos datos de catalogo (ver seccion 8 del informe). Ademas de
 * EstadoFuncion (heredado, para el flujo de negocio de programacion),
 * implementa IEliminable para poder retirarse del sistema de forma
 * administrativa sin perder el historial.
 */
/*
public class ActividadCultural extends Funcion implements IEliminable {
    private String nombre;
    private String expositor;
    private TipoActividad tipoActividad;
    private boolean activo;

    public ActividadCultural (int id, Sala sala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, double precio, String idioma,
                              String nombre, String expositor, TipoActividad tipoActividad){
        super(id, sala, fecha, horaInicio, horaFin, precio, idioma);
        setNombre(nombre);
        setExpositor(expositor);
        setTipoActividad(tipoActividad);
        this.activo=true;
    }

    //Constructor copia
    public ActividadCultural (final ActividadCultural actividadCultural){
        super(actividadCultural);
        setNombre(actividadCultural.getNombre());
        setExpositor(actividadCultural.getExpositor());
        setTipoActividad(actividadCultural.getTipoActividad());
        if(actividadCultural.estaActivo()){
            activar();
        } else {
            desactivar();
        }
    }

    @Override
    public String getTitulo(){
        return nombre;
    }

    @Override
    public void activar(){
        this.activo=true;
    }

    @Override
    public void desactivar(){
        this.activo=false;
    }

    @Override
    public boolean estaActivo(){
        return activo;
    }

    public String getNombre(){
        return nombre;
    }

    public void setNombre(String nombre){
        if(nombre == null || nombre.isEmpty()){
            throw new IllegalArgumentException("nombre no puede ser nulo o vacio");
        }
        this.nombre=nombre;
    }

    public String getExpositor(){
        return expositor;
    }

    public void setExpositor(String expositor){
        this.expositor=expositor;
    }

    public TipoActividad getTipoActividad(){
        return tipoActividad;
    }

    public void setTipoActividad(TipoActividad tipoActividad){
        if(tipoActividad == null){
            throw new IllegalArgumentException("tipoActividad no puede ser nulo");
        }
        this.tipoActividad=tipoActividad;
    }

}
*/
package pe.edu.pucp.entrefunciones.modelo.catalogo;

public class ActividadCultural extends Evento {

    private String nombre;
    private String expositor;
    private TipoActividad tipoActividad;

    public ActividadCultural(int id, double precio,
                             String nombre,
                             String expositor,
                             TipoActividad tipoActividad) {

        super(id, precio);

        setNombre(nombre);
        setExpositor(expositor);
        setTipoActividad(tipoActividad);
    }

    /** Constructor de copia. */
    public ActividadCultural(
            final ActividadCultural actividadCultural) {

        super(actividadCultural);

        setNombre(actividadCultural.getNombre());
        setExpositor(actividadCultural.getExpositor());
        setTipoActividad(actividadCultural.getTipoActividad());
        for (Funcion funcion : actividadCultural.getFunciones()) {
            Funcion copiaFuncion = new Funcion(funcion);
            agregarFuncion(copiaFuncion);
        }

    }

    @Override
    public boolean requiereAsientoNumerado() {
        return false;
    }
    
    @Override
    public String getTitulo() {
        return nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException(
                    "nombre no puede ser nulo o vacio"
            );
        }
        this.nombre = nombre;
    }

    public String getExpositor() {
        return expositor;
    }

    public void setExpositor(String expositor) {
        this.expositor = expositor;
    }

    public TipoActividad getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(TipoActividad tipoActividad) {
        if (tipoActividad == null) {
            throw new IllegalArgumentException(
                    "tipoActividad no puede ser nulo"
            );
        }
        this.tipoActividad = tipoActividad;
    }
}
