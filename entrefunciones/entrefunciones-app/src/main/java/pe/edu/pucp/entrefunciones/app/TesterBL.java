package pe.edu.pucp.entrefunciones.app;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.catalogo.ActividadCulturalBL;
import pe.edu.pucp.entrefunciones.bl.catalogo.PeliculaBL;
import pe.edu.pucp.entrefunciones.bl.espacios.BloqueoBL;
import pe.edu.pucp.entrefunciones.bl.espacios.ButacaBL;
import pe.edu.pucp.entrefunciones.bl.espacios.SalaBL;
import pe.edu.pucp.entrefunciones.bl.espacios.SedeBL;
import pe.edu.pucp.entrefunciones.bl.eventos.FuncionBL;
import pe.edu.pucp.entrefunciones.bl.impl.catalogo.ActividadCulturalBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.catalogo.PeliculaBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.BloqueoBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.ButacaBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.SalaBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.espacios.SedeBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.FuncionBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.ClienteBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.ventas.EntradaBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.ventas.PagoBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.ventas.ReservaBLImpl;
import pe.edu.pucp.entrefunciones.bl.usuarios.ClienteBL;
import pe.edu.pucp.entrefunciones.bl.ventas.EntradaBL;
import pe.edu.pucp.entrefunciones.bl.ventas.PagoBL;
import pe.edu.pucp.entrefunciones.bl.ventas.ReservaBL;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.EntradaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.PagoDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.ReservaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ActividadCultural;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.catalogo.TipoActividad;
import pe.edu.pucp.entrefunciones.modelo.espacios.Bloqueo;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoEntrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoReserva;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;

/**
 * Modulo de prueba de la capa de negocio (BL): a diferencia de Main.java
 * (que simula el modelo de dominio en memoria, sin base de datos), esta
 * clase ejercita el flujo CRUD real de al menos 4 entidades a traves de
 * sus clases *BLImpl -> *DAOImpl -> procedimientos almacenados / SQL, tal
 * como lo exige la rubrica del laboratorio para la capa de logica de
 * negocio: "gestion de transacciones desde la BL" + "Tester/main()
 * demostrando CRUD via BL para al menos 4 entidades".
 *
 * <p>Ademas del CRUD basico (insert/find/update/delete) sobre Sede, Sala,
 * Butaca, Cliente, Pelicula (Evento), Funcion, ActividadCultural y
 * Bloqueo, el bloque 8-10 ejercita especificamente el trabajo de
 * TransactionsManager/TransaccionBL de este ciclo de correcciones:</p>
 * <ul>
 *   <li>El flujo completo Reserva -&gt; Pago -&gt; N Entradas
 *   ({@link PagoBL#validar}) como una unica transaccion atomica, tanto
 *   para asiento numerado (Pelicula/ObraTeatro) como para aforo general
 *   (ActividadCultural);</li>
 *   <li>Un caso de <b>rollback real</b>: una reserva de aforo general que
 *   excede la capacidad de la sala es rechazada por el trigger de MySQL
 *   trg_reserva_tipo_y_aforo a mitad de la transaccion, y se verifica que
 *   TransaccionBL revierte el INSERT por completo (no queda una fila a
 *   medias) y que TransactionsManager queda en un estado limpio para que
 *   la siguiente operacion funcione con normalidad.</li>
 * </ul>
 *
 * <p>Requiere una base de datos accesible segun entrefunciones-app/src/
 * main/resources/db.properties (mismo mecanismo que usa DBManager en el
 * resto del proyecto).</p>
 */
public class TesterBL {

    private static int totalVerificaciones = 0;
    private static int verificacionesOk = 0;

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
        System.out.println("=== EntreFunciones: CRUD via la capa de negocio (BL) contra la base de datos ===\n");

        try {
            String sufijo = String.valueOf(System.currentTimeMillis());
            // ---------- 1. Sede ----------
            SedeBL sedeBL = new SedeBLImpl();
            Sede sede = new Sede(0, "Sede Tester BL", "Av. Tester 100", "Lima", "011000000");
            sedeBL.insert(sede);
            verificar("Sede insertada via BL con id autogenerado", sede.getId() > 0);

            Sede sedeLeida = sedeBL.findById(sede.getId());
            verificar("Sede leida via BL coincide con la insertada",
                    sedeLeida != null && "Sede Tester BL".equals(sedeLeida.getRazonSocial()));

            sede.setTelefonoContacto("011999999");
            sedeBL.update(sede);
            Sede sedeActualizada = sedeBL.findById(sede.getId());
            verificar("Sede actualizada via BL persiste el nuevo telefono",
                    "011999999".equals(sedeActualizada.getTelefonoContacto()));

            // ---------- 2. Sala (una numerada, una de aforo general) ----------
            SalaBL salaBL = new SalaBLImpl();
            Sala salaCine = new Sala(0, sede, "Sala Tester Cine", 50, "PELICULA");
            salaBL.insert(salaCine);
            Sala salaAforo = new Sala(0, sede, "Sala Tester Aforo", 30, "ACTIVIDAD_CULTURAL");
            salaBL.insert(salaAforo);
            verificar("Las 2 salas se insertaron via BL con id autogenerado",
                    salaCine.getId() > 0 && salaAforo.getId() > 0);

            List<Sala> salasDeSede = salaBL.listarPorSede(sede.getId());
            verificar("listarPorSede() via BL devuelve las 2 salas recien creadas",
                    salasDeSede.size() == 2);

            // ---------- 3. Butaca (para la sala de cine) ----------
            ButacaBL butacaBL = new ButacaBLImpl();
            Butaca butacaA1 = new Butaca(0, salaCine, "A", 1, "GENERAL");
            Butaca butacaA2 = new Butaca(0, salaCine, "A", 2, "GENERAL");
            butacaBL.insert(butacaA1);
            butacaBL.insert(butacaA2);
            verificar("Butacas A1/A2 insertadas via BL con id autogenerado",
                    butacaA1.getId() > 0 && butacaA2.getId() > 0);

            // ---------- 4. Cliente ----------


            ClienteBL clienteBL = new ClienteBLImpl();

            String ultimos8 =
                    sufijo.substring(sufijo.length() - 8);

            String correoTester =
                    "tester.bl." + sufijo + "@example.com";

            String dniTester =
                    ultimos8;

            String telefonoTester =
                    "9" + ultimos8;

            Cliente cliente = new Cliente(
                    0,
                    "Tester",
                    "BL",
                    correoTester,
                    "hash123",
                    dniTester,
                    LocalDate.of(2020, 1, 1),
                    telefonoTester
            );

            clienteBL.insert(cliente);


            verificar("Cliente insertado via BL con id autogenerado", cliente.getId() > 0);

            long numeroBase = Long.parseLong(ultimos8);

            String telefonoActualizado =
                    "9" + String.format(
                            "%08d",
                            (numeroBase + 1) % 100000000
                    );

            cliente.setTelefono(telefonoActualizado);
            clienteBL.update(cliente);

            Cliente clienteActualizado =
                    clienteBL.findById(cliente.getId());

            verificar(
                    "Cliente actualizado via BL persiste el nuevo telefono",
                    clienteActualizado != null
                            && telefonoActualizado.equals(clienteActualizado.getTelefono())
            );

            // ---------- 5. Pelicula (Evento de asiento numerado) ----------
            PeliculaBL peliculaBL = new PeliculaBLImpl();
            Pelicula pelicula = new Pelicula(0, 20.00, "Pelicula Tester", 120,
                    CategoriaGenero.ACCION, ClasificacionEdad.PG13, "Director Tester");
            peliculaBL.insert(pelicula);
            verificar("Pelicula (Evento) insertada via BL con id autogenerado", pelicula.getId() > 0);

            // ---------- 6. Funcion (numerada, para la pelicula) ----------
            FuncionBL funcionBL = new FuncionBLImpl();
            Funcion funcionCine = new Funcion(0, pelicula, salaCine, LocalDate.of(2026, 12, 1),
                    LocalTime.of(20, 0), LocalTime.of(22, 0), "ES");
            pelicula.agregarFuncion(funcionCine);
            funcionBL.insert(funcionCine);
            verificar("Funcion de asiento numerado insertada via BL con id autogenerado",
                    funcionCine.getId() > 0);

            // ---------- 7. ActividadCultural (Evento de aforo general) + su Funcion ----------
            ActividadCulturalBL actividadBL = new ActividadCulturalBLImpl();
            ActividadCultural actividad = new ActividadCultural(0, 12.00,
                    "Taller Tester", "Expositor Tester", TipoActividad.TALLER);
            Funcion funcionActividad = new Funcion(0, actividad, salaAforo, LocalDate.of(2026, 12, 2),
                    LocalTime.of(18, 0), LocalTime.of(19, 0), "ES");
            actividad.agregarFuncion(funcionActividad);
            actividadBL.insert(actividad);
            verificar("ActividadCultural (aforo general) insertada via BL junto con su Funcion",
                    actividad.getId() > 0 && funcionActividad.getId() > 0
                            && !actividad.requiereAsientoNumerado());

            // ---------- 8. Flujo transaccional completo: asiento numerado ----------
            // Reserva -> Pago -> validar() (confirma la reserva y emite las
            // entradas EN UNA SOLA TRANSACCION via TransaccionBL).
            System.out.println("\n--- Flujo transaccional (asiento numerado): Reserva -> Pago -> Entradas ---");
            ReservaBL reservaBL = new ReservaBLImpl();
            ArrayList<Butaca> butacasReservadas = new ArrayList<>();
            butacasReservadas.add(butacaA1);
            butacasReservadas.add(butacaA2);
            LocalDateTime ahora = LocalDateTime.now();
            Reserva reservaNumerada = new Reserva(0, funcionCine, cliente, butacasReservadas,
                    ahora, ahora.plusMinutes(15));
            reservaBL.crearReserva(reservaNumerada);
            verificar("Reserva de asiento numerado creada via BL (2 butacas)",
                    reservaNumerada.getId() > 0 && !reservaNumerada.esAforoGeneral());

            PagoBL pagoBL = new PagoBLImpl(
                    new PagoDAOImpl(new ReservaDAOImpl()),
                    new ReservaDAOImpl(),
                    new EntradaDAOImpl(new ReservaDAOImpl(), new ButacaDAOImpl()));

            Pago pagoNumerado = pagoBL.generarPago(reservaNumerada, 40.00);
            verificar("Pago generado via BL para la reserva numerada", pagoNumerado.getId() > 0);

            pagoBL.procesarPago(pagoNumerado.getId(), 40.00);
            List<Entrada> entradasNumeradas = pagoBL.validar(pagoNumerado.getId());
            Reserva reservaNumeradaConfirmada = reservaBL.findById(reservaNumerada.getId());
            verificar("validar() confirma la Reserva y emite 2 Entradas de forma atomica",
                    entradasNumeradas.size() == 2
                            && reservaNumeradaConfirmada.getEstado() == EstadoReserva.CONFIRMADA);

            EntradaBL entradaBL = new EntradaBLImpl(new EntradaDAOImpl(new ReservaDAOImpl(), new ButacaDAOImpl()));
            entradaBL.validarIngreso(entradasNumeradas.get(0).getCodigoQR());
            Entrada entradaValidada = entradaBL.buscarPorCodigoQR(entradasNumeradas.get(0).getCodigoQR());
            verificar("La primera entrada, tras validarIngreso(), queda UTILIZADA",
                    entradaValidada != null && entradaValidada.getEstado() == EstadoEntrada.UTILIZADA);

            entradaBL.cancelar(entradasNumeradas.get(1).getId());
            Entrada entradaCancelada = entradaBL.buscarPorCodigoQR(entradasNumeradas.get(1).getCodigoQR());
            verificar("La segunda entrada se cancela via BL (eliminado logico)",
                    entradaCancelada != null && entradaCancelada.getEstado() == EstadoEntrada.CANCELADA);

            // ---------- 9. Flujo transaccional completo: aforo general ----------
            System.out.println("\n--- Flujo transaccional (aforo general): Reserva -> Pago -> Entradas ---");
            Reserva reservaAforo = new Reserva(0, funcionActividad, cliente, 5, ahora, ahora.plusMinutes(15));
            reservaBL.crearReserva(reservaAforo);
            verificar("Reserva de aforo general creada via BL (cantidad=5, sin butacas)",
                    reservaAforo.getId() > 0 && reservaAforo.esAforoGeneral());

            Pago pagoAforo = pagoBL.generarPago(reservaAforo, 12.00 * 5);
            pagoBL.procesarPago(pagoAforo.getId(), 60.00);
            List<Entrada> entradasAforo = pagoBL.validar(pagoAforo.getId());
            boolean todasSinButaca = true;
            for (Entrada e : entradasAforo) {
                if (!e.esAforoGeneral()) {
                    todasSinButaca = false;
                }
            }
            verificar("validar() emite 5 Entradas de aforo general (sin butaca asignada)",
                    entradasAforo.size() == 5 && todasSinButaca);

            // ---------- 10. Prueba de ROLLBACK real: exceder el aforo disponible ----------
            System.out.println("\n--- Prueba de rollback: una reserva que excede el aforo disponible ---");
            int reservasAntesDelRechazo = reservaBL.listarPorFuncion(funcionActividad.getId()).size();
            boolean rechazoPorAforo = false;
            try {
                // Aforo de la sala = 30, ya colocamos 5 -> quedan 25 libres; se
                // pide 100 a proposito para que el trigger de MySQL
                // trg_reserva_tipo_y_aforo dispare un SIGNAL a mitad del
                // INSERT y TransaccionBL revierta la transaccion completa.
                Reserva reservaExcedida = new Reserva(0, funcionActividad, cliente, 100, ahora, ahora.plusMinutes(15));
                reservaBL.crearReserva(reservaExcedida);
            } catch (BLException ex) {
                rechazoPorAforo = true;
                System.out.println("  (rechazo esperado por el trigger de aforo: " + ex.getMessage() + ")");
            }
            int reservasDespuesDelRechazo = reservaBL.listarPorFuncion(funcionActividad.getId()).size();
            verificar("La reserva que excede el aforo se rechaza y no deja una fila a medias (rollback)",
                    rechazoPorAforo && reservasDespuesDelRechazo == reservasAntesDelRechazo);

            boolean transactionsManagerQuedoLimpio;
            Reserva reservaPostRollback = new Reserva(0, funcionActividad, cliente, 1, ahora, ahora.plusMinutes(15));
            try {
                reservaBL.crearReserva(reservaPostRollback);
                transactionsManagerQuedoLimpio = reservaPostRollback.getId() > 0;
            } catch (BLException ex) {
                transactionsManagerQuedoLimpio = false;
            }
            verificar("Tras el rollback, TransactionsManager queda limpio: la siguiente operacion funciona normalmente",
                    transactionsManagerQuedoLimpio);

            // ---------- 11. Bloqueo (historial inmutable) ----------
            // El esquema vigente no tiene una columna activo para Bloqueo.
            // Por esa razón la BL rechaza la baja y conserva el registro:
            // nunca se sustituye una baja lógica por un DELETE físico.
            System.out.println("\n--- Bloqueo: insercion y conservacion del historial ---");
            BloqueoBL bloqueoBL = new BloqueoBLImpl();
            Bloqueo bloqueo = new Bloqueo();
            bloqueo.setMotivo("Mantenimiento (tester BL)");
            bloqueo.setFechaInicio(LocalDateTime.of(2026, 12, 10, 8, 0));
            bloqueo.setFechaFin(LocalDateTime.of(2026, 12, 10, 12, 0));
            bloqueo.setSala(salaCine);
            bloqueoBL.insert(bloqueo);
            verificar("Bloqueo insertado via BL con id autogenerado", bloqueo.getId() > 0);

            boolean bajaBloqueoRechazada = false;
            try {
                bloqueoBL.delete(bloqueo.getId());
            } catch (BLException ex) {
                bajaBloqueoRechazada = true;
            }
            Bloqueo bloqueoConservado = bloqueoBL.findById(bloqueo.getId());
            verificar("Bloqueo sin estado activo rechaza la baja y conserva su historial",
                    bajaBloqueoRechazada && bloqueoConservado != null);

            // ---------- 12. Sala (eliminado logico), para cerrar el ciclo CRUD ----------
            salaBL.delete(salaCine.getId());
            Sala salaTrasEliminar = salaBL.findById(salaCine.getId());
            verificar("Sala eliminada logicamente via BL (activo=false) pero sigue en la base de datos",
                    salaTrasEliminar != null && !salaTrasEliminar.estaActivo());

            System.out.println("\n" + verificacionesOk + "/" + totalVerificaciones + " verificaciones superadas.");
            if (verificacionesOk == totalVerificaciones) {
                System.out.println("La capa de negocio (BL) y la gestion de transacciones funcionan correctamente "
                        + "contra la base de datos.");
            } else {
                System.out.println("Hay verificaciones fallidas: revisar la capa de negocio y/o la conexion a la "
                        + "base de datos configurada en db.properties.");
            }

        } catch (BLException ex) {
            System.out.println("\n[ERROR INESPERADO] Una operacion de negocio fallo fuera de los casos previstos: "
                    + ex.getMessage());
            Throwable causa = ex.getCause();
            if (causa != null) {
                System.out.println("  causa: " + causa.getMessage());
            }
            System.out.println(verificacionesOk + "/" + totalVerificaciones
                    + " verificaciones superadas antes del error.");
        }
    }
}
