package pe.edu.pucp.entrefunciones.modelo.espacios;
import java.util.ArrayList;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;
public class Sede implements IEliminable {
    private int id;
    private String razonSocial;
    private String direccion;
    private String ciudad;
    private String telefonoContacto;
    private boolean activo;
    private ArrayList<Sala> salas = new ArrayList<>();

    public Sede (int id, String razonSocial, String direccion, String ciudad, String telefonoContacto){
        setId(id);
        setRazonSocial(razonSocial);
        setDireccion(direccion);
        setCiudad(ciudad);
        setTelefonoContacto(telefonoContacto);
        this.activo=true;
    }

    /** Constructor de copia (no copia la lista de salas: usar agregarSala). */
    public Sede (final Sede sede){
        if(sede == null){
            throw new IllegalArgumentException("sede no puede ser nulo");
        }
        setId(sede.getId());
        setRazonSocial(sede.getRazonSocial());
        setDireccion(sede.getDireccion());
        setCiudad(sede.getCiudad());
        setTelefonoContacto(sede.getTelefonoContacto());
        if(sede.estaActivo()){
            activar();
        } else {
            desactivar();
        }
    }

    /** Mantiene consistente la referencia inversa Sala -> Sede (Sede "1" o-- "*" Sala). */
    public void agregarSala(Sala sala){
        salas.add(sala);
        sala.setSede(this);
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

    public String getRazonSocial(){
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial){
        if(razonSocial == null || razonSocial.isEmpty()){
            throw new IllegalArgumentException("razonSocial no puede ser nulo o vacio");
        }
        this.razonSocial=razonSocial;
    }

    public String getDireccion(){
        return direccion;
    }

    public void setDireccion(String direccion){
        if(direccion == null || direccion.isEmpty()){
            throw new IllegalArgumentException("direccion no puede ser nulo o vacio");
        }
        this.direccion=direccion;
    }

    public String getCiudad(){
        return ciudad;
    }

    public void setCiudad(String ciudad){
        if(ciudad == null || ciudad.isEmpty()){
            throw new IllegalArgumentException("ciudad no puede ser nulo o vacio");
        }
        this.ciudad=ciudad;
    }

    public String getTelefonoContacto(){
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto){
        this.telefonoContacto=telefonoContacto;
    }

    public ArrayList<Sala> getSalas(){
        return salas;
    }

    public void setSalas(ArrayList<Sala> salas){
        if(salas == null){
            throw new IllegalArgumentException("salas no puede ser nulo");
        }
        this.salas=salas;
    }

}
