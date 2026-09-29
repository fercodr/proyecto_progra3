package pe.edu.pucp.entrefunciones.modelo.espacios;
import java.time.LocalDateTime;


public class Bloqueo {
    private int id;
    private String motivo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Sala sala;
    private Recurso recurso;

    public Bloqueo (){

    }

    /** Constructor de copia. */
    public Bloqueo (final Bloqueo bloqueo){
        if(bloqueo == null){
            throw new IllegalArgumentException("bloqueo no puede ser nulo");
        }
        if((sala == null) == (recurso == null)){
            throw new IllegalArgumentException("un bloqueo debe tener exactamente una sala o un recurso, nunca ambos ni ninguno");
        }
        if(fechaInicio != null && fechaFin != null && !fechaFin.isAfter(fechaInicio)){
            throw new IllegalArgumentException("fechaFin debe ser posterior a fechaInicio");
        }
        setId(bloqueo.getId());
        setMotivo(bloqueo.getMotivo());
        setFechaInicio(bloqueo.getFechaInicio());
        setFechaFin(bloqueo.getFechaFin());
        this.sala=bloqueo.getSala();
        this.recurso=bloqueo.getRecurso();
    }

    public boolean estaVigente(LocalDateTime instante){
        return !instante.isBefore(fechaInicio) && !instante.isAfter(fechaFin);
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

    public String getMotivo(){
        return motivo;
    }

    public void setMotivo(String motivo){
        if(motivo == null || motivo.isEmpty()){
            throw new IllegalArgumentException("motivo no puede ser nulo o vacio");
        }
        this.motivo=motivo;
    }

    public LocalDateTime getFechaInicio(){
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio){
        if(fechaInicio == null){
            throw new IllegalArgumentException("fechaInicio no puede ser nulo");
        }
        this.fechaInicio=fechaInicio;
    }

    public LocalDateTime getFechaFin(){
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin){
        if(fechaFin == null){
            throw new IllegalArgumentException("fechaFin no puede ser nulo");
        }
        this.fechaFin=fechaFin;
    }

    public Sala getSala(){
        return sala;
    }

    public void setSala(Sala sala){
        this.sala=sala;
    }

    public Recurso getRecurso(){
        return recurso;
    }

    public void setRecurso(Recurso recurso){
        this.recurso=recurso;
    }

}
