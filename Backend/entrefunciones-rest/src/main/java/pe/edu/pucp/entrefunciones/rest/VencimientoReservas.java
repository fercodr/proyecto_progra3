package pe.edu.pucp.entrefunciones.rest;
import jakarta.ejb.*;
import pe.edu.pucp.entrefunciones.bl.impl.ventas.ReservaBLImpl;
@Singleton @Startup @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VencimientoReservas {
    @Schedule(hour="*",minute="*",second="*/30",persistent=false)
    public void liberarVencidas(){
        try{new ReservaBLImpl().expirarVencidas();}
        catch(Exception e){java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"No se pudieron liberar las reservas vencidas",e);}
    }
}
