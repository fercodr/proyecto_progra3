package pe.edu.pucp.entrefunciones.modelo.usuarios;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;
public abstract class Usuario implements IAutenticable, IEliminable {
    private int id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasena;
    private String dni;
    private boolean activo;

    public Usuario (int id, String nombres, String apellidos, String correo, String contrasena, String dni){
        setId(id);
        setNombres(nombres);
        setApellidos(apellidos);
        setCorreo(correo);
        setContrasena(contrasena);
        setDni(dni);
        this.activo=true;
    }

    /** Constructor de copia: crea un Usuario con los mismos datos que otro. */
    public Usuario (final Usuario usuario){
        if(usuario == null){
            throw new IllegalArgumentException("usuario no puede ser nulo");
        }
        setId(usuario.getId());
        setNombres(usuario.getNombres());
        setApellidos(usuario.getApellidos());
        setCorreo(usuario.getCorreo());
        setContrasena(usuario.getContrasena());
        setDni(usuario.getDni());
        if(usuario.estaActivo()){
            activar();
        } else {
            desactivar();
        }
    }

    public boolean iniciarSesion(){
        return validarCredenciales(this.correo, this.contrasena);
    }

    public void cerrarSesion(){
        System.out.println("Sesion cerrada para " + this.correo);
    }

    @Override
    public boolean validarCredenciales(String correo, String contrasena){
        return this.correo.equals(correo) && this.contrasena.equals(contrasena);
    }

    @Override
    public void recuperarContrasena(){
        System.out.println("Se envio un correo de recuperacion a " + this.correo);
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

    public int getId(){
        return id;
    }

    public void setId(int id){
        if(id < 0){
            throw new IllegalArgumentException("id no puede ser negativo");
        }
        this.id=id;
    }

    public String getNombres(){
        return nombres;
    }

    public void setNombres(String nombres){
        if(nombres == null || nombres.isEmpty()){
            throw new IllegalArgumentException("nombres no puede ser nulo o vacio");
        }
        this.nombres=nombres;
    }

    public String getApellidos(){
        return apellidos;
    }

    public void setApellidos(String apellidos){
        if(apellidos == null || apellidos.isEmpty()){
            throw new IllegalArgumentException("apellidos no puede ser nulo o vacio");
        }
        this.apellidos=apellidos;
    }

    public String getCorreo(){
        return correo;
    }

    public void setCorreo(String correo){
        if(correo == null || correo.isEmpty()){
            throw new IllegalArgumentException("correo no puede ser nulo o vacio");
        }
        this.correo=correo;
    }

    public String getContrasena(){
        return contrasena;
    }

    public void setContrasena(String contrasena){
        if(contrasena == null || contrasena.isEmpty()){
            throw new IllegalArgumentException("contrasena no puede ser nulo o vacio");
        }
        this.contrasena=contrasena;
    }

    public String getDni(){
        return dni;
    }

    public void setDni(String dni){
        if(dni == null || dni.isEmpty()){
            throw new IllegalArgumentException("dni no puede ser nulo o vacio");
        }
        this.dni=dni;
    }

}
