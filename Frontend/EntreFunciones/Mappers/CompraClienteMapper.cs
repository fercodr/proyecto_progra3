using System.Globalization;
using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;

namespace EntreFunciones.Mappers
{
    // Convierte las reservas del REST en lo que muestran las pantallas del flujo de compra
    // (Selección de butacas, Reserva y pago, Mis compras). Centraliza textos y colores de estado.
    public static class CompraClienteMapper
    {
        /// <param name="pagoRegistrado">
        /// true si el cliente ya pagó y falta la validación del administrador
        /// (el backend mantiene la reserva ACTIVA hasta que se valide).
        /// </param>
        public static CompraClienteViewModel ToViewModel(ReservaDto dto, bool pagoRegistrado = false)
        {
            string estado = dto.Estado == "ACTIVA" && pagoRegistrado ? "EN_VALIDACION" : EstadoEfectivo(dto);
            return new CompraClienteViewModel
            {
                Id = dto.Id,
                Codigo = dto.CodigoOId,
                Funcion = dto.FuncionNombre,
                FechaFuncion = dto.FechaFuncion,
                Cantidad = dto.Cantidad,
                Monto = dto.MontoTotal,
                FechaCreacion = dto.FechaCreacion,
                FechaExpiracion = dto.FechaExpiracion,
                Estado = EstadoTexto(estado),
                EstadoClase = EstadoClase(estado),
                Pendiente = estado == "ACTIVA"
            };
        }

        public static List<CompraClienteViewModel> ToViewModels(IEnumerable<ReservaDto> dtos, ISet<int>? reservasPagadas = null) =>
            dtos.Select(d => ToViewModel(d, reservasPagadas?.Contains(d.Id) == true))
                .OrderByDescending(c => c.FechaCreacion).ToList();

        /// <summary>
        /// Una reserva ACTIVA cuyo plazo ya venció se muestra como EXPIRADA aunque el backend
        /// todavía no la haya marcado.
        /// </summary>
        public static string EstadoEfectivo(ReservaDto dto) =>
            dto.Estado == "ACTIVA" && dto.FechaExpiracion <= dto.FechaServidor ? "EXPIRADA" : dto.Estado;

        public static string EstadoTexto(string estado) => estado switch
        {
            "ACTIVA" => "Pendiente de pago",
            "EN_VALIDACION" => "Pago en validación",
            "CONFIRMADA" => "Confirmada",
            "EXPIRADA" => "Expirada",
            "CANCELADA" => "Cancelada",
            _ => estado
        };

        public static string EstadoClase(string estado) => estado switch
        {
            "ACTIVA" => "text-bg-warning",
            "EN_VALIDACION" => "text-bg-info",
            "CONFIRMADA" => "text-bg-success",
            "EXPIRADA" => "text-bg-secondary",
            "CANCELADA" => "text-bg-danger",
            _ => "text-bg-light"
        };

        public static string Soles(decimal monto) => $"S/ {monto.ToString("N2", CultureInfo.InvariantCulture)}";
    }
}
