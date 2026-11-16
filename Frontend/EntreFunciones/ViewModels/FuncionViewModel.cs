namespace EntreFunciones.ViewModels
{
    public class FuncionViewModel : ViewModel
    {
        public string Titulo { get; set; } = string.Empty;
        public DateOnly Fecha { get; set; }
        public TimeOnly HoraInicio { get; set; }
        public TimeOnly HoraFin { get; set; }
        public string Idioma { get; set; } = string.Empty;
        public string Sede { get; set; } = string.Empty;
        public string Sala { get; set; } = string.Empty;
        public string Estado { get; set; } = string.Empty;
        public EventoViewModel? Evento { get; set; }
        public SalaViewModel? SalaRelacionada { get; set; }
        public SedeViewModel? SedeRelacionada { get; set; }
        public int Aforo { get; set; }
        public int? EntradasVigentes { get; set; }
        public int? EntradasEmitidas { get; set; }
        public int? EntradasCanceladas { get; set; }
        public decimal? IngresosConfirmados { get; set; }
        public decimal? PorcentajeOcupacion { get; set; }

        public string Presentacion =>
            $"{Titulo} · {Fecha:dd/MM}, {HoraInicio:HH\\:mm}-{HoraFin:HH\\:mm} · {Sede} / {Sala}";
    }
}
