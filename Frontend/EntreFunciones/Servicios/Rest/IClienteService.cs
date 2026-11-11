using EntreFunciones.DTOSS.Usuarios;

namespace EntreFunciones.Servicios.Rest
{
    public interface IClienteService
    {
        Task<ClienteDTO> Registrar(ClienteDTO cliente);
        Task<List<ClienteDTO>> Listar();                                   
        Task<ClienteDTO> ObtenerPorId(int id);                             
        Task<ClienteDTO> Actualizar(int id, ClienteDTO cliente);          
        Task CambiarContrasena(int id, string actual, string nueva);      
        Task Desactivar(int id);                                           
        Task Activar(int id);
    }
}
