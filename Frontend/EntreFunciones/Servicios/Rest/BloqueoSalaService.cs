using EntreFunciones.DTOSS;
using Microsoft.AspNetCore.Components.Authorization;
using System.Globalization;

namespace EntreFunciones.Servicios.Rest;

public class BloqueoSalaService : IBloqueoSalaService
{
    private readonly IConfiguration _config;
    private readonly AuthenticationStateProvider _auth;
    private readonly string _baseUrl;

    public BloqueoSalaService(IConfiguration config, AuthenticationStateProvider auth)
    {
        _config = config;
        _auth = auth;
        _baseUrl = config["Rest:BaseUrl"]
            ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
    }

    public async Task<List<BloqueoSalaDto>> Listar() =>
        await HttpUtils.DoGetList<BloqueoSalaDto>(Url("bloqueos-sala"), cabeceras: await Cabeceras());

    public async Task<BloqueoSalaDto> Insertar(BloqueoSalaDto bloqueo) =>
        await HttpUtils.DoPost<BloqueoSalaDto, BloqueoSalaDto>(
            Url("bloqueos-sala"), bloqueo, cabeceras: await Cabeceras());

    public async Task<BloqueoSalaDto> Actualizar(BloqueoSalaDto bloqueo) =>
        await HttpUtils.DoPut<BloqueoSalaDto, BloqueoSalaDto>(
            Url($"bloqueos-sala/{bloqueo.Id}"), bloqueo, cabeceras: await Cabeceras());

    public async Task Desactivar(int id) =>
        await HttpUtils.DoDelete(Url($"bloqueos-sala/{id}"), cabeceras: await Cabeceras());

    public async Task<DisponibilidadProgramacionDto> ConsultarDisponibilidad(
        int salaId, DateOnly fecha, TimeOnly inicio, TimeOnly fin, int? excluirFuncionId = null)
    {
        string url = HttpUtils.AgregarParametrosConsulta(
            Url("bloqueos-sala/disponibilidad"),
            new Dictionary<string, string?>
            {
                ["salaId"] = salaId.ToString(CultureInfo.InvariantCulture),
                ["fecha"] = fecha.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture),
                ["horaInicio"] = inicio.ToString("HH:mm", CultureInfo.InvariantCulture),
                ["horaFin"] = fin.ToString("HH:mm", CultureInfo.InvariantCulture),
                ["excluirFuncionId"] = excluirFuncionId?.ToString(CultureInfo.InvariantCulture)
            });
        return await HttpUtils.DoGet<DisponibilidadProgramacionDto>(url, cabeceras: await Cabeceras());
    }

    private string Url(string ruta) => HttpUtils.ConstruirUrl(_baseUrl, ruta);
    private async Task<IReadOnlyDictionary<string, string>> Cabeceras() =>
        await HttpUtils.CrearCabecerasAutenticadas(_config, _auth);
}
