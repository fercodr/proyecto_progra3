namespace EntreFunciones.Seguridad
{
    // Datos hardcodeados para maquetar las pantallas sin conectar a la BD ni al BL.
    public class ClienteDemo
    {
        public int Id { get; set; }
        public string Nombres { get; set; } = "";
        public string Apellidos { get; set; } = "";
        public string Dni { get; set; } = "";
        public string Correo { get; set; } = "";
        public string Telefono { get; set; } = "";
        public bool IsActivo { get; set; }
    }

    public class AdministradorDemo
    {
        public int Id { get; set; }
        public string CodigoEmpleado { get; set; } = "";
        public string Nombres { get; set; } = "";
        public string Apellidos { get; set; } = "";
        public string Correo { get; set; } = "";
        public bool IsActivo { get; set; }
    }

    public static class DatosDemo
    {
        public static readonly List<ClienteDemo> Clientes = [
            new() { Id = 1,  Nombres = "Luis",    Apellidos = "Ramírez Soto",   Dni = "70123456", Correo = "luis.ramirez@mail.com",   Telefono = "987654321", IsActivo = true },
            new() { Id = 2,  Nombres = "María",   Apellidos = "Quispe Huamán",  Dni = "71234567", Correo = "maria.quispe@mail.com",    Telefono = "986543210", IsActivo = true },
            new() { Id = 3,  Nombres = "Carlos",  Apellidos = "Mendoza Rojas",  Dni = "72345678", Correo = "carlos.mendoza@mail.com",  Telefono = "985432109", IsActivo = false },
            new() { Id = 4,  Nombres = "Ana",     Apellidos = "Torres Vega",    Dni = "73456789", Correo = "ana.torres@mail.com",      Telefono = "984321098", IsActivo = true },
            new() { Id = 5,  Nombres = "Jorge",   Apellidos = "Castillo Díaz",  Dni = "74567890", Correo = "jorge.castillo@mail.com",  Telefono = "983210987", IsActivo = true },
            new() { Id = 6,  Nombres = "Rosa",    Apellidos = "Flores Paredes", Dni = "75678901", Correo = "rosa.flores@mail.com",     Telefono = "982109876", IsActivo = false },
            new() { Id = 7,  Nombres = "Pedro",   Apellidos = "Salazar Luna",   Dni = "76789012", Correo = "pedro.salazar@mail.com",   Telefono = "981098765", IsActivo = true },
            new() { Id = 8,  Nombres = "Lucía",   Apellidos = "Vargas Cruz",    Dni = "77890123", Correo = "lucia.vargas@mail.com",    Telefono = "980987654", IsActivo = true },
            new() { Id = 9,  Nombres = "Miguel",  Apellidos = "Herrera Ponce",  Dni = "78901234", Correo = "miguel.herrera@mail.com",  Telefono = "979876543", IsActivo = true },
            new() { Id = 10, Nombres = "Sofía",   Apellidos = "Ríos Medina",    Dni = "79012345", Correo = "sofia.rios@mail.com",      Telefono = "978765432", IsActivo = false },
            new() { Id = 11, Nombres = "Diego",   Apellidos = "Navarro Gil",    Dni = "70234567", Correo = "diego.navarro@mail.com",   Telefono = "977654321", IsActivo = true },
            new() { Id = 12, Nombres = "Valeria", Apellidos = "Campos Ortiz",   Dni = "71345678", Correo = "valeria.campos@mail.com",  Telefono = "976543210", IsActivo = true }
        ];

        public static readonly List<AdministradorDemo> Administradores = [
            new() { Id = 1, CodigoEmpleado = "EMP-0001", Nombres = "Roberto",  Apellidos = "Gómez Lara",    Correo = "admin@entrefunciones.com",            IsActivo = true },
            new() { Id = 2, CodigoEmpleado = "EMP-0002", Nombres = "Patricia", Apellidos = "Núñez Rivas",   Correo = "patricia.nunez@entrefunciones.com",   IsActivo = true },
            new() { Id = 3, CodigoEmpleado = "EMP-0003", Nombres = "Fernando", Apellidos = "Aguilar Paz",   Correo = "fernando.aguilar@entrefunciones.com", IsActivo = false },
            new() { Id = 4, CodigoEmpleado = "EMP-0004", Nombres = "Carmen",   Apellidos = "Delgado Ruiz",  Correo = "carmen.delgado@entrefunciones.com",   IsActivo = true },
            new() { Id = 5, CodigoEmpleado = "EMP-0005", Nombres = "Andrés",   Apellidos = "Silva Montes",  Correo = "andres.silva@entrefunciones.com",     IsActivo = true },
            new() { Id = 6, CodigoEmpleado = "EMP-0006", Nombres = "Julia",    Apellidos = "Paredes Luque", Correo = "julia.paredes@entrefunciones.com",    IsActivo = true },
            new() { Id = 7, CodigoEmpleado = "EMP-0007", Nombres = "Héctor",   Apellidos = "Bravo Meza",    Correo = "hector.bravo@entrefunciones.com",     IsActivo = false }
        ];
    }
}
