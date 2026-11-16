package pe.edu.pucp.entrefunciones.rest.dto.espacios;

import java.util.List;

public class SalaDTO {
    private int id;
    private boolean activo;
    private int idSede;
    private SedeDTO sede;
    private String nombre;
    private int aforo;
    private String tipoSala;
    private boolean accesible;
    // Solo al crear una sala con su mapa de butacas (InsertConButacas en C#)
    private List<ButacaDTO> butacas;

    public SalaDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getIdSede() {
        return idSede;
    }

    public void setIdSede(int idSede) {
        this.idSede = idSede;
    }

    public SedeDTO getSede() {
        return sede;
    }

    public void setSede(SedeDTO sede) {
        this.sede = sede;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getAforo() {
        return aforo;
    }

    public void setAforo(int aforo) {
        this.aforo = aforo;
    }

    public String getTipoSala() {
        return tipoSala;
    }

    public void setTipoSala(String tipoSala) {
        this.tipoSala = tipoSala;
    }

    public boolean isAccesible() { return accesible; }
    public void setAccesible(boolean accesible) { this.accesible = accesible; }

    public List<ButacaDTO> getButacas() {
        return butacas;
    }

    public void setButacas(List<ButacaDTO> butacas) {
        this.butacas = butacas;
    }
}
