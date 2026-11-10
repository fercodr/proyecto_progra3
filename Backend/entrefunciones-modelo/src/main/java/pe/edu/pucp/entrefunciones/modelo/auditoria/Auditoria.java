package pe.edu.pucp.entrefunciones.modelo.auditoria;

import java.time.LocalDateTime;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

public class Auditoria {
    private int id;
    private Funcion funcion;
    private LocalDateTime fecha;
    private Administrador responsable;
    private String responsableTextoOriginal;
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

    /** RNF-SEG-01: registro de trazabilidad general, sin una funcion asociada. */
    public Auditoria(int id, LocalDateTime fecha, Administrador responsable,
                     String descripcion) {
        this(id, fecha, responsable, descripcion, null);
    }

    /**
     * Restaura una fila historica cuyo responsable aun no pudo relacionarse
     * de forma segura. Las altas nuevas deben usar los constructores normales.
     */
    public static Auditoria restaurarHistorica(
            int id, LocalDateTime fecha, Administrador responsable,
            String responsableTextoOriginal, String descripcion,
            Funcion funcion, EstadoAuditoria estado) {

        if (responsable == null
                && (responsableTextoOriginal == null
                || responsableTextoOriginal.isBlank())) {
            throw new IllegalArgumentException(
                    "La auditoria historica debe conservar un responsable");
        }

        Auditoria auditoria = new Auditoria();
        auditoria.setId(id);
        auditoria.setFecha(fecha);
        auditoria.responsable = responsable;
        auditoria.responsableTextoOriginal = responsableTextoOriginal;
        auditoria.setDescripcion(descripcion);
        auditoria.setFuncion(funcion);
        auditoria.setEstado(estado);
        return auditoria;
    }

    private Auditoria() {
    }

    /** Constructor de copia. */
    public Auditoria(final Auditoria auditoria) {
        if (auditoria == null) {
            throw new IllegalArgumentException("auditoria no puede ser nulo");
        }
        setId(auditoria.getId());
        setFecha(auditoria.getFecha());
        this.responsable = auditoria.getResponsable();
        this.responsableTextoOriginal = auditoria.getResponsableTextoOriginal();
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

    /** Solo se informa para filas historicas aun no vinculadas. */
    public String getResponsableTextoOriginal() {
        return responsableTextoOriginal;
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
