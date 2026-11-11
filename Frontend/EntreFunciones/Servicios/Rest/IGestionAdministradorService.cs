using EntreFunciones.DTOSS.Usuarios;

namespace EntreFunciones.Servicios.Rest
{
 
    public interface IGestionAdministradorService
    {
        
        Task<List<AdministradorDTO>> Listar();

       
        Task<AdministradorDTO> Registrar(AdministradorDTO administrador);

     
        Task Desactivar(int id);

      
        Task Activar(int id);
    }
}
