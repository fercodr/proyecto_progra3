package pe.edu.pucp.entrefunciones.rest.reportes;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Genera los PDF de detalle (boleto por boleto) de un evento especifico,
 * a partir de los stored procedures sp_reporte_ocupacion_evento y
 * sp_reporte_entradas_evento. A diferencia de ReporteFuncionesPdf (que arma
 * los datos en Java con la capa de negocio), aqui se le entrega a Jasper una
 * conexion JDBC en vivo y es la plantilla la que llama al stored procedure
 * directamente (mismo patron mostrado en clase: sesion-de-clase-22).
 */
public final class ReporteDetalleEventoPdf {
    private static final String PLANTILLA_OCUPACION =
            "/reportes/detalle/ocupacion_evento.jasper";
    private static final String PLANTILLA_INGRESOS =
            "/reportes/detalle/ingresos_evento.jasper";
    private static final String LOGO =
            "/reportes/detalle/logo.png";

    public byte[] generarOcupacion(int idEvento, Connection conexion)
            throws JRException, SQLException {
        return exportar(PLANTILLA_OCUPACION, idEvento, conexion);
    }

    public byte[] generarIngresos(int idEvento, Connection conexion)
            throws JRException, SQLException {
        return exportar(PLANTILLA_INGRESOS, idEvento, conexion);
    }

    private byte[] exportar(String plantilla, int idEvento, Connection conexion)
            throws JRException, SQLException {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("id_evento", idEvento);
        parametros.put("logo", cargarLogo());
        try (InputStream recurso = ReporteDetalleEventoPdf.class
                .getResourceAsStream(plantilla)) {
            if (recurso == null) {
                throw new JRException("No se encontró la plantilla " + plantilla);
            }
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    recurso, parametros, conexion);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (IOException ex) {
            throw new JRException("No se pudo cerrar la plantilla " + plantilla, ex);
        }
    }

    private Image cargarLogo() throws JRException {
        try (InputStream recurso = ReporteDetalleEventoPdf.class
                .getResourceAsStream(LOGO)) {
            if (recurso == null) {
                throw new JRException("No se encontró el logo " + LOGO);
            }
            return ImageIO.read(recurso);
        } catch (IOException ex) {
            throw new JRException("No se pudo cargar el logo " + LOGO, ex);
        }
    }
}
