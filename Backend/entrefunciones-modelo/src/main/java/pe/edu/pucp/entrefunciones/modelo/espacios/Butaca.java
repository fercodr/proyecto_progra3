package pe.edu.pucp.entrefunciones.modelo.espacios;
import pe.edu.pucp.entrefunciones.modelo.IEliminable;

public class Butaca implements IEliminable {
    private int id;
    private Sala sala;
    private String fila;
    private int columna;
    private String tipoButaca;
    private boolean activo;

    public Butaca (int id, Sala sala, String fila, int columna, String tipoButaca){
        setId(id);
        setSala(sala);
        setFila(fila);
        setColumna(columna);
        setTipoButaca(tipoButaca);
        this.activo=true;
    }
    /** Constructor de copia. */
    public Butaca (final Butaca butaca){
        if(butaca == null){
            throw new IllegalArgumentException("butaca no puede ser nulo");
        }
        setId(butaca.getId());
        setSala(butaca.getSala());
        setFila(butaca.getFila());
        setColumna(butaca.getColumna());
        setTipoButaca(butaca.getTipoButaca());
        if(butaca.estaActivo()){
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

    public Sala getSala(){
        return sala;
    }

    public void setSala(Sala sala){
        if(sala == null){
            throw new IllegalArgumentException("sala no puede ser nulo");
        }
        this.sala=sala;
    }

    public String getFila(){
        return fila;
    }

    public void setFila(String fila){
        if(fila == null || fila.isEmpty()){
            throw new IllegalArgumentException("fila no puede ser nulo o vacio");
        }
        this.fila=fila;
    }

    public int getColumna(){
        return columna;
    }

    public void setColumna(int columna){
        if(columna < 0){
            throw new IllegalArgumentException("columna no puede ser negativo");
        }
        this.columna=columna;
    }

    public String getTipoButaca(){
        return tipoButaca;
    }

    public void setTipoButaca(String tipoButaca){
        if(tipoButaca == null || tipoButaca.isEmpty()){
            throw new IllegalArgumentException("tipoButaca no puede ser nulo o vacio");
        }
        this.tipoButaca=tipoButaca;
    }

}
