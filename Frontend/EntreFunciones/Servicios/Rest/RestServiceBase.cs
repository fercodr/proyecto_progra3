using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;
using Microsoft.AspNetCore.Components.Authorization;
using System.Globalization;

namespace EntreFunciones.Servicios.Rest
{
    public abstract class RestServiceBase<TDto, TViewModel>
        where TDto : Dto
        where TViewModel : ViewModel
    {
        private readonly IConfiguration _config;
        private readonly AuthenticationStateProvider _auth;

        protected abstract string ClaveRuta { get; }

        protected RestServiceBase(
            IConfiguration config,
            AuthenticationStateProvider auth)
        {
            _config = config;
            _auth = auth;
        }

        protected string Url
        {
            get => ConstruirUrlParaRuta(ClaveRuta);
        }

        protected string ConstruirUrlParaRuta(string claveRuta)
        {
            string baseUrl = _config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException(
                    "Falta configurar Rest:BaseUrl.");
            string ruta = _config[$"Rest:Rutas:{claveRuta}"]
                ?? throw new InvalidOperationException(
                    $"Falta configurar la ruta {claveRuta}.");

            return HttpUtils.ConstruirUrl(baseUrl, ruta);
        }

        protected abstract TViewModel ToViewModel(TDto dto);

        public virtual async Task<List<TViewModel>> FindAll()
        {
            return await ConsultarModelos(Url);
        }

        public virtual async Task<TViewModel> FindById(int id)
        {
            ValidarId(id);
            string url = ConstruirSubruta(
                id.ToString(CultureInfo.InvariantCulture));
            TDto dto = await Consultar<TDto>(url);
            return ToViewModel(dto);
        }

        protected async Task<List<TViewModel>> ConsultarModelos(
            IEnumerable<KeyValuePair<string, string?>> parametros)
        {
            string url = HttpUtils.AgregarParametrosConsulta(
                Url,
                parametros);
            return await ConsultarModelos(url);
        }

        protected async Task<List<TViewModel>> ConsultarModelos(string url)
        {
            List<TDto> dtos = await ConsultarLista<TDto>(url);
            return ToViewModels(dtos);
        }

        protected string ConstruirSubruta(params string[] segmentos)
        {
            return HttpUtils.ConstruirUrl(Url, segmentos);
        }

        protected async Task<TResponse> Consultar<TResponse>(string url)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            return await HttpUtils.DoGet<TResponse>(
                url,
                cabeceras: cabeceras);
        }

        protected async Task<List<TResponse>> ConsultarLista<TResponse>(
            string url)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            return await HttpUtils.DoGetList<TResponse>(
                url,
                cabeceras: cabeceras);
        }

        protected async Task<TViewModel> Insert<TRequest>(TRequest payload)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            TDto dto = await HttpUtils.DoPost<TRequest, TDto>(
                Url,
                payload,
                cabeceras: cabeceras);
            return ToViewModel(dto);
        }

        protected async Task<TViewModel> Update<TRequest>(
            int id,
            TRequest payload)
        {
            ValidarId(id);
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = ConstruirSubruta(
                id.ToString(CultureInfo.InvariantCulture));
            TDto dto = await HttpUtils.DoPut<TRequest, TDto>(
                url,
                payload,
                cabeceras: cabeceras);
            return ToViewModel(dto);
        }

        protected async Task Delete(int id)
        {
            ValidarId(id);
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = ConstruirSubruta(
                id.ToString(CultureInfo.InvariantCulture));
            await HttpUtils.DoDelete(url, cabeceras: cabeceras);
        }

        protected List<TViewModel> ToViewModels(IEnumerable<TDto> dtos)
        {
            List<TViewModel> modelos = new();
            foreach (TDto dto in dtos)
            {
                modelos.Add(ToViewModel(dto));
            }

            return modelos;
        }

        private async Task<IReadOnlyDictionary<string, string>>
            CrearCabeceras()
        {
            return await HttpUtils.CrearCabecerasAutenticadas(
                _config,
                _auth);
        }

        private static void ValidarId(int id)
        {
            if (id <= 0)
            {
                throw new ArgumentOutOfRangeException(
                    nameof(id),
                    "El identificador debe ser positivo.");
            }
        }
    }
}
