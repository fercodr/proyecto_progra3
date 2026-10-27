package pe.edu.pucp.entrefunciones.modelo;
/**
 * Contrato comun para las entidades que se gestionan mediante eliminado logico:
 * en lugar de borrarse de la base de datos, se marcan como inactivas para
 * conservar el historial y las referencias que otras entidades tengan hacia ellas.
 */
public interface IEliminable{
    void activar();
    void desactivar();
    boolean estaActivo();
}
