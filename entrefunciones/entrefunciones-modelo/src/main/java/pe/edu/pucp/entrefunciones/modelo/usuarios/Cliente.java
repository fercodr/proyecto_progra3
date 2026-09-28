package pe.edu.pucp.entrefunciones.modelo.usuarios;
import java.util.ArrayList;
import java.time.LocalDate;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
public class Cliente extends Usuario {
    private LocalDate fechaRegistro;
    private String telefono;
    private ArrayList<Entrada> entradas = new ArrayList<>();

    public Cliente (int id, String nombres, String apellidos, String correo, String contrasena, String dni, LocalDate fechaRegistro, String telefono){
        super (id,nombres,apellidos,correo,contrasena,dni);
        setFechaRegistro(fechaRegistro);
        setTelefono(telefono);
    }

    /** Constructor de copia (no copia el historial de entradas: usar comprarEntrada). */
    public Cliente (final Cliente cliente){
        super(cliente);
        setFechaRegistro(cliente.getFechaRegistro());
        setTelefono(cliente.getTelefono());
    }

    public Entrada comprarEntrada(Entrada entrada){
        entradas.add(entrada);
        return entrada;
    }

    public LocalDate getFechaRegistro(){
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro){
        if(fechaRegistro == null){
            throw new IllegalArgumentException("fechaRegistro no puede ser nulo");
        }
        this.fechaRegistro=fechaRegistro;
    }

    public String getTelefono(){
        return telefono;
    }

    public void setTelefono(String telefono){
        this.telefono=telefono;
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
