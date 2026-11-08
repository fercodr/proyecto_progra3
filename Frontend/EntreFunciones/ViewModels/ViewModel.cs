namespace EntreFunciones.ViewModels
{
    public abstract class ViewModel
    {
        public int Id { get; set; }
        public bool IsActivo { get; set; } = true;
    }
}
