using EntreFunciones.DTOSS.Usuarios;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class GestionAdministradorService : IGestionAdministradorService
    {
        private readonly IConfiguration _config;
        private readonly AuthenticationStateProvider _auth;
        private readonly string _baseUrl;

        public GestionAdministradorService(IConfiguration config, AuthenticationStateProvider auth)
        {
            _config = config;
            _auth = auth;
            _baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
        }

        public async Task<List<AdministradorDTO>> Listar()
        {
            return await HttpUtils.DoGetList<AdministradorDTO>(
                _baseUrl + "administradores", cabeceras: await CabecerasConSesion());
        }

        public async Task<AdministradorDTO> Registrar(AdministradorDTO administrador)
        {
            return await HttpUtils.DoPost<AdministradorDTO, AdministradorDTO>(
                _baseUrl + "administradores", administrador, cabeceras: await CabecerasConSesion());
        }

        public async Task Desactivar(int id)
        {
            await HttpUtils.DoDelete(
                _baseUrl + $"administradores/{id}", cabeceras: await CabecerasConSesion());
        }

        public async Task Activar(int id)
        {
            await HttpUtils.DoPost(
                _baseUrl + $"administradores/{id}/activar", cabeceras: await CabecerasConSesion());
        }

        // El id del administrador que inició sesión
        private async Task<IReadOnlyDictionary<string, string>> CabecerasConSesion()
        {
            return await HttpUtils.CrearCabecerasAutenticadas(_config, _auth);
        }
    }
}
