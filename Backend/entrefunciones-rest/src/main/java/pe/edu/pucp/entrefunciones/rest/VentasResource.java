package pe.edu.pucp.entrefunciones.rest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.json.*;
import pe.edu.pucp.entrefunciones.bl.*;
import pe.edu.pucp.entrefunciones.bl.impl.ventas.*;
import pe.edu.pucp.entrefunciones.dao.impl.*;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.*;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.usuarios.ClienteDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.*;
import pe.edu.pucp.entrefunciones.modelo.ventas.*;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import java.util.*;
import java.time.LocalDateTime;
@Path("/") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
public class VentasResource {
    @Context SecurityContext session;
    private final ReservaBLImpl reservas=new ReservaBLImpl();
    private final PagoBLImpl pagos=new PagoBLImpl();
    private int usuario(){return Integer.parseInt(session.getUserPrincipal().getName());}
    private boolean admin(){return session.isUserInRole("Administrador");}
    private void exigirAdmin(){if(!admin())throw new ForbiddenException("Se requiere administrador");}
    private Reserva propia(int id)throws Exception{
        Reserva r=reservas.findById(id);
        if(r==null || (!admin() && r.getCliente().getId()!=usuario()))throw new NotFoundException("No se encontró la reserva");
        return r;
    }
    static String titulo(Funcion f){Evento e=f.getEvento();return e instanceof Pelicula p?p.getNombrePelicula():e instanceof ObraTeatro o?o.getTitulo():((ActividadCultural)e).getNombre();}
    @GET @Path("funciones/{id}/disponibilidad") public JsonObject disponibilidad(@PathParam("id")int id)throws Exception{
        return Transacciones.ejecutar("No se pudo consultar la disponibilidad",()-> {
            ControlOperacionesDAO c=new ControlOperacionesDAO();c.bloquearFuncion(id);c.expirarEnFuncion(id);
            Funcion f=new FuncionDAOImpl().findById(id);
            if(f==null || f.getEstado()!=pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion.CONFIRMADA || !f.getEvento().estaActivo()
                || !f.getSala().estaActivo() || !f.getSala().getSede().estaActivo() || !f.getFecha().atTime(f.getHoraInicio()).isAfter(c.ahora()))throw new BLException("La función no está disponible");
            Set<Integer> ocupadas=c.butacasOcupadas(id);JsonArrayBuilder asientos=Json.createArrayBuilder();
            if(f.requiereAsientoNumerado())for(Butaca b:new ButacaDAOImpl().findBySalaId(f.getSala().getId()))
                asientos.add(Json.createObjectBuilder().add("id",b.getId()).add("fila",b.getFila()).add("columna",b.getColumna()).add("activo",b.estaActivo()).add("ocupada",ocupadas.contains(b.getId())));
            return Json.createObjectBuilder().add("id",id).add("titulo",titulo(f)).add("sede",f.getSala().getSede().getRazonSocial()).add("sala",f.getSala().getNombre())
                .add("fecha",f.getFecha().toString()).add("horaInicio",f.getHoraInicio().toString()).add("precio",f.getEvento().getPrecio())
                .add("aforoNumerado",f.requiereAsientoNumerado()).add("cuposDisponibles",Math.max(0,f.getSala().getAforo()-(f.requiereAsientoNumerado()?ocupadas.size():c.cuposOcupados(id))))
                .add("butacas",asientos).build();
        });
    }
    @POST @Path("reservas") public Response crear(JsonObject body)throws Exception{
        if(admin())throw new ForbiddenException("Use una cuenta cliente para comprar");
        if(body==null || !body.containsKey("funcionId"))throw new BadRequestException("Indique la función");
        Funcion f=new FuncionDAOImpl().findById(body.getInt("funcionId"));
        if(f==null)throw new NotFoundException("No se encontró la función");
        var cliente=new ClienteDAOImpl().findById(usuario());if(cliente==null)throw new ForbiddenException("Se requiere una cuenta cliente");
        ArrayList<Butaca> butacas=new ArrayList<>();
        JsonArray seleccion=body.getJsonArray("butacaIds");
        if(seleccion!=null){if(seleccion.size()>1000)throw new BadRequestException("Selección demasiado grande");
            for(JsonValue valor:seleccion){Butaca b=new ButacaDAOImpl().findById(((JsonNumber)valor).intValueExact());if(b==null)throw new BadRequestException("Butaca inexistente");butacas.add(b);}}
        var now=LocalDateTime.now();Reserva r=new Reserva(0,f,cliente,butacas,now,now.plusMinutes(15));
        if(!f.requiereAsientoNumerado())r.setCantidad(body.getInt("cantidad"));
        reservas.crearReserva(r);
        return Response.status(201).entity(reservaDto(r)).build();
    }
    @GET @Path("reservas/{id}") public JsonObject detalle(@PathParam("id")int id)throws Exception{return reservaDto(propia(id));}
    @GET @Path("reservas") public JsonArray listarReservas()throws Exception{
        JsonArrayBuilder b=Json.createArrayBuilder();for(Reserva r:admin()?reservas.findAll():reservas.listarPorCliente(usuario()))b.add(reservaDto(r));return b.build();
    }
    @DELETE @Path("reservas/{id}") public Response cancelar(@PathParam("id")int id)throws Exception{propia(id);reservas.delete(id);return Response.noContent().build();}
    @POST @Path("reservas/{id}/pago") public JsonObject generar(@PathParam("id")int id)throws Exception{Reserva r=propia(id);return pagoDto(pagos.generarPago(r,0));}
    @POST @Path("pagos/{id}/registrar") public Response registrar(@PathParam("id")int id)throws Exception{
        Pago p=pagos.findById(id);if(p==null)throw new NotFoundException("No se encontró el pago");propia(p.getReserva().getId());pagos.procesarPago(id,p.getMonto());return Response.noContent().build();
    }
    @GET @Path("pagos") public JsonArray listarPagos()throws Exception{
        exigirAdmin();reservas.expirarVencidas();JsonArrayBuilder b=Json.createArrayBuilder();for(Pago p:pagos.findAll())b.add(pagoDto(p));return b.build();
    }
    @POST @Path("pagos/{id}/validar") public JsonArray validar(@PathParam("id")int id)throws Exception{
        exigirAdmin();JsonArrayBuilder b=Json.createArrayBuilder();for(Entrada e:pagos.validar(id))b.add(Json.createObjectBuilder().add("id",e.getId()).add("codigoQR",e.getCodigoQR()).add("estado",e.getEstado().name()));return b.build();
    }
    @POST @Path("pagos/{id}/rechazar") public Response rechazar(@PathParam("id")int id)throws Exception{exigirAdmin();pagos.rechazar(id);return Response.noContent().build();}
    private JsonObject reservaDto(Reserva r){
        return Json.createObjectBuilder().add("id",r.getId()).add("codigoOId","RES-"+r.getId()).add("funcionId",r.getFuncion().getId()).add("funcionNombre",titulo(r.getFuncion()))
          .add("fechaFuncion",r.getFuncion().getFecha().atTime(r.getFuncion().getHoraInicio()).toString()).add("cliente",r.getCliente().getNombres()+" "+r.getCliente().getApellidos()).add("dni",r.getCliente().getDni())
          .add("fechaServidor",java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Lima")).toLocalDateTime().toString()).add("cantidad",r.getCantidad()).add("fechaCreacion",r.getFechaCreacion().toString()).add("fechaExpiracion",r.getFechaExpiracion().toString()).add("montoTotal",r.getMontoTotal()).add("estado",r.getEstado().name()).build();
    }
    private JsonObject pagoDto(Pago p){return Json.createObjectBuilder().add("id",p.getId()).add("reservaId",p.getReserva().getId()).add("codigoPago",p.getCodigoPago()).add("cliente",p.getReserva().getCliente().getNombres()+" "+p.getReserva().getCliente().getApellidos()).add("monto",p.getMonto()).add("fechaPago",p.getFechaPago()==null?"":p.getFechaPago().toString()).add("estado",p.getEstado().name()).build();}
}
