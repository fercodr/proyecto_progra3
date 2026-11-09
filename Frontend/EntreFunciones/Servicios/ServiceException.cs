namespace EntreFunciones.Servicios
{
    public class ServiceException : Exception
    {
        public ServiceException(string mensaje) : base(mensaje)
        {
        }

        public ServiceException(string mensaje, Exception causa) : base(mensaje, causa)
        {
        }
    }
}
