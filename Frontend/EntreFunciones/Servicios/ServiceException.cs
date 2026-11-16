namespace EntreFunciones.Servicios
{
    public class ServiceException : Exception
    {
        public int? StatusCode { get; }
        public string? Endpoint { get; }
        public string? ResponseContentType { get; }

        public ServiceException(string mensaje) : base(mensaje)
        {
        }

        public ServiceException(string mensaje, int statusCode)
            : base(mensaje)
        {
            StatusCode = statusCode;
        }

        public ServiceException(
            string mensaje,
            int statusCode,
            string? endpoint,
            string? responseContentType)
            : base(mensaje)
        {
            StatusCode = statusCode;
            Endpoint = endpoint;
            ResponseContentType = responseContentType;
        }

        public ServiceException(string mensaje, Exception causa) : base(mensaje, causa)
        {
        }

        public ServiceException(
            string mensaje,
            Exception causa,
            string? endpoint)
            : base(mensaje, causa)
        {
            Endpoint = endpoint;
        }
    }
}
