using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;
using Microsoft.AspNetCore.Components.Authorization;
using System.Security.Claims;

namespace EntreFunciones.Servicios.Rest
{
    public class AuditoriaService : IAuditoriaService
    {
        private readonly IConfiguration _config;
        private readonly AuthenticationStateProvider _auth;

        public AuditoriaService(
            IConfiguration config,
            AuthenticationStateProvider auth)
        {
            _config = config;
            _auth = auth;
        }

        private string Url
        {
            get
            {
                string baseUrl = _config["Rest:BaseUrl"]
                    ?? throw new InvalidOperationException(
                        "Falta configurar Rest:BaseUrl.");
                string ruta = _config["Rest:Rutas:Auditorias"]
                    ?? throw new InvalidOperationException(
                        "Falta configurar la ruta Auditorias.");
                return $"{baseUrl.TrimEnd('/')}/{ruta.TrimStart('/')}";
            }
        }

        public async Task<List<AuditoriaViewModel>> FindAll(
            string? descripcion = null,
            EstadoAuditoria? estado = null)
        {
            List<string> parametros = new();
            if (!string.IsNullOrWhiteSpace(descripcion))
            {
                parametros.Add(
                    $"descripcion={Uri.EscapeDataString(descripcion.Trim())}");
            }
            if (estado.HasValue)
            {
                parametros.Add($"estado={estado.Value}");
            }

            string url = parametros.Count == 0
                ? Url
                : $"{Url}?{string.Join("&", parametros)}";
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            List<AuditoriaDTO> dtos =
                await HttpUtils.DoGet<List<AuditoriaDTO>>(
                    url,
                    cabeceras: cabeceras);
            return dtos.Select(ToViewModel).ToList();
        }

        public async Task<AuditoriaViewModel> FindById(int id)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            AuditoriaDTO dto = await HttpUtils.DoGet<AuditoriaDTO>(
                $"{Url}/{id}",
                cabeceras: cabeceras);
            return ToViewModel(dto);
        }

        public async Task<AuditoriaViewModel> Insert(
            NuevaAuditoriaFormModel formulario)
        {
            CrearAuditoriaDTO payload = new()
            {
                FuncionId = formulario.FuncionId,
                ResponsableId = formulario.ResponsableId,
                Descripcion = formulario.Descripcion
            };
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            AuditoriaDTO dto =
                await HttpUtils.DoPost<CrearAuditoriaDTO, AuditoriaDTO>(
                    Url,
                    payload,
                    cabeceras: cabeceras);
            return ToViewModel(dto);
        }

        public async Task<AuditoriaViewModel> Update(
            int id,
            AtencionAuditoriaFormModel formulario)
        {
            ActualizarAuditoriaDTO payload = new()
            {
                ResponsableId = formulario.ResponsableId,
                Estado = formulario.Estado
            };
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            AuditoriaDTO dto =
                await HttpUtils.DoPut<ActualizarAuditoriaDTO, AuditoriaDTO>(
                    $"{Url}/{id}",
                    payload,
                    cabeceras: cabeceras);
            return ToViewModel(dto);
        }

        public async Task<List<AdministradorViewModel>>
            FindResponsablesActivos()
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            List<AdministradorDTO> dtos =
                await HttpUtils.DoGet<List<AdministradorDTO>>(
                    $"{Url}/opciones/responsables",
                    cabeceras: cabeceras);
            return dtos.Select(ToViewModel).ToList();
        }

        public async Task<List<FuncionViewModel>> FindFuncionesAsignables()
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            List<FuncionDTO> dtos =
                await HttpUtils.DoGet<List<FuncionDTO>>(
                    $"{Url}/opciones/funciones",
                    cabeceras: cabeceras);
            return dtos.Select(ToViewModel).ToList();
        }

        private static AuditoriaViewModel ToViewModel(AuditoriaDTO dto)
        {
            return new AuditoriaViewModel
            {
                Id = dto.Id,
                Funcion = dto.Funcion is null
                    ? null
                    : ToViewModel(dto.Funcion),
                Fecha = dto.Fecha,
                Responsable = dto.Responsable is null
                    ? null
                    : ToViewModel(dto.Responsable),
                ResponsableTextoOriginal = dto.ResponsableTextoOriginal,
                Descripcion = dto.Descripcion,
                EstadoAuditoria = dto.Estado
            };
        }

        private static AdministradorViewModel ToViewModel(
            AdministradorDTO dto)
        {
            return new AdministradorViewModel
            {
                Id = dto.Id,
                IsActivo = dto.Activo,
                Nombres = dto.Nombres,
                Apellidos = dto.Apellidos
            };
        }

        private static FuncionViewModel ToViewModel(FuncionDTO dto)
        {
            return new FuncionViewModel
            {
                Id = dto.Id,
                Titulo = dto.Titulo,
                Fecha = dto.Fecha,
                HoraInicio = dto.HoraInicio,
                Sede = dto.Sede,
                Sala = dto.Sala,
                Estado = dto.Estado
            };
        }

        private async Task<IReadOnlyDictionary<string, string>>
            CrearCabeceras()
        {
            AuthenticationState estadoAutenticacion =
                await _auth.GetAuthenticationStateAsync();
            ClaimsPrincipal usuario = estadoAutenticacion.User;
            string? idUsuario = usuario
                .FindFirst(ClaimTypes.NameIdentifier)?.Value;

            if (usuario.Identity?.IsAuthenticated != true
                || !int.TryParse(idUsuario, out int cuentaId)
                || cuentaId <= 0)
            {
                throw new InvalidOperationException(
                    "Inicia sesion para continuar.");
            }

            string? apiKey = _config["Rest:ApiKey"];
            if (string.IsNullOrWhiteSpace(apiKey))
            {
                throw new InvalidOperationException(
                    "Falta configurar el acceso al servicio REST.");
            }

            return new Dictionary<string, string>
            {
                ["X-Api-Key"] = apiKey,
                ["X-Usuario-Id"] = cuentaId.ToString()
            };
        }
    }
}
