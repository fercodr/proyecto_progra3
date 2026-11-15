namespace EntreFunciones.DTOSS.Imagenes
{
    // Gemelo de ImagenDTO.java: lo que se envía a POST /eventos/{id}/imagen
    public class ImagenDTO
    {
        public string TipoContenido { get; set; } = string.Empty;
        public string ContenidoBase64 { get; set; } = string.Empty;
    }
}
