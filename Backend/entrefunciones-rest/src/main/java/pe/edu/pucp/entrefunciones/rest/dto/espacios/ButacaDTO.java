package pe.edu.pucp.entrefunciones.rest.dto.espacios;

public class ButacaDTO {
    private int id;
    private boolean activo;
    private int idSala;
    private String fila;
    private int columna;
    private String tipoButaca;

    public ButacaDTO() {
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

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    public String getTipoButaca() {
        return tipoButaca;
    }

    public void setTipoButaca(String tipoButaca) {
        this.tipoButaca = tipoButaca;
    }
}
