using EntreFunciones.DTOSS;

namespace EntreFunciones.Servicios
{
    public interface ICatalogoService
    {
        // Lectura publica (Cartelera / Detalle): no requiere que el visitante haya iniciado sesion.
        Task<List<EventoDTO>> ListarEventos();
        Task<EventoDTO> ObtenerEvento(int id, bool completo = false);
        Task<List<FuncionDTO>> ListarFunciones();

        // Consulta historica y escritura administrativas: requieren sesion de Administrador.
        Task<List<FuncionDTO>> ListarFuncionesAdministrativas();
        Task<EventoDTO> CrearEvento(EventoDTO evento);
        Task<EventoDTO> ActualizarEvento(EventoDTO evento);
        Task EliminarEvento(int id);

        Task<FuncionDTO> ProgramarFuncion(FuncionDTO funcion);
        Task<FuncionDTO> ActualizarFuncion(FuncionDTO funcion);
        Task CancelarFuncion(int id);
    }
}
