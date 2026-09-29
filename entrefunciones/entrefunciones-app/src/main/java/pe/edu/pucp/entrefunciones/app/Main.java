package pe.edu.pucp.entrefunciones.app;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Recurso;
import pe.edu.pucp.entrefunciones.modelo.espacios.Bloqueo;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ObraTeatro;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ActividadCultural;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.TipoActividad;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoReserva;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoPago;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoEntrada;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
/**
 * Modulo de ejecucion: simula de principio a fin el modelo de negocio de
 * EntreFunciones usando unicamente las clases del modulo de dominio (no hay
 * base de datos ni interfaz grafica involucradas). El propio main() cumple el
 * rol de "codigo de prueba": ademas de narrar el flujo de negocio, la seccion
 * final ejecuta comparaciones explicitas (metodo verificar()) sobre el estado
 * de los objetos y reporta cuantas se cumplieron, sin depender de ningun
 * framework externo (no usa JUnit).
 */
public class Main {

    private static int totalVerificaciones = 0;
    private static int verificacionesOk = 0;

    /** Imprime el resultado de una verificacion puntual y lleva el conteo total. */
    private static void verificar(String descripcion, boolean condicion) {
        totalVerificaciones++;
        if (condicion) {
            verificacionesOk++;
            System.out.println("[OK]    " + descripcion);
        } else {
            System.out.println("[ERROR] " + descripcion);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== EntreFunciones: simulacion del modelo de negocio ===\n");

        // ---------- 1. Usuarios ----------
        Administrador admin = new Administrador(1, "Marco", "Reyes",
                "marco.reyes@entrefunciones.pe", "hash_admin1", "70011122", "EMP-0001");

        Cliente valeria = new Cliente(2, "Valeria", "Suarez",
                "valeria.suarez@gmail.com", "hash_cliente1", "71122334",
                LocalDate.of(2026, 1, 15), "987654321");

        Cliente diego = new Cliente(3, "Diego", "Palacios",
                "diego.palacios@gmail.com", "hash_cliente2", "72233445",
                LocalDate.of(2026, 2, 3), "987654322");

        System.out.println("Administrador: " + admin.getNombres() + " " + admin.getApellidos()
                + " (DNI " + admin.getDni() + ", codigo " + admin.getCodigoEmpleado() + ")");
        System.out.println("Clientes registrados: " + valeria.getNombres() + ", " + diego.getNombres());

        // ---------- 2. Espacios: sede, salas, butacas, recurso, bloqueo ----------
        Sede sede = new Sede(1, "EntreFunciones Miraflores S.A.C.", "Av. Larco 1234", "Lima", "014001122");
        Sala sala1 = new Sala(1, sede, "Sala 1", 80, "Cine 2D");
        Sala sala2 = new Sala(2, sede, "Sala 2", 40, "Teatro");
        sede.agregarSala(sala1);
        sede.agregarSala(sala2);

        Butaca a1 = new Butaca(1, sala1, "A", 1, "General");
        Butaca a2 = new Butaca(2, sala1, "A", 2, "General");
        Butaca a3 = new Butaca(3, sala1, "A", 3, "VIP");
        sala1.agregarButaca(a1);
        sala1.agregarButaca(a2);
        sala1.agregarButaca(a3);

        System.out.println("\nSede: " + sede.getRazonSocial() + " (" + sede.getSalas().size() + " salas)");
        System.out.println("Sala 1 pertenece a: " + sala1.getSede().getRazonSocial());
        System.out.println("Butaca A1 pertenece a la sala: " + a1.getSala().getNombre());

        Recurso proyector = new Recurso(1, "Proyector 4K Sala 1", "Proyeccion");
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setId(1);
        bloqueo.setMotivo("Calibracion de proyector");
        bloqueo.setFechaInicio(LocalDateTime.of(2026, 9, 18, 9, 0));
        bloqueo.setFechaFin(LocalDateTime.of(2026, 9, 18, 11, 0));
        bloqueo.setRecurso(proyector);
        boolean bloqueoVigente = bloqueo.estaVigente(LocalDateTime.of(2026, 9, 18, 10, 0));
        System.out.println("Bloqueo activo el 18/09 10:00? " + bloqueoVigente);

        // ---------- 3. Eventos (catalogo) y Funciones (programacion) ----------
        // Evento es abstracta: Pelicula, ObraTeatro y ActividadCultural HEREDAN de
        // ella (id, precio, lista de funciones, activo/inactivo). Cada Evento se
        // crea UNA sola vez y guarda su lista de Funciones; cada Funcion apunta al
        // Evento que se presenta (sala, fecha, horario, idioma y estado son de la
        // Funcion). Asi, si Interestelar se programa a las 15h y a las 18h, los
        // datos de catalogo (titulo, genero, director) NO se repiten.
        Pelicula interestelar = new Pelicula(1, 25.00,
                "Interestelar", 169, CategoriaGenero.DRAMA, ClasificacionEdad.PG13, "Christopher Nolan");
        ObraTeatro laMuralla = new ObraTeatro(2, 35.00,
                "La Muralla", "Sofia Herrera", "Juan Perez, Maria Lopez");
        ActividadCultural charlaCine = new ActividadCultural(3, 15.00,
                "Charla de Cine Peruano Contemporaneo", "Rodrigo Vega", TipoActividad.CHARLA);

        Funcion funcion15h = new Funcion(1, interestelar, sala1, LocalDate.of(2026, 9, 20),
                LocalTime.of(15, 0), LocalTime.of(17, 49), "Espanol");
        Funcion funcion18h = new Funcion(2, interestelar, sala1, LocalDate.of(2026, 9, 20),
                LocalTime.of(18, 0), LocalTime.of(20, 49), "Subtitulada");
        Funcion funcionObra = new Funcion(3, laMuralla, sala2, LocalDate.of(2026, 9, 21),
                LocalTime.of(19, 0), LocalTime.of(21, 0), "Espanol");
        Funcion funcionCharla = new Funcion(4, charlaCine, sala2, LocalDate.of(2026, 9, 22),
                LocalTime.of(18, 0), LocalTime.of(19, 0), "Espanol");

        // Evento -> lista de Funciones
        interestelar.getFunciones().add(funcion15h);
        interestelar.getFunciones().add(funcion18h);
        laMuralla.getFunciones().add(funcionObra);
        charlaCine.getFunciones().add(funcionCharla);

        // Sala -> lista de Funciones (y Funcion -> Sala)
        sala1.agregarFuncion(funcion15h);
        sala1.agregarFuncion(funcion18h);
        sala2.agregarFuncion(funcionObra);
        sala2.agregarFuncion(funcionCharla);

        // Polimorfismo: los 3 tipos se tratan como Evento
        ArrayList<Evento> cartelera = new ArrayList<>();
        cartelera.add(interestelar);
        cartelera.add(laMuralla);
        cartelera.add(charlaCine);
        System.out.println("\nCartelera (" + cartelera.size() + " eventos):");
        for (Evento e : cartelera) {
            System.out.println("  - [" + e.getClass().getSimpleName() + "] " + e.getTitulo()
                    + " | precio S/ " + e.getPrecio() + " | funciones: " + e.getFunciones().size());
        }

        System.out.println("Interestelar programada en 2 funciones (id " + funcion15h.getId()
                + " y " + funcion18h.getId() + "), duracion " + interestelar.getDuracionMinutos() + " min cada vez.");
        System.out.println("Ambas funciones apuntan al MISMO objeto Evento? "
                + (funcion15h.getEvento() == funcion18h.getEvento())
                + " (con la nueva estructura el catalogo no se duplica)");

        boolean funcion15hConfirmada = admin.gestionarFuncion(funcion15h);
        admin.gestionarFuncion(funcionObra);

        // ---------- 4. Flujo de venta completo: Reserva -> Pago -> Entrada ----------
        System.out.println("\n--- Flujo de venta (Valeria compra 2 butacas para la funcion de las 15h) ---");
        ArrayList<Butaca> butacasReservadas = new ArrayList<>();
        butacasReservadas.add(a1);
        butacasReservadas.add(a2);

        LocalDateTime ahora = LocalDateTime.of(2026, 9, 14, 10, 0);
        Reserva reserva1 = new Reserva(1, funcion15h, valeria, butacasReservadas, ahora, ahora.plusMinutes(15));
        System.out.println("Reserva creada, estado: " + reserva1.getEstado());

        double totalReserva1 = funcion15h.getEvento().getPrecio() * butacasReservadas.size();
        Pago pago1 = new Pago(1, "PAY-0001", totalReserva1, reserva1);
        System.out.println("Pago generado (" + pago1.getCodigoPago() + "), reserva.getPago() es el mismo objeto? "
                + (reserva1.getPago() == pago1));

        boolean pagoOk = pago1.procesarPago(totalReserva1);
        System.out.println("Cliente registra el pago: " + pagoOk);
        boolean validado = pago1.validar();
        System.out.println("Administrador valida el pago: " + validado + " (estado: " + pago1.getEstado() + ")");

        Entrada entradaA1 = null;
        Entrada entradaA2 = null;
        boolean ingreso = false;
        if (pago1.getEstado() == EstadoPago.VALIDADO) {
            reserva1.confirmar();
            System.out.println("Reserva confirmada, estado: " + reserva1.getEstado());

            LocalDateTime fechaEmision = ahora.plusMinutes(6);
            entradaA1 = new Entrada(1, fechaEmision, "QR-0000000001", funcion15h.getEvento().getPrecio(), a1, reserva1);
            entradaA2 = new Entrada(2, fechaEmision, "QR-0000000002", funcion15h.getEvento().getPrecio(), a2, reserva1);
            valeria.comprarEntrada(entradaA1);
            valeria.comprarEntrada(entradaA2);
            System.out.println("Entradas emitidas: " + entradaA1.getCodigoQR() + ", " + entradaA2.getCodigoQR());
            System.out.println("Historial de Valeria tiene " + valeria.getEntradas().size() + " entrada(s).");

            ingreso = entradaA1.validarIngreso();
            System.out.println("Valeria ingresa con la entrada " + entradaA1.getCodigoQR() + ": " + ingreso
                    + " (estado ahora: " + entradaA1.getEstado() + ")");
        }

        // ---------- 5. Flujo de reserva que vence sin pagarse ----------
        System.out.println("\n--- Flujo de reserva vencida (Diego no completa el pago a tiempo) ---");
        ArrayList<Butaca> butacaDiego = new ArrayList<>();
        butacaDiego.add(a3);
        LocalDateTime creacionDiego = LocalDateTime.of(2026, 9, 10, 9, 0);
        Reserva reserva2 = new Reserva(2, funcion18h, diego, butacaDiego, creacionDiego, creacionDiego.plusMinutes(15));
        Pago pago2 = new Pago(2, "PAY-0002", funcion18h.getEvento().getPrecio(), reserva2);

        LocalDateTime momentoDeRevision = creacionDiego.plusMinutes(20);
        boolean expiro = reserva2.expirarSiVencida(momentoDeRevision);
        System.out.println("La reserva de Diego expiro al revisarla 20 min despues? " + expiro
                + " (estado: " + reserva2.getEstado() + ", pago: " + pago2.getEstado() + ")");
        System.out.println("La butaca A3 vuelve a estar libre para la funcion 18h (no se emitio ninguna Entrada).");

        // ---------- 6. Auditoria ----------
        System.out.println("\n--- Auditoria ---");
        Auditoria incidencia = new Auditoria(1, LocalDateTime.of(2026, 9, 14, 10, 6),
                "Marco Reyes", "Cliente reporto butaca A2 con reposabrazos suelto durante la funcion.", funcion15h);
        System.out.println("Incidencia registrada para la funcion " + incidencia.getFuncion().getId()
                + ", estado: " + incidencia.getEstado());
        boolean incidenciaEnProceso = incidencia.atender();
        boolean incidenciaResuelta = incidencia.resolver();
        System.out.println("Incidencia atendida y resuelta, estado final: " + incidencia.getEstado());

        Auditoria trazabilidad = new Auditoria(2, LocalDateTime.of(2026, 9, 14, 9, 58),
                "valeria.suarez@gmail.com", "Inicio de sesion exitoso desde nueva ubicacion.");
        System.out.println("Registro de trazabilidad general (sin funcion asociada)? "
                + (trazabilidad.getFuncion() == null));

        // ---------- 7. Casos adicionales: eliminado logico y reglas de reserva ----------
        System.out.println("\n--- Casos adicionales: eliminado logico y reglas de reserva ---");

        // 7.1 La butaca A2 (la de la incidencia) se retira temporalmente de servicio y luego se repara.
        a2.desactivar();
        boolean butacaQuedoInactiva = !a2.estaActivo();
        a2.activar();
        boolean butacaQuedoActivaDeNuevo = a2.estaActivo();
        System.out.println("Butaca A2 retirada por mantenimiento y luego reactivada? "
                + (butacaQuedoInactiva && butacaQuedoActivaDeNuevo));

        // 7.2 Se cancela la funcion de teatro (por ejemplo, cambio de planes del elenco).
        boolean funcionObraCancelada = funcionObra.cancelar();
        System.out.println("La funcion de 'La Muralla' se cancelo? " + funcionObraCancelada
                + " (estado: " + funcionObra.getEstado() + ")");

        // 7.2b El Evento (no la funcion) tambien es IEliminable: se retira del catalogo sin borrarlo.
        charlaCine.desactivar();
        boolean eventoQuedoInactivo = !charlaCine.estaActivo();
        charlaCine.activar();
        boolean eventoQuedoActivoDeNuevo = charlaCine.estaActivo();
        System.out.println("La charla se retiro del catalogo y luego se reactivo? "
                + (eventoQuedoInactivo && eventoQuedoActivoDeNuevo));

        // 7.2c Constructor de copia de Evento (Pelicula): campos iguales, objeto distinto.
        Pelicula copiaInterestelar = new Pelicula(interestelar);
        System.out.println("Copia de Interestelar: " + copiaInterestelar.getTitulo() + " ("
                + copiaInterestelar.getFunciones().size() + " funciones), mismo objeto? "
                + (copiaInterestelar == interestelar));

        // 7.2d Validaciones del constructor de Funcion.
        boolean rechazoEventoNulo = false;
        try {
            new Funcion(99, null, sala1, LocalDate.of(2026, 9, 25), LocalTime.of(10, 0), LocalTime.of(12, 0), "Espanol");
        } catch (IllegalArgumentException ex) {
            rechazoEventoNulo = true;
        }
        boolean rechazoHorarioInvalido = false;
        try {
            new Funcion(98, interestelar, sala1, LocalDate.of(2026, 9, 25), LocalTime.of(12, 0), LocalTime.of(10, 0), "Espanol");
        } catch (IllegalArgumentException ex) {
            rechazoHorarioInvalido = true;
        }
        System.out.println("Funcion sin evento rechazada? " + rechazoEventoNulo
                + " | Funcion con horaFin < horaInicio rechazada? " + rechazoHorarioInvalido);

        // 7.3 Valeria pide reembolso de la entrada A2 antes de usarla.
        boolean entradaA2Cancelada = entradaA2 != null && entradaA2.cancelar();
        System.out.println("La entrada A2 se cancelo antes de usarse? " + entradaA2Cancelada
                + " (estado: " + (entradaA2 != null ? entradaA2.getEstado() : "N/A") + ")");

        // 7.4 Diego cancela una reserva explicitamente, sin esperar a que venza.
        ArrayList<Butaca> butacasReserva3 = new ArrayList<>();
        butacasReserva3.add(a3);
        LocalDateTime creacionReserva3 = LocalDateTime.of(2026, 9, 15, 9, 0);
        Reserva reserva3 = new Reserva(3, funcion15h, diego, butacasReserva3, creacionReserva3, creacionReserva3.plusMinutes(15));
        boolean reserva3Cancelada = reserva3.cancelar();
        System.out.println("Diego cancela su reserva sin esperar que venza? " + reserva3Cancelada
                + " (estado: " + reserva3.getEstado() + ")");

        // 7.5 La reserva de un cuarto cliente NO debe expirar si se revisa antes de tiempo.
        ArrayList<Butaca> butacasReserva4 = new ArrayList<>();
        butacasReserva4.add(a1);
        LocalDateTime creacionReserva4 = LocalDateTime.of(2026, 9, 15, 12, 0);
        Reserva reserva4 = new Reserva(4, funcion18h, valeria, butacasReserva4, creacionReserva4, creacionReserva4.plusMinutes(15));
        boolean expiroAntesDeTiempo = reserva4.expirarSiVencida(creacionReserva4.plusMinutes(5));
        System.out.println("La reserva revisada solo 5 min despues (vence a los 15) expiro antes de tiempo? "
                + expiroAntesDeTiempo + " (deberia ser false; estado: " + reserva4.getEstado() + ")");

        // ---------- 8. Verificacion de comportamiento esperado ----------
        System.out.println("\n=== Verificacion de comportamiento esperado ===");

        verificar("Sala 1 conoce su Sede", sala1.getSede() == sede);
        verificar("Butaca A1 conoce su Sala", a1.getSala() == sala1);
        verificar("El bloqueo del proyector esta vigente dentro del intervalo configurado",
                bloqueoVigente && bloqueo.getRecurso() == proyector && bloqueo.getSala() == null);
        verificar("La funcion de las 15h conoce su Sala", funcion15h.getSala() == sala1);
        verificar("Pelicula, ObraTeatro y ActividadCultural son Evento (herencia)",
                (Object) interestelar instanceof Evento && (Object) laMuralla instanceof Evento
                        && (Object) charlaCine instanceof Evento);
        verificar("getTitulo() es polimorfico segun el tipo de Evento",
                interestelar.getTitulo().equals("Interestelar")
                        && laMuralla.getTitulo().equals("La Muralla")
                        && charlaCine.getTitulo().startsWith("Charla"));
        verificar("Interestelar tiene 2 funciones en su lista",
                interestelar.getFunciones().size() == 2
                        && interestelar.getFunciones().contains(funcion15h)
                        && interestelar.getFunciones().contains(funcion18h));
        verificar("Las 2 funciones de Interestelar comparten el MISMO Evento (catalogo no duplicado)",
                funcion15h.getEvento() == interestelar && funcion18h.getEvento() == interestelar
                        && funcion15h != funcion18h);
        verificar("Cada funcion conoce su Evento y el Evento la contiene (relacion bidireccional)",
                funcionObra.getEvento() == laMuralla && laMuralla.getFunciones().contains(funcionObra)
                        && funcionCharla.getEvento() == charlaCine && charlaCine.getFunciones().contains(funcionCharla));
        verificar("El precio ahora vive en el Evento y se hereda a las 3 subclases",
                interestelar.getPrecio() == 25.00 && laMuralla.getPrecio() == 35.00 && charlaCine.getPrecio() == 15.00);
        verificar("calcularDuracion() de la funcion coincide con la duracion de la pelicula",
                funcion15h.calcularDuracion() == interestelar.getDuracionMinutos());
        verificar("El constructor de copia de Pelicula copia los datos en un objeto distinto",
                copiaInterestelar != interestelar
                        && copiaInterestelar.getTitulo().equals(interestelar.getTitulo())
                        && copiaInterestelar.getGenero() == interestelar.getGenero()
                        && copiaInterestelar.getPrecio() == interestelar.getPrecio());
        verificar("Un Evento se puede retirar del catalogo y reactivar (IEliminable)",
                eventoQuedoInactivo && eventoQuedoActivoDeNuevo);
        verificar("Una Funcion sin Evento es rechazada", rechazoEventoNulo);
        verificar("Una Funcion con horaFin anterior a horaInicio es rechazada", rechazoHorarioInvalido);
        verificar("Pago de la reserva 1 = precio del evento x butacas",
                pago1.getMonto() == 50.00);
        verificar("Reserva y Pago se referencian mutuamente",
                reserva1.getPago() == pago1 && pago1.getReserva() == reserva1);
        verificar("El administrador confirmo la funcion de las 15h",
                funcion15hConfirmada && funcion15h.getEstado() == EstadoFuncion.CONFIRMADA);
        verificar("La auditoria de incidencia queda ligada a la funcion durante la que ocurrio",
                incidencia.getFuncion() == funcion15h);
        verificar("El registro de trazabilidad general no tiene funcion asociada",
                trazabilidad.getFuncion() == null);
        verificar("El ciclo de estados de una auditoria (pendiente -> en proceso -> resuelta) funciona",
                incidenciaEnProceso && incidenciaResuelta && incidencia.getEstado() == EstadoAuditoria.RESUELTA);
        verificar("El flujo de venta completo deja el pago VALIDADO",
                pago1.getEstado() == EstadoPago.VALIDADO);
        verificar("El flujo de venta completo deja la entrada A1 UTILIZADA tras el ingreso",
                ingreso && entradaA1 != null && entradaA1.getEstado() == EstadoEntrada.UTILIZADA);
        verificar("La reserva de Diego expira si se revisa despues de vencida",
                expiro && reserva2.getEstado() == EstadoReserva.EXPIRADA);
        verificar("La reserva NO expira si se revisa antes de tiempo",
                !expiroAntesDeTiempo && reserva4.getEstado() == EstadoReserva.ACTIVA);
        verificar("Una reserva se puede cancelar explicitamente, sin esperar a que venza",
                reserva3Cancelada && reserva3.getEstado() == EstadoReserva.CANCELADA);
        verificar("Una butaca se puede retirar de servicio y reactivar (eliminado logico)",
                butacaQuedoInactiva && butacaQuedoActivaDeNuevo);
        verificar("Una funcion se puede cancelar (eliminado logico)",
                funcionObraCancelada && funcionObra.getEstado() == EstadoFuncion.CANCELADA);
        verificar("Una entrada se puede cancelar antes de usarse (eliminado logico)",
                entradaA2Cancelada && entradaA2 != null && entradaA2.getEstado() == EstadoEntrada.CANCELADA);

        System.out.println("\n" + verificacionesOk + "/" + totalVerificaciones + " verificaciones superadas.");
        if (verificacionesOk == totalVerificaciones) {
            System.out.println("El dominio soporta correctamente el modelo de negocio descrito.");
        } else {
            System.out.println("Hay verificaciones fallidas: revisar el modulo de dominio.");
        }

        // ---------- 9. Conexion a la base de datos (AWS RDS) ----------
        // Usa db.DBManager (patron Singleton) con los datos de db.properties.
        // Mientras la instancia de AWS RDS no este creada / db.properties no
        // tenga los datos reales, esta seccion va a mostrar un mensaje de
        // error controlado en vez de interrumpir el resto de la simulacion.
        System.out.println("\n--- Conexion a la base de datos ---");
        try (Connection con = DBManager.getInstance().getConnection()) {
            System.out.println("Conexion a la base de datos establecida correctamente. Valida: "
                    + con.isValid(2));
        } catch (Exception e) {
            System.out.println("No se pudo conectar a la base de datos (revisar los datos en "
                    + "db.properties y que la instancia de AWS RDS este activa): " + e.getMessage());
        }

        System.out.println("\n=== Fin de la simulacion ===");
    }
}
