/*package pe.edu.pucp.entrefunciones.modelo.catalogo;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import java.time.LocalDate;
import java.time.LocalTime;
/**
 * Una funcion de teatro concreta: hereda de Funcion (sala, fecha, horario,
 * precio, idioma, estado) y agrega los datos propios de la obra que se
 * representa (titulo, director, elenco). Si la misma obra se programa en
 * varios horarios, cada horario es un objeto ObraTeatro distinto que repite
 * estos datos de catalogo (ver seccion 8 del informe). Ademas de
 * EstadoFuncion (heredado, para el flujo de negocio de programacion),
 * implementa IEliminable para poder retirarse del sistema de forma
 * administrativa sin perder el historial.

public class ObraTeatro extends Funcion implements IEliminable {
    private String titulo;
    private String nombreDirector;
    private String elenco;
    private boolean activo;

    public ObraTeatro (int id, Sala sala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, double precio, String idioma,
                       String titulo, String nombreDirector, String elenco){
        super(id, sala, fecha, horaInicio, horaFin, precio, idioma);
        setTitulo(titulo);
        setNombreDirector(nombreDirector);
        setElenco(elenco);
        this.activo=true;
    }

    //Constructor copia
    public ObraTeatro (final ObraTeatro obraTeatro){
        super(obraTeatro);
        setTitulo(obraTeatro.getTitulo());
        setNombreDirector(obraTeatro.getNombreDirector());
        setElenco(obraTeatro.getElenco());
        if(obraTeatro.estaActivo()){
            activar();
        } else {
            desactivar();
        }
    }

    @Override
    public String getTitulo(){
        return titulo;
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

    public void setTitulo(String titulo){
        if(titulo == null || titulo.isEmpty()){
            throw new IllegalArgumentException("titulo no puede ser nulo o vacio");
        }
        this.titulo=titulo;
    }

    public String getNombreDirector(){
        return nombreDirector;
    }

    public void setNombreDirector(String nombreDirector){
        this.nombreDirector=nombreDirector;
    }

    public String getElenco(){
        return elenco;
    }

    public void setElenco(String elenco){
        this.elenco=elenco;
    }

}
*/
package pe.edu.pucp.entrefunciones.modelo.catalogo;

public class ObraTeatro extends Evento {

    private String titulo;
    private String nombreDirector;
    private String elenco;

    public ObraTeatro(int id, double precio,
                      String titulo,
                      String nombreDirector,
                      String elenco) {

        super(id, precio);

        setTitulo(titulo);
        setNombreDirector(nombreDirector);
        setElenco(elenco);
    }

    /** Constructor de copia. */
    public ObraTeatro(final ObraTeatro obraTeatro) {

        super(obraTeatro);
    
        setTitulo(obraTeatro.getTitulo());
        setNombreDirector(obraTeatro.getNombreDirector());
        setElenco(obraTeatro.getElenco());
        for (Funcion funcion : obraTeatro.getFunciones()) {
            Funcion copiaFuncion = new Funcion(funcion);
            agregarFuncion(copiaFuncion);
        }
    }

    @Override
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isEmpty()) {
            throw new IllegalArgumentException(
                    "titulo no puede ser nulo o vacio"
            );
        }
        this.titulo = titulo;
    }

    public String getNombreDirector() {
        return nombreDirector;
    }

    public void setNombreDirector(String nombreDirector) {
        this.nombreDirector = nombreDirector;
    }

    public String getElenco() {
        return elenco;
    }

    public void setElenco(String elenco) {
        this.elenco = elenco;
    }
}
