package pe.edu.pucp.entrefunciones.modelo.espacios;
import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.entrefunciones.modelo.IEliminable;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
public class Sala implements IEliminable {
    private int id;
    private Sede sede;
    private String nombre;
    private int aforo;
    private String tipoSala;
    private boolean activo;
    private List<Butaca> butacas = new ArrayList<>();
    private List<Funcion> funciones = new ArrayList<>();

    public Sala(){

    }

    public Sala (int id, Sede sede, String nombre, int aforo, String tipoSala){
        setId(id);
        setSede(sede);
        setNombre(nombre);
        setAforo(aforo);
        setTipoSala(tipoSala);
        this.activo=true;
    }

    /** Constructor de copia (no copia butacas ni funciones: usar agregarButaca/agregarFuncion). */
    public Sala (final Sala sala){
        if(sala == null){
            throw new IllegalArgumentException("sala no puede ser nulo");
        }
        setId(sala.getId());
        setSede(sala.getSede());
        setNombre(sala.getNombre());
        setAforo(sala.getAforo());
        setTipoSala(sala.getTipoSala());
        if(sala.estaActivo()){
            activar();
        } else {
            desactivar();
        }
    }

    /** Mantiene consistente la referencia inversa Butaca -> Sala (RF-M2-03: la butaca pertenece a esta sala). */
    public void agregarButaca(Butaca butaca){
        butacas.add(butaca);
        butaca.setSala(this);
    }

    /** Mantiene consistente la referencia inversa Funcion -> Sala (RF-M3-01: la funcion se programa en esta sala). */
    public void agregarFuncion(Funcion funcion){
        funciones.add(funcion);
        funcion.setSala(this);
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

    public Sede getSede(){
        return new Sede(sede);
    }

    public void setSede(Sede sede){
        if(sede == null){
            throw new IllegalArgumentException("sede no puede ser nulo");
        }
        this.sede=new Sede(sede);
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

    public int getAforo(){
        return aforo;
    }

    public void setAforo(int aforo){
        if(aforo <= 0){
            throw new IllegalArgumentException("aforo debe ser mayor a cero");
        }
        this.aforo=aforo;
    }

    public String getTipoSala(){
        return tipoSala;
    }

    public void setTipoSala(String tipoSala){
        if(tipoSala == null || tipoSala.isEmpty()){
            throw new IllegalArgumentException("tipoSala no puede ser nulo o vacio");
        }
        this.tipoSala=tipoSala;
    }

    public List<Butaca> getButacas(){
        return List.copyOf(butacas);
    }

    public void setButacas(List<Butaca> butacas){
        if(butacas == null){
            throw new IllegalArgumentException("butacas no puede ser nulo");
        }
        this.butacas=butacas;
    }

    public List<Funcion> getFunciones(){

        return List.copyOf(funciones);
    }

    public void setFunciones(ArrayList<Funcion> funciones){
        if(funciones == null){
            throw new IllegalArgumentException("funciones no puede ser nulo");
        }
        this.funciones=funciones;
    }

}
