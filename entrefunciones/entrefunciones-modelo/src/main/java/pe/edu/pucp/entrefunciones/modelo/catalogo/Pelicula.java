package pe.edu.pucp.entrefunciones.modelo.catalogo;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.funciones.Funcion;
import java.time.LocalDate;
import java.time.LocalTime;
/**
 * Una funcion de cine concreta: hereda de Funcion (sala, fecha, horario,
 * precio, idioma, estado) y agrega los datos propios de la pelicula que se
 * proyecta (titulo, duracion, genero, clasificacion, director). Si la misma
 * pelicula se programa en varios horarios, cada horario es un objeto
 * Pelicula distinto que repite estos datos de catalogo (ver seccion 8 del
 * informe). Ademas de EstadoFuncion (heredado, para el flujo de negocio de
 * programacion), implementa IEliminable para poder retirarse del sistema de
 * forma administrativa sin perder el historial.
 */
public class Pelicula extends Funcion implements IEliminable {
    private String nombrePelicula;
    private int duracionMinutos;
    private CategoriaGenero genero;
    private ClasificacionEdad clasificacionPorEdad;
    private String director;
    private boolean activo;

    public Pelicula (int id, Sala sala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, double precio, String idioma,
                     String nombrePelicula, int duracionMinutos, CategoriaGenero genero, ClasificacionEdad clasificacionPorEdad, String director){
        super(id, sala, fecha, horaInicio, horaFin, precio, idioma);
        setNombrePelicula(nombrePelicula);
        setDuracionMinutos(duracionMinutos);
        setGenero(genero);
        setClasificacionPorEdad(clasificacionPorEdad);
        setDirector(director);
        this.activo=true;
    }

    /** Constructor de copia. */
    public Pelicula (final Pelicula pelicula){
        super(pelicula);
        setNombrePelicula(pelicula.getNombrePelicula());
        setDuracionMinutos(pelicula.getDuracionMinutos());
        setGenero(pelicula.getGenero());
        setClasificacionPorEdad(pelicula.getClasificacionPorEdad());
        setDirector(pelicula.getDirector());
        if(pelicula.estaActivo()){
            activar();
        } else {
            desactivar();
        }
    }

    @Override
    public String getTitulo(){
        return nombrePelicula;
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

    public String getNombrePelicula(){
        return nombrePelicula;
    }

    public void setNombrePelicula(String nombrePelicula){
        if(nombrePelicula == null || nombrePelicula.isEmpty()){
            throw new IllegalArgumentException("nombrePelicula no puede ser nulo o vacio");
        }
        this.nombrePelicula=nombrePelicula;
    }

    public int getDuracionMinutos(){
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos){
        if(duracionMinutos <= 0){
            throw new IllegalArgumentException("duracionMinutos debe ser mayor a cero");
        }
        this.duracionMinutos=duracionMinutos;
    }

    public CategoriaGenero getGenero(){
        return genero;
    }

    public void setGenero(CategoriaGenero genero){
        if(genero == null){
            throw new IllegalArgumentException("genero no puede ser nulo");
        }
        this.genero=genero;
    }

    public ClasificacionEdad getClasificacionPorEdad(){
        return clasificacionPorEdad;
    }

    public void setClasificacionPorEdad(ClasificacionEdad clasificacionPorEdad){
        if(clasificacionPorEdad == null){
            throw new IllegalArgumentException("clasificacionPorEdad no puede ser nulo");
        }
        this.clasificacionPorEdad=clasificacionPorEdad;
    }

    public String getDirector(){
        return director;
    }

    public void setDirector(String director){
        this.director=director;
    }

}
