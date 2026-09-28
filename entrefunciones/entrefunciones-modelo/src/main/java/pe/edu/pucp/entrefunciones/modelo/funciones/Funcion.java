package pe.edu.pucp.entrefunciones.modelo.funciones;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
/**
 * Una Funcion es la programacion concreta de un contenido en una fecha,
 * horario y precio determinados. Es una clase abstracta: Pelicula,
 * ObraTeatro y ActividadCultural heredan de ella y agregan sus propios datos
 * de catalogo (titulo, genero, elenco, etc.). Como cada una de esas
 * subclases ES una Funcion (y no solo tiene una referencia a un contenido),
 * si la misma pelicula se programa en dos horarios distintos (p. ej. 3pm y
 * 6pm), sus datos de catalogo se repiten en cada objeto, porque cada
 * funcion es a la vez el contenido que se proyecta (ver seccion 8 del
 * informe).
 */
public abstract class Funcion {
    private int id;
    private Evento evento;
    private Sala sala;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    //private double precio;
    private String idioma;
    private EstadoFuncion estado;
    private ArrayList<Entrada> entradas = new ArrayList<>();

    public Funcion (int id, Sala sala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, double precio, String idioma){
        if(horaInicio != null && horaFin != null && !horaFin.isAfter(horaInicio)){
            throw new IllegalArgumentException("horaFin debe ser posterior a horaInicio");
        }
        setId(id);
        setEvento(evento);
        setSala(sala);
        setFecha(fecha);
        setHoraInicio(horaInicio);
        setHoraFin(horaFin);
        setPrecio(precio);
        setIdioma(idioma);
        this.estado=EstadoFuncion.PROGRAMADA;
        this.entradas = new ArrayList<>();
    }

    /** Constructor de copia (no copia la lista de entradas: usar agregarEntrada). Lo invocan las subclases con super(...). */
    protected Funcion (final Funcion funcion){
        if(funcion == null){
            throw new IllegalArgumentException("funcion no puede ser nulo");
        }
        setId(funcion.getId());
        setEvento(funcion.getEvento());
        setSala(funcion.getSala());
        setFecha(funcion.getFecha());
        setHoraInicio(funcion.getHoraInicio());
        setHoraFin(funcion.getHoraFin());
        setPrecio(funcion.getPrecio());
        setIdioma(funcion.getIdioma());
        setEstado(funcion.getEstado());
        this.entradas = new ArrayList<>(funcion.getEntradas());
    }

    /** Titulo del contenido que se proyecta en esta funcion; cada subclase concreta lo define. */
    //public abstract String getTitulo();

    public int calcularDuracion(){
        return (int) Duration.between(horaInicio, horaFin).toMinutes();
    }

    public void agregarEntrada(Entrada entrada){
        if (entrada == null) { 
            throw new IllegalArgumentException( 
                "entrada no puede ser nula" 
            ); 
        }
        entradas.add(entrada);
    }

    /** RF-M3-03: el administrador confirma una funcion programada y la publica en cartelera. */
    public boolean confirmar(){
        if(estado == EstadoFuncion.PROGRAMADA){
            estado = EstadoFuncion.CONFIRMADA;
            return true;
        }
        return false;
    }

    /**
     * Eliminado logico de una funcion: en vez de borrarla, se marca CANCELADA.
     * Una funcion ya cancelada no puede volver a cancelarse.
     */
    public boolean cancelar(){
        if(estado != EstadoFuncion.CANCELADA){
            estado = EstadoFuncion.CANCELADA;
            return true;
        }
        return false;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        if(id < 0){
            throw new IllegalArgumentException("id no puede ser negativo");
        }
        this.id=id;
    }
    
    public Evento getEvento() { 
        return evento; 
    } 
    
    public void setEvento(Evento evento) { 
        if (evento == null) { 
            throw new IllegalArgumentException( 
                "evento no puede ser nulo" 
            ); 
        } 
        this.evento = evento; 
    }
    
    public Sala getSala(){
        return sala;
    }

    public void setSala(Sala sala){
        if(sala == null){
            throw new IllegalArgumentException("sala no puede ser nulo");
        }
        this.sala=sala;
    }

    public LocalDate getFecha(){
        return fecha;
    }

    public void setFecha(LocalDate fecha){
        if(fecha == null){
            throw new IllegalArgumentException("fecha no puede ser nulo");
        }
        this.fecha=fecha;
    }

    public LocalTime getHoraInicio(){
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio){
        if(horaInicio == null){
            throw new IllegalArgumentException("horaInicio no puede ser nulo");
        }
        this.horaInicio=horaInicio;
    }

    public LocalTime getHoraFin(){
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin){
        if(horaFin == null){
            throw new IllegalArgumentException("horaFin no puede ser nulo");
        }
        this.horaFin=horaFin;
    }

    /*public double getPrecio(){
        return precio;
    }

    public void setPrecio(double precio){
        if(precio < 0){
            throw new IllegalArgumentException("precio no puede ser negativo");
        }
        this.precio=precio;
    }
    */

    public String getIdioma(){
        return idioma;
    }

    public void setIdioma(String idioma){
        if(idioma == null || idioma.isEmpty()){
            throw new IllegalArgumentException("idioma no puede ser nulo o vacio");
        }
        this.idioma=idioma;
    }

    public EstadoFuncion getEstado(){
        return estado;
    }

    public void setEstado(EstadoFuncion estado){
        if(estado == null){
            throw new IllegalArgumentException("estado no puede ser nulo");
        }
        this.estado=estado;
    }

    public ArrayList<Entrada> getEntradas(){
        return entradas;
    }

    public void setEntradas(ArrayList<Entrada> entradas){
        if(entradas == null){
            throw new IllegalArgumentException("entradas no puede ser nulo");
        }
        this.entradas=entradas;
    }

}
