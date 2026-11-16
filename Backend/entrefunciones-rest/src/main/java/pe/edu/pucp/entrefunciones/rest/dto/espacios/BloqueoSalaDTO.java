package pe.edu.pucp.entrefunciones.rest.dto.espacios;

import java.time.LocalDateTime;

public class BloqueoSalaDTO {
    private int id;
    private int salaId;
    private String salaNombre;
    private String sedeNombre;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private boolean activo;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSalaId() { return salaId; }
    public void setSalaId(int salaId) { this.salaId = salaId; }
    public String getSalaNombre() { return salaNombre; }
    public void setSalaNombre(String salaNombre) { this.salaNombre = salaNombre; }
    public String getSedeNombre() { return sedeNombre; }
    public void setSedeNombre(String sedeNombre) { this.sedeNombre = sedeNombre; }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFin() { return fin; }
    public void setFin(LocalDateTime fin) { this.fin = fin; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
