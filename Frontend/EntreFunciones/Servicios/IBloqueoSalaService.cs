using EntreFunciones.DTOSS;

namespace EntreFunciones.Servicios;

public interface IBloqueoSalaService
{
    Task<List<BloqueoSalaDto>> Listar();
    Task<BloqueoSalaDto> Insertar(BloqueoSalaDto bloqueo);
    Task<BloqueoSalaDto> Actualizar(BloqueoSalaDto bloqueo);
    Task Desactivar(int id);
    Task<DisponibilidadProgramacionDto> ConsultarDisponibilidad(
        int salaId, DateOnly fecha, TimeOnly inicio, TimeOnly fin, int? excluirFuncionId = null);
}
