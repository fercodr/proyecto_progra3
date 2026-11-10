using EntreFunciones.DTOSS.Usuarios;

namespace EntreFunciones.Servicios.Rest
{
    public interface IClienteService
    {
        Task<ClienteDTO> Registrar(ClienteDTO cliente);
    }
}
