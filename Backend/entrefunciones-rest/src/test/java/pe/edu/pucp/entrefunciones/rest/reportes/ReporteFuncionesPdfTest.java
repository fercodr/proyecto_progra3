package pe.edu.pucp.entrefunciones.rest.reportes;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintFrame;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporteFuncionesPdfTest {
    private final ReporteFuncionesPdf generador = new ReporteFuncionesPdf();

    @Test
    void ocupacionConFiltrosConservaLasCifrasDeLaConsulta() throws Exception {
        Funcion funcion = funcionDeReporte();
        String filtros = "Periodo: del 01/11/2026 al 30/11/2026"
                + " | Sede: Centro Cultural | Sala: Sala Azul";

        JasperPrint impresion = generador.prepararOcupacion(
                List.of(funcion), filtros);
        String texto = texto(impresion);

        assertTrue(texto.contains("Ocupación por función"));
        assertTrue(texto.contains(filtros));
        assertTrue(texto.contains("La función de prueba"));
        assertTrue(texto.contains("25"));
        assertTrue(texto.contains("50"));
        assertTrue(texto.contains("50.00"));
        byte[] pdf = generador.generarOcupacion(List.of(funcion), filtros);
        assertPdf(pdf);
        String textoExportado = textoPdf(pdf);
        assertTrue(textoExportado.contains("Ocupación por función"));
        assertTrue(textoExportado.contains("La función de prueba"));
        assertTrue(textoExportado.contains("50.00 %"));
        guardarMuestra("ocupacion-con-datos.pdf", pdf);
    }

    @Test
    void ingresosConFiltrosConservaLasCifrasDeLaConsulta() throws Exception {
        Funcion funcion = funcionDeReporte();
        String filtros = "Periodo: desde 01/11/2026"
                + " | Sede: Centro Cultural | Tipo de evento: Película";

        JasperPrint impresion = generador.prepararIngresos(
                List.of(funcion), filtros);
        String texto = texto(impresion);

        assertTrue(texto.contains("Ingresos y cancelaciones"));
        assertTrue(texto.contains(filtros));
        assertTrue(texto.contains("1,250.50"), texto);
        assertTrue(texto.contains("30"));
        assertTrue(texto.contains("5"));
        byte[] pdf = generador.generarIngresos(List.of(funcion), filtros);
        assertPdf(pdf);
        String textoExportado = textoPdf(pdf);
        assertTrue(textoExportado.contains("Ingresos y cancelaciones"));
        assertTrue(textoExportado.contains("1,250.50"));
        assertTrue(textoExportado.contains("30"));
        assertTrue(textoExportado.contains("5"));
        guardarMuestra("ingresos-con-datos.pdf", pdf);
    }

    @Test
    void ambosReportesSeGeneranSinFechasYSinResultados() throws Exception {
        String filtrosOcupacion =
                "Periodo: todo | Sede: Todas | Sala: Todas";
        String filtrosIngresos =
                "Periodo: todo | Sede: Todas | Tipo de evento: Todos";

        String ocupacion = texto(generador.prepararOcupacion(
                List.of(), filtrosOcupacion));
        String ingresos = texto(generador.prepararIngresos(
                List.of(), filtrosIngresos));

        assertTrue(ocupacion.contains(filtrosOcupacion));
        assertTrue(ingresos.contains(filtrosIngresos));
        assertTrue(ocupacion.contains("No se encontraron resultados"));
        assertTrue(ingresos.contains("No se encontraron resultados"));
        byte[] pdfOcupacion = generador.generarOcupacion(
                List.of(), filtrosOcupacion);
        byte[] pdfIngresos = generador.generarIngresos(
                List.of(), filtrosIngresos);
        assertPdf(pdfOcupacion);
        assertPdf(pdfIngresos);
        assertTrue(textoPdf(pdfOcupacion)
                .contains("No se encontraron resultados"));
        assertTrue(textoPdf(pdfIngresos)
                .contains("No se encontraron resultados"));
    }

    private static Funcion funcionDeReporte() {
        Evento evento = new Evento(9, 50.0) {
            @Override
            public String getTitulo() {
                return "La función de prueba";
            }
        };
        Sede sede = new Sede(
                3, "Centro Cultural", "Av. Principal 123",
                "Lima", "999999999");
        Sala sala = new Sala(7, sede, "Sala Azul", 50, "CINE");
        Funcion funcion = new Funcion(
                11, evento, sala, LocalDate.of(2026, 11, 15),
                LocalTime.of(19, 30), LocalTime.of(21, 30), "Español");
        funcion.setEntradasVigentes(25);
        funcion.setPorcentajeOcupacion(new BigDecimal("50.00"));
        funcion.setIngresosConfirmados(new BigDecimal("1250.50"));
        funcion.setEntradasEmitidas(30);
        funcion.setEntradasCanceladas(5);
        return funcion;
    }

    private static void assertPdf(byte[] contenido) {
        assertFalse(contenido.length == 0);
        assertEquals("%PDF-", new String(
                contenido, 0, 5, StandardCharsets.US_ASCII));
    }

    private static void guardarMuestra(String nombre, byte[] contenido)
            throws Exception {
        if (!Boolean.getBoolean("reportes.generarMuestras")) {
            return;
        }
        Path directorio = Path.of("target", "reportes-prueba");
        Files.createDirectories(directorio);
        Files.write(directorio.resolve(nombre), contenido);
    }

    private static String textoPdf(byte[] contenido) throws Exception {
        PdfReader lector = new PdfReader(contenido);
        try {
            return new PdfTextExtractor(lector).getTextFromPage(1);
        } finally {
            lector.close();
        }
    }

    private static String texto(JasperPrint impresion) {
        List<String> textos = new ArrayList<>();
        for (JRPrintPage pagina : impresion.getPages()) {
            extraer(pagina.getElements(), textos);
        }
        return String.join(" ", textos);
    }

    private static void extraer(List<JRPrintElement> elementos,
                                List<String> textos) {
        for (JRPrintElement elemento : elementos) {
            if (elemento instanceof JRPrintText texto) {
                textos.add(texto.getFullText());
            } else if (elemento instanceof JRPrintFrame marco) {
                extraer(marco.getElements(), textos);
            }
        }
    }
}
