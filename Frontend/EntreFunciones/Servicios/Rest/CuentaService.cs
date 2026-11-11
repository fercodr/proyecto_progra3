using EntreFunciones.DTOSS.Usuarios;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class CuentaService : ICuentaService
    {
        private readonly IConfiguration _config;
        private readonly AuthenticationStateProvider _auth;
        private readonly string _baseUrl;

        public CuentaService(IConfiguration config, AuthenticationStateProvider auth)
        {
            _config = config;
            _auth = auth;
            _baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
        }

        public async Task<SesionDTO?> Autenticar(string correo, string contrasena)
        {
            CredencialesDTO credenciales = new() { Correo = correo, Contrasena = contrasena };
            try
            {
                return await HttpUtils.DoPost<CredencialesDTO, SesionDTO>(
                    _baseUrl + "cuentas/login", credenciales);
            }
            catch (ServiceException ex) when (ex.StatusCode == 401)
            {
                return null;
            }
        }

        public async Task<CuentaUsuarioDTO> FindByCorreo(string correo)
        {
            return await HttpUtils.DoGet<CuentaUsuarioDTO>(
                _baseUrl + $"cuentas/{Uri.EscapeDataString(correo)}",
                cabeceras: await CrearCabeceras());
        }

        public async Task<string?> ObtenerRol(string correo)
        {
            try
            {
                await HttpUtils.DoGet<AdministradorDTO>(
                    _baseUrl + $"cuentas/{Uri.EscapeDataString(correo)}/administrador",
                    cabeceras: await CrearCabeceras());
                return "Administrador";
            }
            catch (ServiceException) { }

            try
            {
                await HttpUtils.DoGet<ClienteDTO>(
                    _baseUrl + $"cuentas/{Uri.EscapeDataString(correo)}/cliente",
                    cabeceras: await CrearCabeceras());
                return "Cliente";
            }
            catch (ServiceException) { }

            return null;
        }

        public async Task Recuperar(string correo, string dni, string contrasenaNueva)
        {
            RecuperarContrasenaDTO dto = new()
            {
                Correo = correo, Dni = dni, ContrasenaNueva = contrasenaNueva
            };
            await HttpUtils.DoPost<RecuperarContrasenaDTO>(
                _baseUrl + "cuentas/recuperar", dto);
        }

        private async Task<IReadOnlyDictionary<string, string>> CrearCabeceras()
        {
            return await HttpUtils.CrearCabecerasAutenticadas(_config, _auth);
        }
    }
}
