using EntreFunciones.DTOSS.Usuarios;

namespace EntreFunciones.Servicios.Rest
{
    public class ClienteService : IClienteService
    {
        private readonly IConfiguration _config;
        private readonly string _baseUrl;

        public ClienteService(IConfiguration config)
        {
            _config = config;
            _baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
        }

        public async Task<ClienteDTO> Registrar(ClienteDTO cliente)
        {
            return await HttpUtils.DoPost<ClienteDTO, ClienteDTO>(
                _baseUrl + "clientes", cliente, cabeceras: CrearCabeceras());
        }

        // Headers que exige AccesoRest.java. Quien se registra aún no tiene sesión,
        // así que se usa la cuenta de servicio (igual que en CuentaService).
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