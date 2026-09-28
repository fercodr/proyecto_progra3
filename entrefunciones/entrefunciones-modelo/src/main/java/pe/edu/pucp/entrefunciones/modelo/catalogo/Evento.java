package pe.edu.pucp.entrefunciones.modelo.catalogo;

import java.util.ArrayList;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;
import pe.edu.pucp.entrefunciones.modelo.funciones.Funcion;

public abstract class Evento implements IEliminable {

    private int id;
    private double precio;
    private boolean activo;
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
        setActivo(evento.isActivo());
        funciones = new ArrayList<>();
    }

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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
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

    @Override
    public void eliminar() {
        activo = false;
    }
}
