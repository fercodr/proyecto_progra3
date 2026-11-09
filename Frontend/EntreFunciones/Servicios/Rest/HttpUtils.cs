using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;

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

            HttpResponseMessage respuesta = await _http.SendAsync(peticion);

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

            TResponse? resultado = await respuesta.Content.ReadFromJsonAsync<TResponse>();

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

        private sealed class ErrorRespuestaDto
        {
            public string Mensaje { get; set; } = string.Empty;
        }
    }
}
