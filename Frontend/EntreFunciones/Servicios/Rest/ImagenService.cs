using EntreFunciones.DTOSS.Imagenes;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class ImagenService : IImagenService
    {
        private readonly IConfiguration _config;
        private readonly AuthenticationStateProvider _auth;
        private readonly string _baseUrl;

        public ImagenService(IConfiguration config, AuthenticationStateProvider auth)
        {
            _config = config;
            _auth = auth;
            _baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
        }

        public async Task<string> SubirPosterEvento(int eventoId, byte[] contenido, string tipoContenido)
        {
            // La imagen viaja dentro del JSON como texto base64
            ImagenDTO dto = new()
            {
                TipoContenido = tipoContenido,
                ContenidoBase64 = Convert.ToBase64String(contenido)
            };

            ImagenSubidaDTO respuesta = await HttpUtils.DoPost<ImagenDTO, ImagenSubidaDTO>(
                _baseUrl + $"eventos/{eventoId}/imagen", dto,
                cabeceras: await HttpUtils.CrearCabecerasAutenticadas(_config, _auth));

            return respuesta.ImagenUrl;
        }
    }
}
