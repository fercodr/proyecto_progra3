package pe.edu.pucp.entrefunciones.modelo.catalogo;
import pe.edu.pucp.entrefunciones.modelo.funciones;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
public abstract class Evento {

    private int id;
    private double precio;
    private ArrayList<Funcion> funciones;

    public Evento() {
        funciones = new ArrayList<>();
    }

    public Evento(int id, double precio) {
        this.id = id;
        this.precio = precio;
        this.funciones = new ArrayList<>();
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
}
