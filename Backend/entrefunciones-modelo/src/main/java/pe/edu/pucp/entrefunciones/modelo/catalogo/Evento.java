package pe.edu.pucp.entrefunciones.modelo.catalogo;

import java.util.ArrayList;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;

public abstract class Evento implements IEliminable {

    private int id;
    private double precio;
    private boolean activo;
    private String imagenUrl;
    private ArrayList<Funcion> funciones;

    public Evento() {
        funciones = new ArrayList<>();
        activo = true;
    }

    public Evento(int id, double precio) {
        setId(id);
        setPrecio(precio);
        activo = true;
        funciones = new ArrayList<>();
    }

    protected Evento(Evento evento) {
        setId(evento.getId());
        setPrecio(evento.getPrecio());
        this.activo = evento.estaActivo();
        funciones = new ArrayList<>();
    }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id no puede ser negativo.");
        }
        this.id = id;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.precio = precio;
    }

    @Override
    public void activar() {
        this.activo = true;
    }

    @Override
    public void desactivar() {
        this.activo = false;
    }

    @Override
    public boolean estaActivo() {
        return activo;
    }

    /**
     * Indica si el contenido de este evento se vende por asiento numerado
     * (Cineplanet) o por aforo general (Joinnus, sin butaca puntual).
     * Por defecto es asiento numerado; ActividadCultural la sobreescribe.
     */
    public boolean requiereAsientoNumerado() {
        return true;
    }

    public ArrayList<Funcion> getFunciones() {
        return funciones;
    }

    public void agregarFuncion(Funcion funcion) {
        if (funcion == null) {
            throw new IllegalArgumentException("La función no puede ser null.");
        }
        if (!funciones.contains(funcion)) {
            funciones.add(funcion);
        }
        if (funcion.getEvento() != this) {
            funcion.setEvento(this);
        }
    }

    public abstract String getTitulo();
}
