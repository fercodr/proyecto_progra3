package pe.edu.pucp.entrefunciones.modelo.espacios;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;
/** RF-M2-02: recurso que la sede pone a disposicion de una funcion (proyector, equipo de sonido, personal tecnico, etc.). */
public class Recurso implements IEliminable {
    private int id;
    private String nombre;
    private String tipo;
    private boolean activo;

    public Recurso (int id, String nombre, String tipo){
        setId(id);
        setNombre(nombre);
        setTipo(tipo);
        this.activo=true;
    }

    /** Constructor de copia. */
    public Recurso (final Recurso recurso){
        if(recurso == null){
            throw new IllegalArgumentException("recurso no puede ser nulo");
        }
        setId(recurso.getId());
        setNombre(recurso.getNombre());
        setTipo(recurso.getTipo());
        if(recurso.estaActivo()){
            activar();
        } else {
            desactivar();
        }
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

    public String getNombre(){
        return nombre;
    }

    public void setNombre(String nombre){
        if(nombre == null || nombre.isEmpty()){
            throw new IllegalArgumentException("nombre no puede ser nulo o vacio");
        }
        this.nombre=nombre;
    }

    public String getTipo(){
        return tipo;
    }

    public void setTipo(String tipo){
        if(tipo == null || tipo.isEmpty()){
            throw new IllegalArgumentException("tipo no puede ser nulo o vacio");
        }
        this.tipo=tipo;
    }

}
