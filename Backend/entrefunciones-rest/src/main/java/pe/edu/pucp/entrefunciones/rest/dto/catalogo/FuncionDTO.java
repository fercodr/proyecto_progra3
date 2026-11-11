package pe.edu.pucp.entrefunciones.rest.dto.catalogo;

import java.math.BigDecimal;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.SalaDTO;
import pe.edu.pucp.entrefunciones.rest.dto.espacios.SedeDTO;

public class FuncionDTO {
    private int id;
    private int idEvento;

    public EventoDTO getEvento() {
        return evento;
    }

    public void setEvento(EventoDTO evento) {
        this.evento = evento;
    }

    public SalaDTO getSala() {
        return sala;
    }

    public void setSala(SalaDTO sala) {
        this.sala = sala;
    }

    public SedeDTO getSede() {
        return sede;
    }

    public void setSede(SedeDTO sede) {
        this.sede = sede;
    }

    private EventoDTO evento;
    private SalaDTO sala;
    private SedeDTO sede;
    private String tituloEvento;
    private int idSala;
    private String nombreSala;
    private int idSede;
    private String nombreSede;
    private String ciudadSede;
    private String fecha;
    private String horaInicio;
    private String horaFin;
    private String idioma;
    private String estado;
    private Integer entradasNoCanceladas;
    private Integer entradasCanceladas;
    private BigDecimal ingresosValidos;
    private Integer asistentes;
    private BigDecimal porcentajeOcupacion;

    public FuncionDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    public String getTituloEvento() {
        return tituloEvento;
    }

    public void setTituloEvento(String tituloEvento) {
        this.tituloEvento = tituloEvento;
    }

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public String getNombreSala() {
        return nombreSala;
    }

    public void setNombreSala(String nombreSala) {
        this.nombreSala = nombreSala;
    }

    public int getIdSede() {
        return idSede;
    }

    public void setIdSede(int idSede) {
        this.idSede = idSede;
    }

    public String getNombreSede() {
        return nombreSede;
    }

    public void setNombreSede(String nombreSede) {
        this.nombreSede = nombreSede;
    }

    public String getCiudadSede() {
        return ciudadSede;
    }

    public void setCiudadSede(String ciudadSede) {
        this.ciudadSede = ciudadSede;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getEntradasNoCanceladas() {
        return entradasNoCanceladas;
    }

    public void setEntradasNoCanceladas(Integer entradasNoCanceladas) {
        this.entradasNoCanceladas = entradasNoCanceladas;
    }

    public Integer getEntradasCanceladas() {
        return entradasCanceladas;
    }

    public void setEntradasCanceladas(Integer entradasCanceladas) {
        this.entradasCanceladas = entradasCanceladas;
    }

    public BigDecimal getIngresosValidos() {
        return ingresosValidos;
    }

    public void setIngresosValidos(BigDecimal ingresosValidos) {
        this.ingresosValidos = ingresosValidos;
    }

    public Integer getAsistentes() {
        return asistentes;
    }

    public void setAsistentes(Integer asistentes) {
        this.asistentes = asistentes;
    }

    public BigDecimal getPorcentajeOcupacion() {
        return porcentajeOcupacion;
    }

    public void setPorcentajeOcupacion(BigDecimal porcentajeOcupacion) {
        this.porcentajeOcupacion = porcentajeOcupacion;
    }
}
