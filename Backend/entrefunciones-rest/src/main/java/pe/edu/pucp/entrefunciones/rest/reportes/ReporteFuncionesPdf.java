package pe.edu.pucp.entrefunciones.rest.reportes;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Genera los PDF de reportes a partir de la misma lista que alimenta la tabla. */
public final class ReporteFuncionesPdf {
    private static final String PLANTILLA_OCUPACION =
            "/reportes/ocupacion_funcion.jrxml";
    private static final String PLANTILLA_INGRESOS =
            "/reportes/ingresos_cancelaciones.jrxml";
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final ConcurrentHashMap<String, JasperReport> PLANTILLAS =
            new ConcurrentHashMap<>();

    public byte[] generarOcupacion(List<Funcion> funciones, String filtros)
            throws JRException {
        List<Map<String, ?>> filas = funciones.stream()
                .map(ReporteFuncionesPdf::filaOcupacion)
                .toList();
        return exportar(PLANTILLA_OCUPACION,
                "Ocupación por función", filtros, filas);
    }

    public byte[] generarIngresos(List<Funcion> funciones, String filtros)
            throws JRException {
        List<Map<String, ?>> filas = funciones.stream()
                .map(ReporteFuncionesPdf::filaIngresos)
                .toList();
        return exportar(PLANTILLA_INGRESOS,
                "Ingresos y cancelaciones", filtros, filas);
    }

    JasperPrint prepararOcupacion(List<Funcion> funciones, String filtros)
            throws JRException {
        List<Map<String, ?>> filas = funciones.stream()
                .map(ReporteFuncionesPdf::filaOcupacion)
                .toList();
        return llenar(PLANTILLA_OCUPACION,
                "Ocupación por función", filtros, filas);
    }

    JasperPrint prepararIngresos(List<Funcion> funciones, String filtros)
            throws JRException {
        List<Map<String, ?>> filas = funciones.stream()
                .map(ReporteFuncionesPdf::filaIngresos)
                .toList();
        return llenar(PLANTILLA_INGRESOS,
                "Ingresos y cancelaciones", filtros, filas);
    }

    private byte[] exportar(String plantilla, String titulo, String filtros,
                            List<Map<String, ?>> filas) throws JRException {
        return JasperExportManager.exportReportToPdf(
                llenar(plantilla, titulo, filtros, filas));
    }

    private JasperPrint llenar(String plantilla, String titulo, String filtros,
                               List<Map<String, ?>> filas) throws JRException {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("TITULO", titulo);
        parametros.put("FILTROS", filtros);
        parametros.put(JRParameter.REPORT_LOCALE, Locale.US);
        return JasperFillManager.fillReport(
                compilar(plantilla),
                parametros,
                new JRMapCollectionDataSource(filas));
    }

    private JasperReport compilar(String ruta) throws JRException {
        JasperReport compilada = PLANTILLAS.get(ruta);
        if (compilada != null) {
            return compilada;
        }
        try (InputStream recurso = ReporteFuncionesPdf.class
                .getResourceAsStream(ruta)) {
            if (recurso == null) {
                throw new JRException("No se encontró la plantilla " + ruta);
            }
            JasperReport nueva = JasperCompileManager.compileReport(recurso);
            JasperReport previa = PLANTILLAS.putIfAbsent(ruta, nueva);
            return previa == null ? nueva : previa;
        } catch (java.io.IOException ex) {
            throw new JRException("No se pudo cerrar la plantilla " + ruta, ex);
        }
    }

    private static Map<String, ?> filaOcupacion(Funcion funcion) {
        Map<String, Object> fila = datosComunes(funcion);
        fila.put("sala", funcion.getSala().getNombre());
        fila.put("aforo", funcion.getSala().getAforo());
        fila.put("entradasVigentes", valor(funcion.getEntradasVigentes()));
        fila.put("porcentajeOcupacion",
                decimal(funcion.getPorcentajeOcupacion()));
        return fila;
    }

    private static Map<String, ?> filaIngresos(Funcion funcion) {
        Map<String, Object> fila = datosComunes(funcion);
        fila.put("ingresos", decimal(funcion.getIngresosConfirmados()));
        fila.put("entradasEmitidas", valor(funcion.getEntradasEmitidas()));
        fila.put("entradasCanceladas", valor(funcion.getEntradasCanceladas()));
        return fila;
    }

    private static Map<String, Object> datosComunes(Funcion funcion) {
        Map<String, Object> fila = new HashMap<>();
        fila.put("funcion", funcion.getEvento().getTitulo());
        fila.put("fecha", funcion.getFecha().format(FECHA));
        return fila;
    }

    private static Integer valor(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private static BigDecimal decimal(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
