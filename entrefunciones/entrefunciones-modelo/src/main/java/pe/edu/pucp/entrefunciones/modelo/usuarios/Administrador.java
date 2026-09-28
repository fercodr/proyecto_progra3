package pe.edu.pucp.entrefunciones.modelo.usuarios;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
public class Administrador extends Usuario {
    private String codigoEmpleado;

    public Administrador (int id, String nombres, String apellidos, String correo, String contrasena, String dni, String codigoEmpleado){
        super (id,nombres,apellidos,correo,contrasena,dni);
        setCodigoEmpleado(codigoEmpleado);
    }

    /** Constructor de copia. */
    public Administrador (final Administrador administrador){
        super(administrador);
        setCodigoEmpleado(administrador.getCodigoEmpleado());
    }

    /** RF-M3-03: el administrador confirma una funcion programada y la publica en cartelera. */
    public boolean gestionarFuncion(Funcion funcion){
        boolean confirmada = funcion.confirmar();
        System.out.println("El administrador " + getNombres() + " gestiono la funcion " + funcion.getId()
                + (confirmada ? ": confirmada." : ": no se pudo confirmar (estado actual: " + funcion.getEstado() + ")."));
        return confirmada;
    }

    public String getCodigoEmpleado(){
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado){
        if(codigoEmpleado == null || codigoEmpleado.isEmpty()){
            throw new IllegalArgumentException("codigoEmpleado no puede ser nulo o vacio");
        }
        this.codigoEmpleado=codigoEmpleado;
    }

}
