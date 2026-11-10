using EntreFunciones.DTOSS.Usuarios;

namespace EntreFunciones.Servicios.Rest
{
    public class CuentaService : ICuentaService
    {
        private readonly IConfiguration _config;
        private readonly string _baseUrl;

        public CuentaService(IConfiguration config)
        {
            _config = config;
            _baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
        }

        // Autenticar: 200 -> true
        // 401 -> false
        public async Task<bool> Autenticar(string correo, string contrasena)
        {
            CredencialesDTO credenciales = new()
            {
                Correo = correo,
                Contrasena = contrasena
            };

            try
            {
                await HttpUtils.DoPost<CredencialesDTO>(
                    _baseUrl + "cuentas/login", credenciales, cabeceras: CrearCabeceras());
                return true;
            }
            catch (ServiceException)
            {
                return false;
            }
        }

        // Buscar la cuenta por correo
        public async Task<CuentaUsuarioDTO> FindByCorreo(string correo)
        {
            return await HttpUtils.DoGet<CuentaUsuarioDTO>(
                _baseUrl + $"cuentas/{correo}", cabeceras: CrearCabeceras());
        }

        // Obtener el rol: se pregunta rol por rol; el que responda 200 es el rol
        public async Task<string?> ObtenerRol(string correo)
        {
            try
            {
                await HttpUtils.DoGet<AdministradorDTO>(
                    _baseUrl + $"cuentas/{correo}/administrador", cabeceras: CrearCabeceras());
                return "Administrador";
            }
            catch (ServiceException)
            {
             
            }

            try
            {
                await HttpUtils.DoGet<ClienteDTO>(
                    _baseUrl + $"cuentas/{correo}/cliente", cabeceras: CrearCabeceras());
                return "Cliente";
            }
            catch (ServiceException)
            {
                
            }

            return null;
        }
        private Dictionary<string, string> CrearCabeceras()
        {
            return new Dictionary<string, string>
            {
                ["X-Api-Key"] = _config["Rest:ApiKey"]!,
                ["X-Usuario-Id"] = _config["Rest:CuentaServicioId"]!
            };
        }
    }
}
