using EntreFunciones.DTOSS;

namespace EntreFunciones.Servicios
{
    public interface ICatalogoService
    {
        // Lectura publica (Cartelera / Detalle): no requiere que el visitante haya iniciado sesion,
        // ver CatalogoService.CrearCabecerasPublicas().
        Task<List<EventoDTO>> ListarEventos();
        Task<EventoDTO> ObtenerEvento(int id, bool completo = false);
        Task<List<FuncionDTO>> ListarFunciones();

        // Escritura (Catalogo / Programacion, admin): requiere sesion de Administrador,
        // ver CatalogoService.CrearCabecerasAdministrador().
        Task<EventoDTO> CrearEvento(EventoDTO evento);
        Task<EventoDTO> ActualizarEvento(EventoDTO evento);
        Task EliminarEvento(int id);

        Task<FuncionDTO> ProgramarFuncion(FuncionDTO funcion);
        Task<FuncionDTO> ActualizarFuncion(FuncionDTO funcion);
        Task CancelarFuncion(int id);
    }
}
