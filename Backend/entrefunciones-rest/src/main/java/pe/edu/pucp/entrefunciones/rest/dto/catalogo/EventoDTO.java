package pe.edu.pucp.entrefunciones.rest.dto.catalogo;

import java.util.List;

public class EventoDTO {
    private int id;
    private boolean activo;
    private String tipo;
    private double precio;
    private String titulo;
    private String imagenUrl;
    private boolean requiereAsientoNumerado;

    // Pelicula
    private Integer duracionMinutos;
    private String genero;
    private String clasificacionPorEdad;
    private String director;

    // ObraTeatro
    private String nombreDirector;
    private String elenco;

    // ActividadCultural
    private String expositor;
    private String tipoActividad;

    private List<FuncionDTO> funciones;

    public EventoDTO() {
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean isRequiereAsientoNumerado() {
        return requiereAsientoNumerado;
    }

    public void setRequiereAsientoNumerado(boolean requiereAsientoNumerado) {
        this.requiereAsientoNumerado = requiereAsientoNumerado;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getClasificacionPorEdad() {
        return clasificacionPorEdad;
    }

    public void setClasificacionPorEdad(String clasificacionPorEdad) {
        this.clasificacionPorEdad = clasificacionPorEdad;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getNombreDirector() {
        return nombreDirector;
    }

    public void setNombreDirector(String nombreDirector) {
        this.nombreDirector = nombreDirector;
    }

    public String getElenco() {
        return elenco;
    }

    public void setElenco(String elenco) {
        this.elenco = elenco;
    }

    public String getExpositor() {
        return expositor;
    }

    public void setExpositor(String expositor) {
        this.expositor = expositor;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    public List<FuncionDTO> getFunciones() {
        return funciones;
    }

    public void setFunciones(List<FuncionDTO> funciones) {
        this.funciones = funciones;
    }
}
