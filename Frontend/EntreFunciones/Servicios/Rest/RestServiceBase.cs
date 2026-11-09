using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios.Rest
{
    public abstract class RestServiceBase<TDto, TViewModel>
        where TDto : Dto
        where TViewModel : ViewModel
    {
        private readonly IConfiguration _config;

        protected abstract string ClaveRuta { get; }

        protected RestServiceBase(IConfiguration config)
        {
            _config = config;
        }

        protected string Url
        {
            get
            {
                string baseUrl = _config["Rest:BaseUrl"]
                    ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
                string ruta = _config[$"Rest:Rutas:{ClaveRuta}"]
                    ?? throw new InvalidOperationException($"Falta configurar la ruta {ClaveRuta}.");

                return $"{baseUrl.TrimEnd('/')}/{ruta.TrimStart('/')}";
            }
        }

        protected abstract TViewModel ToViewModel(TDto dto);
        protected abstract TDto ToDto(TViewModel modelo);

        public virtual async Task<List<TViewModel>> FindAll()
        {
            List<TDto> dtos = await HttpUtils.DoGet<List<TDto>>(Url);
            return ToViewModels(dtos);
        }

        public virtual async Task<TViewModel> FindById(int id)
        {
            TDto dto = await HttpUtils.DoGet<TDto>($"{Url.TrimEnd('/')}/{id}");
            return ToViewModel(dto);
        }

        protected List<TViewModel> ToViewModels(List<TDto> dtos)
        {
            List<TViewModel> modelos = new();
            foreach (TDto dto in dtos)
            {
                modelos.Add(ToViewModel(dto));
            }

            return modelos;
        }

        protected List<TDto> ToDtos(List<TViewModel> modelos)
        {
            List<TDto> dtos = new();
            foreach (TViewModel modelo in modelos)
            {
                dtos.Add(ToDto(modelo));
            }

            return dtos;
        }

        public virtual async Task Insert(TViewModel modelo)
        {
            TDto dto = ToDto(modelo);
            await HttpUtils.DoPost<TDto>(Url, dto);
        }

        public virtual async Task Update(TViewModel modelo)
        {
            TDto dto = ToDto(modelo);
            await HttpUtils.DoPut<TDto>($"{Url.TrimEnd('/')}/{modelo.Id}", dto);
        }

        public virtual async Task Delete(int id)
        {
            await HttpUtils.DoDelete($"{Url.TrimEnd('/')}/{id}");
        }
    }
}
