using EntreFunciones.DTOSS;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class CatalogoService : ICatalogoService
    {
        private readonly IConfiguration config;
        private readonly AuthenticationStateProvider auth;
        private readonly string baseUrl;

        public CatalogoService(
            IConfiguration config,
            AuthenticationStateProvider auth)
        {
            this.config = config;
            this.auth = auth;
            baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException(
                    "Falta configurar Rest:BaseUrl.");
        }

        public async Task<List<EventoDTO>> ListarEventos()
        {
            return await HttpUtils.DoGet<List<EventoDTO>>(
                CrearUrl("eventos"));
        }

        public async Task<EventoDTO> ObtenerEvento(
            int id,
            bool completo = false)
        {
            string ruta = $"eventos/{id}"
                + (completo ? "?completo=true" : string.Empty);
            return await HttpUtils.DoGet<EventoDTO>(CrearUrl(ruta));
        }

        public async Task<List<FuncionDTO>> ListarFunciones()
        {
            return await HttpUtils.DoGet<List<FuncionDTO>>(
                CrearUrl("funciones"));
        }

        public async Task<EventoDTO> CrearEvento(EventoDTO evento)
        {
            return await HttpUtils.DoPost<EventoDTO, EventoDTO>(
                CrearUrl("eventos"),
                evento,
                cabeceras: await CrearCabecerasAutenticadas());
        }

        public async Task<EventoDTO> ActualizarEvento(EventoDTO evento)
        {
            return await HttpUtils.DoPut<EventoDTO, EventoDTO>(
                CrearUrl($"eventos/{evento.Id}"),
                evento,
                cabeceras: await CrearCabecerasAutenticadas());
        }

        public async Task EliminarEvento(int id)
        {
            await HttpUtils.DoDelete(
                CrearUrl($"eventos/{id}"),
                cabeceras: await CrearCabecerasAutenticadas());
        }

        public async Task<FuncionDTO> ProgramarFuncion(FuncionDTO funcion)
        {
            return await HttpUtils.DoPost<FuncionDTO, FuncionDTO>(
                CrearUrl("funciones"),
                funcion,
                cabeceras: await CrearCabecerasAutenticadas());
        }

        public async Task<FuncionDTO> ActualizarFuncion(FuncionDTO funcion)
        {
            return await HttpUtils.DoPut<FuncionDTO, FuncionDTO>(
                CrearUrl($"funciones/{funcion.Id}"),
                funcion,
                cabeceras: await CrearCabecerasAutenticadas());
        }

        public async Task CancelarFuncion(int id)
        {
            await HttpUtils.DoDelete(
                CrearUrl($"funciones/{id}"),
                cabeceras: await CrearCabecerasAutenticadas());
        }

        private string CrearUrl(string ruta)
        {
            return $"{baseUrl.TrimEnd('/')}/{ruta.TrimStart('/')}";
        }

        private async Task<IReadOnlyDictionary<string, string>>
            CrearCabecerasAutenticadas()
        {
            return await HttpUtils.CrearCabecerasAutenticadas(config, auth);
        }
    }
}
