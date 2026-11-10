using EntreFunciones.DTOSS.Usuarios;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class ClienteService : IClienteService
    {
        private readonly IConfiguration _config;
        private readonly AuthenticationStateProvider _auth;
        private readonly string _baseUrl;

        public ClienteService(IConfiguration config, AuthenticationStateProvider auth)
        {
            _config = config;
            _auth = auth;
            _baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
        }

        //sin sesion

        public async Task<ClienteDTO> Registrar(ClienteDTO cliente)
        {
            return await HttpUtils.DoPost<ClienteDTO, ClienteDTO>(
                _baseUrl + "clientes", cliente, cabeceras: CabecerasSinSesion());
        }

        // con sesion

        public async Task<List<ClienteDTO>> Listar()
        {
            return await HttpUtils.DoGetList<ClienteDTO>(
                _baseUrl + "clientes", cabeceras: await CabecerasConSesion());
        }

        public async Task<ClienteDTO> ObtenerPorId(int id)
        {
            return await HttpUtils.DoGet<ClienteDTO>(
                _baseUrl + $"clientes/{id}", cabeceras: await CabecerasConSesion());
        }

        public async Task<ClienteDTO> Actualizar(int id, ClienteDTO cliente)
        {
            return await HttpUtils.DoPut<ClienteDTO, ClienteDTO>(
                _baseUrl + $"clientes/{id}", cliente, cabeceras: await CabecerasConSesion());
        }

        public async Task CambiarContrasena(int id, string actual, string nueva)
        {
            CambiarPasswordDTO dto = new()
            {
                ContrasenaActual = actual,
                ContrasenaNueva = nueva
            };
            await HttpUtils.DoPut<CambiarPasswordDTO>(
                _baseUrl + $"clientes/{id}/password", dto, cabeceras: await CabecerasConSesion());
        }

        public async Task Desactivar(int id)
        {
            await HttpUtils.DoDelete(
                _baseUrl + $"clientes/{id}", cabeceras: await CabecerasConSesion());
        }

        public async Task Activar(int id)
        {
            await HttpUtils.DoPost(
                _baseUrl + $"clientes/{id}/activar", cabeceras: await CabecerasConSesion());
        }

        // ---------- Headers que exige AccesoRest.java ----------

        // Registro: aún no hay sesión, se usa la cuenta de servicio
        private Dictionary<string, string> CabecerasSinSesion()
        {
            return new Dictionary<string, string>
            {
                ["X-Api-Key"] = _config["Rest:ApiKey"]!,
                ["X-Usuario-Id"] = _config["Rest:CuentaServicioId"]!
            };
        }

        // Resto: el id del usuario que inició sesión (igual que CompraService)
        private async Task<IReadOnlyDictionary<string, string>> CabecerasConSesion()
        {
            return await HttpUtils.CrearCabecerasAutenticadas(_config, _auth);
        }
    }
}
