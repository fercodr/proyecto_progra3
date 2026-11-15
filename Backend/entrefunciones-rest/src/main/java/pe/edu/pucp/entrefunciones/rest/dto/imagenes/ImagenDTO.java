package pe.edu.pucp.entrefunciones.rest.dto.imagenes;

// Lo que envía C# al subir un póster: el tipo (image/jpeg...) y el archivo en base64
public class ImagenDTO {
    private String tipoContenido;
    private String contenidoBase64;

    public ImagenDTO() {
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public String getContenidoBase64() {
        return contenidoBase64;
    }

    public void setContenidoBase64(String contenidoBase64) {
        this.contenidoBase64 = contenidoBase64;
    }
}
