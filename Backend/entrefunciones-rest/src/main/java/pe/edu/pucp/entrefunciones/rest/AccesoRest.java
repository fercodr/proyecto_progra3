package pe.edu.pucp.entrefunciones.rest;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;
import pe.edu.pucp.entrefunciones.db.DBManager;
import java.sql.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
/** Autenticación entre servidores: la clave solo vive en Java y C#, nunca en el navegador. */
@Provider @Priority(Priorities.AUTHENTICATION)
public class AccesoRest implements ContainerRequestFilter {
    @Override public void filter(ContainerRequestContext request) {
        String configured=System.getProperty("ENTREFUNCIONES_API_KEY",System.getenv("ENTREFUNCIONES_API_KEY"));
        String supplied=request.getHeaderString("X-Api-Key");
        if(configured==null || configured.isBlank()) { rechazar(request,503,"Configure el acceso REST en el servidor");return; }
        if(supplied==null || !MessageDigest.isEqual(configured.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8))) { rechazar(request,401,"Acceso REST no autorizado");return; }
        int id;
        try {id=Integer.parseInt(request.getHeaderString("X-Usuario-Id"));if(id<=0)throw new NumberFormatException();}
        catch(RuntimeException e) {rechazar(request,401,"Se requiere una sesión válida");return;}
        try(Connection c=DBManager.getInstance().getConnection();PreparedStatement q=c.prepareStatement("SELECT EXISTS(SELECT 1 FROM administrador a WHERE a.usuario_id=u.id) AS admin FROM usuario u WHERE u.id=? AND u.activo")) {
            q.setInt(1,id);try(ResultSet rs=q.executeQuery()) {
                if(!rs.next()){rechazar(request,403,"La cuenta no está activa");return;}
                boolean admin=rs.getBoolean(1);boolean secure=request.getSecurityContext().isSecure();
                request.setSecurityContext(new SecurityContext() {
                    public Principal getUserPrincipal(){return ()->Integer.toString(id);}
                    public boolean isUserInRole(String role){return admin?role.equals("Administrador"):role.equals("Cliente");}
                    public boolean isSecure(){return secure;}
                    public String getAuthenticationScheme(){return "ServerKey";}
                });
            }
        }catch(SQLException e){rechazar(request,503,"No se pudo verificar la cuenta");}
    }
    private void rechazar(ContainerRequestContext r,int status,String message){r.abortWith(Response.status(status).type(MediaType.APPLICATION_JSON).entity(java.util.Map.of("mensaje",message)).build());}
}
