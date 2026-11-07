package pe.edu.pucp.entrefunciones.modelo.catalogo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;

public class Funcion {

    private int id;
    private Evento evento;
    private Sala sala;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String idioma;
    private EstadoFuncion estado;
    private ArrayList<Entrada> entradas;

    // Constructor normal
    public Funcion(int id, Evento evento, Sala sala,
                   LocalDate fecha, LocalTime horaInicio,
                   LocalTime horaFin, String idioma) {

        if (horaInicio != null && horaFin != null
                && !horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException(
                    "horaFin debe ser posterior a horaInicio"
            );
        }

        setId(id);
        setEvento(evento);
        setSala(sala);
        setFecha(fecha);
        setHoraInicio(horaInicio);
        setHoraFin(horaFin);
        setIdioma(idioma);

        this.estado = EstadoFuncion.PROGRAMADA;
        this.entradas = new ArrayList<>();
    }

    // Constructor de copia
    public Funcion(Funcion funcion) {

        if (funcion == null) {
            throw new IllegalArgumentException(
                    "funcion no puede ser nula"
            );
        }

        setId(funcion.getId());
        setEvento(funcion.getEvento());
        setSala(funcion.getSala());
        setFecha(funcion.getFecha());
        setHoraInicio(funcion.getHoraInicio());
        setHoraFin(funcion.getHoraFin());
        setIdioma(funcion.getIdioma());
        setEstado(funcion.getEstado());

        this.entradas = new ArrayList<>(funcion.getEntradas());

        // Copia la misma referencia de contenido; no duplica el catálogo.
    }

    public int calcularDuracion() {
        return (int) java.time.Duration
                .between(horaInicio, horaFin)
                .toMinutes();
    }

    //agregado
    public boolean requiereAsientoNumerado() {
        return evento.requiereAsientoNumerado();
    }

    public void agregarEntrada(Entrada entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException(
                    "entrada no puede ser nula"
            );
        }

        entradas.add(entrada);
    }

    /**
     * Confirma una función programada y la publica en cartelera.
     */
    public boolean confirmar() {
        if (estado == EstadoFuncion.PROGRAMADA) {
            estado = EstadoFuncion.CONFIRMADA;
            return true;
        }

        return false;
    }

    /**
     * Cancela una función.
     */
    public boolean cancelar() {
        if (estado != EstadoFuncion.CANCELADA) {
            estado = EstadoFuncion.CANCELADA;
            return true;
        }

        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException(
                    "id no puede ser negativo"
            );
        }

        this.id = id;
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

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        if (sala == null) {
            throw new IllegalArgumentException(
                    "sala no puede ser nulo"
            );
        }

        this.sala = sala;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "fecha no puede ser nulo"
            );
        }

        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        if (horaInicio == null) {
            throw new IllegalArgumentException(
                    "horaInicio no puede ser nulo"
            );
        }

        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        if (horaFin == null) {
            throw new IllegalArgumentException(
                    "horaFin no puede ser nulo"
            );
        }

        if (horaInicio != null && !horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException(
                    "horaFin debe ser posterior a horaInicio"
            );
        }

        this.horaFin = horaFin;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        if (idioma == null || idioma.isEmpty()) {
            throw new IllegalArgumentException(
                    "idioma no puede ser nulo o vacio"
            );
        }

        this.idioma = idioma;
    }

    public EstadoFuncion getEstado() {
        return estado;
    }

    public void setEstado(EstadoFuncion estado) {
        if (estado == null) {
            throw new IllegalArgumentException(
                    "estado no puede ser nulo"
            );
        }

        this.estado = estado;
    }

    public ArrayList<Entrada> getEntradas() {
        return entradas;
    }

    public void setEntradas(ArrayList<Entrada> entradas) {
        if (entradas == null) {
            throw new IllegalArgumentException(
                    "entradas no puede ser nulo"
            );
        }

        this.entradas = entradas;
    }
}
