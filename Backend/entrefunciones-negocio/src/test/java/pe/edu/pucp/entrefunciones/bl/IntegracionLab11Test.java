package pe.edu.pucp.entrefunciones.bl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.bl.impl.ventas.*;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.FuncionBLImpl;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.*;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.*;
import pe.edu.pucp.entrefunciones.dao.impl.usuarios.*;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.*;
import pe.edu.pucp.entrefunciones.modelo.catalogo.*;
import pe.edu.pucp.entrefunciones.modelo.ventas.*;
import pe.edu.pucp.entrefunciones.modelo.espacios.*;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
/** Solo en una BD aislada que termine en _test y con EF_TEST_DB=1.
 * Instalar esquema, procedimientos y datos-demo antes de activar esta prueba. */
@EnabledIfEnvironmentVariable(named="EF_TEST_DB",matches="1")
class IntegracionLab11Test {
    private final ReservaBLImpl reservas=new ReservaBLImpl();
    private final PagoBLImpl pagos=new PagoBLImpl();
    private LocalDate fecha;
    @BeforeEach void preparar() throws Exception {
        if(!System.getenv("DB_NAME").endsWith("_test"))throw new IllegalStateException("Se requiere una BD desechable *_test");
        try(Connection c=DBManager.getInstance().getConnection();Statement q=c.createStatement()) {
            for(String tabla:List.of("auditoria","entrada","pago","reserva_butaca","reserva","funcion"))q.executeUpdate("DELETE FROM "+tabla);
            q.executeUpdate("UPDATE sala SET activo=TRUE,aforo=80 WHERE id=1");
            q.executeUpdate("UPDATE sala SET activo=TRUE,aforo=3 WHERE id=2");
            q.executeUpdate("UPDATE evento SET activo=TRUE,precio=25 WHERE id=1");
            q.executeUpdate("UPDATE evento SET activo=TRUE,precio=15 WHERE id=5");
            try(ResultSet rs=q.executeQuery("SELECT CURRENT_DATE")){rs.next();fecha=rs.getDate(1).toLocalDate().plusDays(2);}
            q.executeUpdate("INSERT INTO funcion(id,evento_id,sala_id,fecha,hora_inicio,hora_fin,idioma,estado) VALUES(1,1,1,'"+fecha+"','10:00','12:00','Español','CONFIRMADA'),(2,5,2,'"+fecha+"','10:00','12:00','Español','CONFIRMADA')");
        }
    }
    private Reserva nueva(int funcion,int cliente,int cantidad,int...ids)throws Exception {
        var f=new FuncionDAOImpl().findById(funcion);var c=new ClienteDAOImpl().findById(cliente);
        ArrayList<Butaca> butacas=new ArrayList<>();for(int id:ids)butacas.add(new ButacaDAOImpl().findById(id));
        var now=LocalDateTime.of(2020,1,1,0,0);var r=new Reserva(0,f,c,butacas,now,now.plusMinutes(15));
        if(!f.requiereAsientoNumerado())r.setCantidad(cantidad);
        return r;
    }
    private Pago registrar(Reserva r)throws Exception {reservas.crearReserva(r);Pago p=pagos.generarPago(r,0);pagos.procesarPago(p.getId(),p.getMonto());return p;}
    private int contar(String sql)throws Exception {try(Connection c=DBManager.getInstance().getConnection();Statement q=c.createStatement();ResultSet rs=q.executeQuery(sql)){rs.next();return rs.getInt(1);}}
    private void sql(String sql)throws Exception {try(Connection c=DBManager.getInstance().getConnection();Statement q=c.createStatement()){q.executeUpdate(sql);}}
    @Test void reservaNumeradaPersisteYUsaDatosDelServidor()throws Exception {
        Reserva r=nueva(1,2,0,1,2);r.setPrecioUnitario(0);reservas.crearReserva(r);
        Reserva guardada=reservas.findById(r.getId());assertEquals(2,guardada.getCantidad());assertEquals(50,guardada.getMontoTotal());
        assertTrue(guardada.getFechaCreacion().getYear()>2020);assertEquals(Duration.ofMinutes(15),Duration.between(guardada.getFechaCreacion(),guardada.getFechaExpiracion()));
        sql("UPDATE evento SET precio=90 WHERE id=1");assertEquals(50,pagos.generarPago(guardada,1).getMonto());
    }
    @Test void aforoGeneralEmiteEntradasSinButaca()throws Exception {
        Reserva r=nueva(2,2,2);Pago p=registrar(r);var entradas=pagos.validar(p.getId());
        assertEquals(2,entradas.size());assertTrue(entradas.stream().allMatch(e->e.getButaca()==null));
        var dao=new EntradaDAOImpl(new ReservaDAOImpl(),new ButacaDAOImpl());assertEquals(2,dao.findByReserva(r.getId()).size());
        assertEquals(2,pagos.validar(p.getId()).size());assertEquals(2,contar("SELECT COUNT(*) FROM entrada"));
    }
    @Test void rechazaButacaAjenaYDuplicada()throws Exception {
        assertThrows(BLException.class,()->reservas.crearReserva(nueva(1,2,0,4)));
        assertThrows(BLException.class,()->reservas.crearReserva(nueva(1,2,0,1,1)));
        assertEquals(0,contar("SELECT COUNT(*) FROM reserva"));
    }
    private int competir(Callable<Void> a,Callable<Void> b)throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch listo=new CountDownLatch(2),salida=new CountDownLatch(1);
        try {
            List<Future<Boolean>> resultados=new ArrayList<>();
            for(var tarea:List.of(a,b))resultados.add(pool.submit(()-> {listo.countDown();salida.await();try{tarea.call();return true;}catch(BLException e){return false;}}));
            assertTrue(listo.await(5,TimeUnit.SECONDS));salida.countDown();int exitos=0;
            for(var f:resultados)if(f.get(20,TimeUnit.SECONDS))exitos++;return exitos;
        }finally{pool.shutdownNow();}
    }
    @Test void dosClientesNoPuedenReservarLaMismaButaca()throws Exception {
        assertEquals(1,competir(()->{reservas.crearReserva(nueva(1,2,0,1));return null;},()->{reservas.crearReserva(nueva(1,3,0,1));return null;}));
        assertEquals(1,contar("SELECT COUNT(*) FROM reserva_butaca"));
    }
    @Test void aforoGeneralNoSeSobrevendeEnConcurrencia()throws Exception {
        assertEquals(1,competir(()->{reservas.crearReserva(nueva(2,2,2));return null;},()->{reservas.crearReserva(nueva(2,3,2));return null;}));
        assertEquals(2,contar("SELECT SUM(cantidad) FROM reserva WHERE estado='ACTIVA'"));
    }
    @Test void vencimientoLiberaButacaYRechazaPagoPendiente()throws Exception {
        Reserva vieja=nueva(1,2,0,1);reservas.crearReserva(vieja);Pago p=pagos.generarPago(vieja,0);
        sql("UPDATE reserva SET fecha_creacion=CURRENT_TIMESTAMP-INTERVAL 20 MINUTE,fecha_expiracion=CURRENT_TIMESTAMP WHERE id="+vieja.getId());
        reservas.expirarVencidas();assertEquals(EstadoReserva.EXPIRADA,reservas.findById(vieja.getId()).getEstado());
        assertThrows(BLException.class,()->pagos.validar(p.getId()));
        assertEquals(EstadoPago.RECHAZADO,pagos.findById(p.getId()).getEstado());reservas.crearReserva(nueva(1,3,0,1));
    }
    @Test void falloDeEmisionReviertePagoReservaYEntradas()throws Exception {
        Reserva r=nueva(1,2,0,1,2);Pago p=registrar(r);
        var dao=new ReservaDAOImpl();
        var entradas=new EntradaDAOImpl(dao,new ButacaDAOImpl()) {
            int numero;
            @Override public void insert(Entrada e)throws SQLException {super.insert(e);if(++numero==2)throw new SQLException("Fallo controlado de emisión");}
        };
        var bl=new PagoBLImpl(new PagoDAOImpl(dao),dao,entradas);
        assertThrows(BLException.class,()->bl.validar(p.getId()));
        assertEquals(EstadoPago.PENDIENTE,pagos.findById(p.getId()).getEstado());
        assertEquals(EstadoReserva.ACTIVA,reservas.findById(r.getId()).getEstado());assertEquals(0,contar("SELECT COUNT(*) FROM entrada"));
    }
    @Test void validarDosVecesEnConcurrenciaNoDuplicaEntradas()throws Exception {
        Reserva r=nueva(1,2,0,1,2);Pago p=registrar(r);
        assertEquals(2,competir(()->{assertEquals(2,new PagoBLImpl().validar(p.getId()).size());return null;},()->{assertEquals(2,new PagoBLImpl().validar(p.getId()).size());return null;}));
        assertEquals(2,contar("SELECT COUNT(*) FROM entrada"));assertEquals(EstadoReserva.CONFIRMADA,reservas.findById(r.getId()).getEstado());
    }
    private Funcion funcion(int id,LocalDate dia,int inicio,int fin)throws Exception {
        return new Funcion(id,new EventoDAOImpl().findById(1),new SalaDAOImpl().findById(1),dia,LocalTime.of(inicio,0),LocalTime.of(fin,0),"Español");
    }
    @Test void horariosRechazanCrucesYPermitenLimitesDiasYAutoedicion()throws Exception {
        var bl=new FuncionBLImpl();assertThrows(BLException.class,()->bl.insert(funcion(0,fecha,11,13)));
        assertDoesNotThrow(()->bl.insert(funcion(0,fecha,12,13)));
        assertDoesNotThrow(()->bl.insert(funcion(0,fecha.plusDays(1),10,12)));
        var f=new FuncionDAOImpl().findById(1);assertDoesNotThrow(()->bl.update(f));
        assertThrows(IllegalArgumentException.class,()->new Funcion(0,f.getEvento(),f.getSala(),fecha,LocalTime.of(23,0),LocalTime.of(1,0),"Español"));
    }
    @Test void programacionConcurrenteSerializaLaSala()throws Exception {
        assertEquals(1,competir(()->{new FuncionBLImpl().insert(funcion(0,fecha,15,17));return null;},()->{new FuncionBLImpl().insert(funcion(0,fecha,16,18));return null;}));
    }
    @Test void cancelarYLiberarReservaEsIdempotente()throws Exception {
        Reserva r=nueva(1,2,0,1);reservas.crearReserva(r);Pago p=pagos.generarPago(r,0);reservas.delete(r.getId());reservas.delete(r.getId());
        assertEquals(EstadoPago.RECHAZADO,pagos.findById(p.getId()).getEstado());reservas.crearReserva(nueva(1,3,0,1));
    }
    @Test void consultasDaoReconstruyenContratosYSubtipos()throws Exception {
        assertNotNull(new AdministradorDAOImpl().findByDni("70011122"));assertNotNull(new ClienteDAOImpl().findByDni("71122334"));
        assertEquals(5,new EventoDAOImpl().findAll().size());assertNotNull(new SedeDAOImpl().findByIdCompleto(1));
        assertFalse(new ButacaDAOImpl().findAll().isEmpty());assertEquals(2,new FuncionDAOImpl().findAll().size());
        Reserva r=nueva(2,2,2);registrar(r);assertFalse(pagos.findAll().isEmpty());assertFalse(reservas.listarPorFuncion(2).isEmpty());
    }
    @Test void cancelarFuncionSinEntradasYRechazarPagoSonPersistentes()throws Exception {
        new FuncionBLImpl().delete(1);
        assertEquals(EstadoFuncion.CANCELADA,new FuncionDAOImpl().findById(1).getEstado());
        Reserva r=nueva(2,2,2);reservas.crearReserva(r);Pago p=pagos.generarPago(r,0);
        pagos.rechazar(p.getId());pagos.rechazar(p.getId());
        assertEquals(EstadoReserva.CANCELADA,reservas.findById(r.getId()).getEstado());
    }

}
