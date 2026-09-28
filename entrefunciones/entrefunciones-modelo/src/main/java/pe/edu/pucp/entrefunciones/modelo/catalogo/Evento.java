package pe.edu.pucp.entrefunciones.modelo.catalogo;

import pe.edu.pucp.entrefunciones.modelo.IEliminable;
import pe.edu.pucp.entrefunciones.modelo.funciones.Funcion;

import java.util.ArrayList;

public abstract class Evento implements IEliminable {

    private int id;
    private double precio;
    private ArrayList<Funcion> funciones;
    private boolean activo;

    public Evento() {
        this.funciones = new ArrayList<>();
        this.activo = true;
    }

    public Evento(int id, double precio) {
        this.id = id;
        this.precio = precio;
        this.funciones = new ArrayList<>();
        this.activo = true;
    }
    
    protected Evento(final Evento evento) {
    
        if (evento == null) {
            throw new IllegalArgumentException(
                    "evento no puede ser nulo"
            );
        }
    
        setId(evento.getId());
        setPrecio(evento.getPrecio());
        setFunciones(new ArrayList<>(evento.getFunciones()));
    
        if (evento.estaActivo()) {
            activar();
        } else {
            desactivar();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public ArrayList<Funcion> getFunciones() {
        return funciones;
    }

    public void setFunciones(ArrayList<Funcion> funciones) {
        this.funciones = funciones;
    }

    public abstract String getTitulo();

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
}
