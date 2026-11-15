namespace EntreFunciones.Servicios.Rest
{
    public interface IImagenService
    {
        // POST /eventos/{eventoId}/imagen -> devuelve la URL pública en S3
        Task<string> SubirPosterEvento(int eventoId, byte[] contenido, string tipoContenido);
    }
}
