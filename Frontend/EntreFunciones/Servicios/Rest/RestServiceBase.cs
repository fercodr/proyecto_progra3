using EntreFunciones.ViewModels;
using EntreFunciones.DTOSS;
using Microsoft.AspNetCore.Components.Authorization;
namespace EntreFunciones.Servicios.Rest
{
    public class RestServiceBase<TDto, TViewModel>
        where TDto : Dto
        where TViewModel : ViewModel
    {
        protected readonly HttpClient _htpp;
        protected readonly IConfiguration _config;
        protected readonly AuthenticationStateProvider _auth;
        

    }
}
