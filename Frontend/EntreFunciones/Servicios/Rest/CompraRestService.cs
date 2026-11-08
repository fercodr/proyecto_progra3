using System.Net.Http.Json;
using System.Text.Json;
using System.Security.Claims;
using Microsoft.AspNetCore.Components.Authorization;
namespace EntreFunciones.Servicios.Rest;
public class CompraRestService(HttpClient http, IConfiguration config, AuthenticationStateProvider auth)
{
    private async Task<T> Enviar<T>(HttpMethod metodo,string ruta,object? datos=null)
    {
        var usuario=(await auth.GetAuthenticationStateAsync()).User;
        var id=usuario.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if(usuario.Identity?.IsAuthenticated!=true || !int.TryParse(id,out int cuenta) || cuenta<=0)
            throw new InvalidOperationException("Inicia sesión para continuar.");
        string? key=config["Rest:ApiKey"];
        if(string.IsNullOrWhiteSpace(key))throw new InvalidOperationException("Falta configurar el acceso al servicio REST.");
        using var peticion=new HttpRequestMessage(metodo,ruta);
        peticion.Headers.Add("X-Api-Key",key);
        peticion.Headers.Add("X-Usuario-Id",id);
        if(metodo==HttpMethod.Post)peticion.Content=JsonContent.Create(datos ?? new {});
        using var respuesta=await http.SendAsync(peticion);
       
        
        if(!respuesta.IsSuccessStatusCode)
        {
            string mensaje=$"No se pudo completar la operación ({(int)respuesta.StatusCode}).";
            try {var error=await respuesta.Content.ReadFromJsonAsync<ErrorDto>();if(!string.IsNullOrWhiteSpace(error?.Mensaje))mensaje=error.Mensaje;} catch(JsonException) { }
            throw new InvalidOperationException(mensaje);
        }
        
        
        if(respuesta.StatusCode==System.Net.HttpStatusCode.NoContent)return default!;
        return (await respuesta.Content.ReadFromJsonAsync<T>())!;
    }
    private class ErrorDto { public string Mensaje {get;set;}=""; }
    public Task<DisponibilidadDto> Disponibilidad(int id)=>Enviar<DisponibilidadDto>(HttpMethod.Get,$"funciones/{id}/disponibilidad");
    public Task<ReservaDto> Crear(int funcion,IEnumerable<int> butacas,int cantidad)=>Enviar<ReservaDto>(HttpMethod.Post,"reservas",new {funcionId=funcion,butacaIds=butacas,cantidad});
    public Task<ReservaDto> Obtener(int id)=>Enviar<ReservaDto>(HttpMethod.Get,$"reservas/{id}");
    public Task<PagoDto> GenerarPago(int id)=>Enviar<PagoDto>(HttpMethod.Post,$"reservas/{id}/pago");
    public async Task RegistrarPago(int id)=>await Enviar<object>(HttpMethod.Post,$"pagos/{id}/registrar");
    public async Task Cancelar(int id)=>await Enviar<object>(HttpMethod.Delete,$"reservas/{id}");
    public Task<List<ReservaDto>> ListarReservas()=>Enviar<List<ReservaDto>>(HttpMethod.Get,"reservas");
    public Task<List<PagoDto>> ListarPagos()=>Enviar<List<PagoDto>>(HttpMethod.Get,"pagos");
    public async Task Validar(int id)=>await Enviar<JsonElement>(HttpMethod.Post,$"pagos/{id}/validar");
    public async Task Rechazar(int id)=>await Enviar<object>(HttpMethod.Post,$"pagos/{id}/rechazar");
}
public class DisponibilidadDto
{
    public int Id {get;set;} public string Titulo {get;set;}=""; public string Sede {get;set;}="";
    public string Sala {get;set;}=""; public DateTime Fecha {get;set;} public string HoraInicio {get;set;}="";
    public decimal Precio {get;set;} public bool AforoNumerado {get;set;} public int CuposDisponibles {get;set;}
    public List<ButacaDto> Butacas {get;set;}=[];
}
public class ButacaDto
{
    public int Id {get;set;} public string Fila {get;set;}=""; public int Columna {get;set;}
    public bool Activo {get;set;} public bool Ocupada {get;set;} public bool Elegida {get;set;}
}
public class ReservaDto
{
    public int Id {get;set;} public string CodigoOId {get;set;}=""; public int FuncionId {get;set;}
    public string FuncionNombre {get;set;}=""; public DateTime FechaFuncion {get;set;}
    public string Cliente {get;set;}=""; public string Dni {get;set;}=""; public int Cantidad {get;set;}
    public DateTime FechaCreacion {get;set;} public DateTime FechaExpiracion {get;set;} public DateTime FechaServidor {get;set;}
    public decimal MontoTotal {get;set;} public string Estado {get;set;}="";
}
public class PagoDto
{
    public int Id {get;set;} public int ReservaId {get;set;} public string CodigoPago {get;set;}="";
    public string Cliente {get;set;}=""; public double Monto {get;set;}
    public string FechaPago {get;set;}=""; public string Estado {get;set;}="";
}
