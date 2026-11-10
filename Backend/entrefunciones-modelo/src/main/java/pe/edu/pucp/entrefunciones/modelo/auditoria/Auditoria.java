package pe.edu.pucp.entrefunciones.modelo.auditoria;

import java.time.LocalDateTime;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

public class Auditoria {
    private int id;
    private Funcion funcion;
    private LocalDateTime fecha;
    private Administrador responsable;
    private String descripcion;
    private EstadoAuditoria estado;

    public Auditoria(int id, LocalDateTime fecha, Administrador responsable,
                     String descripcion, Funcion funcion) {
        setId(id);
        setFecha(fecha);
        setResponsable(responsable);
        setDescripcion(descripcion);
        setFuncion(funcion);
        this.estado = EstadoAuditoria.PENDIENTE;
    }

    public Auditoria(int id, LocalDateTime fecha, Administrador responsable,
                     String descripcion) {
        this(id, fecha, responsable, descripcion, null);
    }

    public Auditoria() {}

    public Auditoria(final Auditoria auditoria) {
        if (auditoria == null) {
            throw new IllegalArgumentException("auditoria no puede ser nulo");
        }
        setId(auditoria.getId());
        setFecha(auditoria.getFecha());
        this.responsable = auditoria.getResponsable();
        setDescripcion(auditoria.getDescripcion());
        setFuncion(auditoria.getFuncion());
        setEstado(auditoria.getEstado());
    }

    public boolean atender(){
        if(estado == EstadoAuditoria.PENDIENTE){
            estado = EstadoAuditoria.EN_PROCESO;
            return true;
        }
        return false;
    }

    public boolean resolver(){
        if(estado != EstadoAuditoria.RESUELTA){
            estado = EstadoAuditoria.RESUELTA;
            return true;
        }
        return false;
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

    public Funcion getFuncion(){
        return funcion;
    }

    public void setFuncion(Funcion funcion){
        this.funcion=funcion;
    }

    public LocalDateTime getFecha(){
        return fecha;
    }

    public void setFecha(LocalDateTime fecha){
        if(fecha == null){
            throw new IllegalArgumentException("fecha no puede ser nulo");
        }
        this.fecha=fecha;
    }

    public Administrador getResponsable(){
        return responsable;
    }

    public void setResponsable(Administrador responsable) {
        if (responsable == null) {
            throw new IllegalArgumentException("responsable no puede ser nulo");
        }
        this.responsable = responsable;
    }


    public String getDescripcion(){
        return descripcion;
    }

    public void setDescripcion(String descripcion){
        if(descripcion == null || descripcion.isBlank()){
            throw new IllegalArgumentException("descripcion no puede ser nulo o vacio");
        }
        this.descripcion=descripcion.trim();
    }

    public EstadoAuditoria getEstado(){
        return estado;
    }

    public void setEstado(EstadoAuditoria estado){
        if(estado == null){
            throw new IllegalArgumentException("estado no puede ser nulo");
        }
        this.estado=estado;
    }

}
