using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Security.Claims;
using System.Text.Json;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public static class HttpUtils
    {
        private static readonly HttpClient _http = new()
        {
            Timeout = TimeSpan.FromSeconds(20)
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
                peticion.Content = JsonContent.Create(datos);
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
            try
            {
                respuesta = await _http.SendAsync(peticion);
            }
            catch (TaskCanceledException ex)
            {
                throw new ServiceException(
                    "El servicio REST no respondio dentro del tiempo esperado.",
                    ex);
            }
            catch (HttpRequestException ex)
            {
                throw new ServiceException(
                    "No se pudo establecer comunicacion con el servicio REST.",
                    ex);
            }

            if (!respuesta.IsSuccessStatusCode)
            {
                int codigo = (int)respuesta.StatusCode;
                string mensaje = await LeerMensajeError(respuesta, codigo);
                respuesta.Dispose();
                throw new ServiceException(mensaje);
            }

            return respuesta;
        }

        private static async Task<string> LeerMensajeError(HttpResponseMessage respuesta, int codigo)
        {
            string mensajePredeterminado = $"No se pudo completar la operación ({codigo}).";

            try
            {
                ErrorRespuestaDto? error = await respuesta.Content.ReadFromJsonAsync<ErrorRespuestaDto>();
                return string.IsNullOrWhiteSpace(error?.Mensaje)
                    ? mensajePredeterminado
                    : error.Mensaje;
            }
            catch (JsonException)
            {
                return mensajePredeterminado;
            }
            catch (NotSupportedException)
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
                    ex);
            }
            catch (NotSupportedException ex)
            {
                throw new ServiceException(
                    "El formato devuelto por el servicio REST no es compatible.",
                    ex);
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
            string? idUsuario = usuario
                .FindFirst(ClaimTypes.NameIdentifier)?.Value;

            if (usuario.Identity?.IsAuthenticated != true
                || !int.TryParse(idUsuario, out int cuentaId)
                || cuentaId <= 0)
            {
                throw new InvalidOperationException(
                    "Inicia sesion para continuar.");
            }

            string? apiKey = config["Rest:ApiKey"];
            if (string.IsNullOrWhiteSpace(apiKey))
            {
                throw new InvalidOperationException(
                    "Falta configurar el acceso al servicio REST.");
            }

            return new Dictionary<string, string>
            {
                ["X-Api-Key"] = apiKey,
                ["X-Usuario-Id"] = cuentaId.ToString()
            };
        }

        private sealed class ErrorRespuestaDto
        {
            [JsonPropertyName("error")]
            public string Mensaje { get; set; } = string.Empty;
        }
    }
}
