using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;

namespace EntreFunciones.Mappers
{
    public static class EventoMapper
    {
        public static EventoViewModel ToViewModel(EventoDTO dto) => new()
        {
            Id = dto.Id,
            IsActivo = dto.Activo,
            Titulo = dto.Titulo,
            Tipo = dto.Tipo,
            Precio = (decimal)dto.Precio,
            ImagenUrl = dto.ImagenUrl
        };

        public static string? ResolverImagenUrl(string? imagenUrl, string baseUri)
        {
            if (string.IsNullOrWhiteSpace(imagenUrl))
            {
                return null;
            }

            string valor = imagenUrl.Trim();
            if (Uri.TryCreate(valor, UriKind.Absolute, out Uri? absoluta))
            {
                return absoluta.Scheme == Uri.UriSchemeHttp
                    || absoluta.Scheme == Uri.UriSchemeHttps
                    ? valor
                    : null;
            }

            if (!Uri.TryCreate(baseUri, UriKind.Absolute, out Uri? baseAbsoluta))
            {
                return null;
            }

            string rutaRelativa = valor.Replace('\\', '/').TrimStart('/');
            return Uri.TryCreate(baseAbsoluta, rutaRelativa, out Uri? resuelta)
                ? resuelta.AbsoluteUri
                : null;
        }
    }
}
