namespace EntreFunciones.Servicios
{
    public class ServiceException : Exception
    {
        public int? StatusCode { get; }

        public ServiceException(string mensaje) : base(mensaje)
        {
        }

        public ServiceException(string mensaje, int statusCode)
            : base(mensaje)
        {
            StatusCode = statusCode;
        }

        public ServiceException(string mensaje, Exception causa) : base(mensaje, causa)
        {
        }
    }
}
