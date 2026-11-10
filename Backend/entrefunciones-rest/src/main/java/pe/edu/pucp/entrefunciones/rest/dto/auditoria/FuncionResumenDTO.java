package pe.edu.pucp.entrefunciones.rest.dto.auditoria;

import java.time.LocalDate;
import java.time.LocalTime;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

public class FuncionResumenDTO {

    private int id;
    private String titulo;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private String sede;
    private String sala;
    private String estado;

    public FuncionResumenDTO() {
    }

    public FuncionResumenDTO(Funcion funcion) {
        this.id = funcion.getId();
        this.titulo = funcion.getEvento().getTitulo();
        this.fecha = funcion.getFecha();
        this.horaInicio = funcion.getHoraInicio();
        this.sede = funcion.getSala().getSede().getRazonSocial();
        this.sala = funcion.getSala().getNombre();
        this.estado = funcion.getEstado().name();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
