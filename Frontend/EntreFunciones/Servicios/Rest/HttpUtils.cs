using Microsoft.AspNetCore.Components.Authorization;
using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Security.Claims;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace EntreFunciones.Servicios.Rest
{
    public static class HttpUtils
    {
        private static readonly HttpClient _http = new()
        {
            // La RDS compartida esta lejos (us-east-1) y sin pool de conexiones del lado
            // del backend una query puede tardar unos segundos; 20s hacia cortaba de
            // mas esos picos y se sentia como que "se cae" cuando solo iba lento.
            Timeout = TimeSpan.FromSeconds(40)
        };

        // Fuerza DateTime a serializarse sin offset de zona horaria: el backend
        // espera java.time.LocalDateTime, que no acepta offset (ver
        // LocalDateTimeJsonConverter).
        // JsonSerializerDefaults.Web agrega camelCase + comparacion
        // case-insensitive, que es lo que Jakarta JSON-B espera del lado del
        // backend (java, responsable, descripcion, etc. en minuscula). Sin
        // esto, JsonContent.Create(datos, options: _opcionesJson) serializa
        // las propiedades tal cual estan declaradas en C# (PascalCase) y el
        // backend las ignora silenciosamente (quedan en null), de ahi
        // errores como "La funcion es obligatoria" o "El responsable es
        // obligatoria" al editar aunque el campo si se haya seleccionado.
        private static readonly JsonSerializerOptions _opcionesJson =
            new(JsonSerializerDefaults.Web)
            {
                Converters = { new LocalDateTimeJsonConverter() }
            };

        private static async Task<HttpResponseMessage> Enviar(
            HttpMethod metodo,
            string url,
            object? datos = null,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            using HttpRequestMessage peticion = new(metodo, url);

            if (datos is not null && (metodo == HttpMethod.Post || metodo == HttpMethod.Put))
            {
                peticion.Content = JsonContent.Create(
                    datos, inputType: datos.GetType(), options: _opcionesJson);
            }

            if (!string.IsNullOrWhiteSpace(token))
            {
                peticion.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            }

            if (cabeceras is not null)
            {
                foreach (KeyValuePair<string, string> cabecera in cabeceras)
                {
                    peticion.Headers.TryAddWithoutValidation(cabecera.Key, cabecera.Value);
                }
            }

            HttpResponseMessage respuesta;
            string? endpoint = ObtenerEndpointSeguro(peticion.RequestUri);
            try
            {
                respuesta = await _http.SendAsync(peticion);
            }
            catch (TaskCanceledException ex)
            {
                if (ex.CancellationToken.IsCancellationRequested)
                {
                    throw;
                }

                throw new ServiceException(
                    "El servicio REST no respondio dentro del tiempo esperado.",
                    ex,
                    endpoint);
            }
            catch (HttpRequestException ex)
            {
                throw new ServiceException(
                    "No se pudo establecer comunicacion con el servicio REST.",
                    ex,
                    endpoint);
            }

            if (!respuesta.IsSuccessStatusCode)
            {
                int codigo = (int)respuesta.StatusCode;
                string? tipoContenido = respuesta.Content.Headers.ContentType?.ToString();
                string mensaje = await LeerMensajeError(respuesta, codigo);
                respuesta.Dispose();
                throw new ServiceException(
                    mensaje,
                    codigo,
                    endpoint,
                    tipoContenido);
            }

            return respuesta;
        }

        private static string? ObtenerEndpointSeguro(Uri? uri)
        {
            if (uri is null)
            {
                return null;
            }

            if (uri.IsAbsoluteUri)
            {
                return uri.GetLeftPart(UriPartial.Path);
            }

            return uri.OriginalString.Split('?', '#')[0];
        }

        private static async Task<string> LeerMensajeError(HttpResponseMessage respuesta, int codigo)
        {
            string mensajePredeterminado = $"No se pudo completar la operación ({codigo}).";

            try
            {
                ErrorRespuestaDto? error = await respuesta.Content.ReadFromJsonAsync<ErrorRespuestaDto>();
                return string.IsNullOrWhiteSpace(error?.Texto)
                    ? mensajePredeterminado
                    : error.Texto;
            }
            catch (JsonException)
            {
                return mensajePredeterminado;
            }
            catch (NotSupportedException)
            {
                return mensajePredeterminado;
            }
            catch (HttpRequestException)
            {
                return mensajePredeterminado;
            }
            catch (TaskCanceledException)
            {
                return mensajePredeterminado;
            }
        }

        private static async Task<TResponse> Enviar<TResponse>(
            HttpMethod metodo,
            string url,
            object? datos = null,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            using HttpResponseMessage respuesta = await Enviar(metodo, url, datos, token, cabeceras);

            if (respuesta.StatusCode == HttpStatusCode.NoContent)
            {
                throw new ServiceException("Se esperaba contenido, pero el servidor devolvió 204.");
            }

            TResponse? resultado;
            try
            {
                resultado = await respuesta.Content.ReadFromJsonAsync<TResponse>();
            }
            catch (JsonException ex)
            {
                throw new ServiceException(
                    "El servicio REST devolvio una respuesta incompatible.",
                    ex,
                    ObtenerEndpointSeguro(respuesta.RequestMessage?.RequestUri));
            }
            catch (NotSupportedException ex)
            {
                throw new ServiceException(
                    "El formato devuelto por el servicio REST no es compatible.",
                    ex,
                    ObtenerEndpointSeguro(respuesta.RequestMessage?.RequestUri));
            }
            catch (HttpRequestException ex)
            {
                throw new ServiceException(
                    "No se pudo leer la respuesta del servicio REST.",
                    ex,
                    ObtenerEndpointSeguro(respuesta.RequestMessage?.RequestUri));
            }
            catch (TaskCanceledException ex)
            {
                throw new ServiceException(
                    "La lectura de la respuesta REST excedio el tiempo esperado.",
                    ex,
                    ObtenerEndpointSeguro(respuesta.RequestMessage?.RequestUri));
            }

            if (resultado is null)
            {
                throw new ServiceException("El servidor devolvió una respuesta nula.");
            }

            return resultado;
        }

        public static async Task<TResponse> DoGet<TResponse>(
            string url,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            return await Enviar<TResponse>(HttpMethod.Get, url, token: token, cabeceras: cabeceras);
        }

        public static async Task<List<TResponse>> DoGetList<TResponse>(
            string url,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            return await DoGet<List<TResponse>>(
                url,
                token,
                cabeceras);
        }

        public static async Task<byte[]> DoGetBytes(
            string url,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            using HttpResponseMessage respuesta = await Enviar(
                HttpMethod.Get,
                url,
                token: token,
                cabeceras: cabeceras);
            try
            {
                return await respuesta.Content.ReadAsByteArrayAsync();
            }
            catch (HttpRequestException ex)
            {
                throw new ServiceException(
                    "No se pudo leer el documento devuelto por el servicio REST.",
                    ex,
                    ObtenerEndpointSeguro(respuesta.RequestMessage?.RequestUri));
            }
            catch (TaskCanceledException ex)
            {
                throw new ServiceException(
                    "La lectura del documento excedio el tiempo esperado.",
                    ex,
                    ObtenerEndpointSeguro(respuesta.RequestMessage?.RequestUri));
            }
        }

        public static async Task<TResponse> DoPost<TRequest, TResponse>(
            string url,
            TRequest payload,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            return await Enviar<TResponse>(HttpMethod.Post, url, payload, token, cabeceras);
        }

        public static async Task<TResponse> DoPost<TResponse>(
            string url,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            return await Enviar<TResponse>(
                HttpMethod.Post,
                url,
                token: token,
                cabeceras: cabeceras);
        }

        public static async Task DoPost<TRequest>(
            string url,
            TRequest payload,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            using HttpResponseMessage respuesta = await Enviar(
                HttpMethod.Post,
                url,
                payload,
                token,
                cabeceras);
        }

        public static async Task DoPost(
            string url,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            using HttpResponseMessage respuesta = await Enviar(
                HttpMethod.Post,
                url,
                token: token,
                cabeceras: cabeceras);
        }

        public static async Task<TResponse> DoPut<TRequest, TResponse>(
            string url,
            TRequest payload,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            return await Enviar<TResponse>(HttpMethod.Put, url, payload, token, cabeceras);
        }

        public static async Task DoPut<TRequest>(
            string url,
            TRequest payload,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            using HttpResponseMessage respuesta = await Enviar(
                HttpMethod.Put,
                url,
                payload,
                token,
                cabeceras);
        }

        public static async Task DoDelete(
            string url,
            string? token = null,
            IReadOnlyDictionary<string, string>? cabeceras = null)
        {
            using HttpResponseMessage respuesta = await Enviar(
                HttpMethod.Delete,
                url,
                token: token,
                cabeceras: cabeceras);
        }

        public static string ConstruirUrl(
            string baseUrl,
            params string[] segmentos)
        {
            if (string.IsNullOrWhiteSpace(baseUrl))
            {
                throw new ArgumentException(
                    "La URL base no puede estar vacia.",
                    nameof(baseUrl));
            }

            string url = baseUrl.TrimEnd('/');
            foreach (string segmento in segmentos)
            {
                if (!string.IsNullOrWhiteSpace(segmento))
                {
                    url = $"{url}/{segmento.Trim('/')}";
                }
            }

            return url;
        }

        public static string AgregarParametrosConsulta(
            string url,
            IEnumerable<KeyValuePair<string, string?>> parametros)
        {
            List<string> valores = new();
            foreach (KeyValuePair<string, string?> parametro in parametros)
            {
                if (!string.IsNullOrWhiteSpace(parametro.Value))
                {
                    valores.Add(
                        $"{Uri.EscapeDataString(parametro.Key)}="
                        + Uri.EscapeDataString(parametro.Value));
                }
            }

            return valores.Count == 0
                ? url
                : $"{url}?{string.Join("&", valores)}";
        }

        public static async Task<IReadOnlyDictionary<string, string>>
            CrearCabecerasAutenticadas(
            IConfiguration config,
            AuthenticationStateProvider auth)
        {
            AuthenticationState estadoAutenticacion =
                await auth.GetAuthenticationStateAsync();
            ClaimsPrincipal usuario = estadoAutenticacion.User;
            string? token = usuario.FindFirst("access_token")?.Value;
            string? venceTexto = usuario.FindFirst("access_token_expires")?.Value;

            if (usuario.Identity?.IsAuthenticated != true
                || string.IsNullOrWhiteSpace(token))
            {
                throw new InvalidOperationException(
                    "Inicia sesion para continuar.");
            }

            if (long.TryParse(venceTexto, out long vence)
                && DateTimeOffset.UtcNow.ToUnixTimeSeconds() >= vence)
            {
                throw new ServiceException(
                    "La sesion REST expiro. Inicia sesion nuevamente.",
                    401);
            }

            return new Dictionary<string, string>
            {
                ["Authorization"] = $"Bearer {token}"
            };
        }

        private sealed class ErrorRespuestaDto
        {
            [JsonPropertyName("error")]
            public string? Error { get; set; }

            [JsonPropertyName("mensaje")]
            public string? Mensaje { get; set; }

            [JsonPropertyName("texto")]
            public string? TextoRespuesta { get; set; }
            [JsonIgnore]
            public string Texto => !string.IsNullOrWhiteSpace(Mensaje)
                ? Mensaje
                : !string.IsNullOrWhiteSpace(TextoRespuesta)
                    ? TextoRespuesta
                    : Error ?? string.Empty;
        }
    }
}
