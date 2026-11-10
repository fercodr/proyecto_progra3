namespace EntreFunciones.ViewModels
{
    public class FuncionViewModel : ViewModel
    {
        public string Titulo { get; set; } = string.Empty;
        public DateOnly Fecha { get; set; }
        public TimeOnly HoraInicio { get; set; }
        public string Sede { get; set; } = string.Empty;
        public string Sala { get; set; } = string.Empty;
        public string Estado { get; set; } = string.Empty;

        public string Presentacion =>
            $"{Titulo} · {Fecha:dd/MM}, {HoraInicio:HH\\:mm} · {Sede} / {Sala}";
    }
}
