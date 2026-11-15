package pe.edu.pucp.entrefunciones.bl.impl.archivos;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**Sube archivos a Amazon S3 y devuelve su URL pública.
  La configuración se busca igual que en DBManager
 Con AWS Academy ,las credenciales vencen al apagar el lab
 */
public class S3Almacenamiento {

    private final String bucket;
    private final String region;
    private final S3Client cliente;

    public S3Almacenamiento() throws BLException {
        ResourceBundle rb = null;
        try {
            rb = ResourceBundle.getBundle("s3");
        } catch (MissingResourceException ignored) {
        }

        bucket = valor("S3_BUCKET", rb, "bucket", null);
        region = valor("S3_REGION", rb, "region", "us-east-1");
        String accessKey = valor("AWS_ACCESS_KEY_ID", rb, "accessKeyId", null);
        String secretKey = valor("AWS_SECRET_ACCESS_KEY", rb, "secretAccessKey", null);
        String sessionToken = valor("AWS_SESSION_TOKEN", rb, "sessionToken", null);

        if (bucket == null || accessKey == null || secretKey == null) {
            throw new BLException("Falta configurar S3 (bucket y credenciales en s3.properties)");
        }

        // Learner Lab da credenciales temporales: llevan sessionToken
        AwsCredentials credenciales = (sessionToken == null)
                ? AwsBasicCredentials.create(accessKey, secretKey)
                : AwsSessionCredentials.create(accessKey, secretKey, sessionToken);

        cliente = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credenciales))
                .httpClientBuilder(UrlConnectionHttpClient.builder().tlsTrustManagersProvider(S3Almacenamiento::certificadosDelJdk))
                .build();
    }

    /** Sube el archivo con la clave indicada (p. ej. "eventos/3/abc.jpg") y devuelve su URL pública. */
    public String subir(String clave, byte[] contenido, String tipoContenido) throws BLException {
        try {
            PutObjectRequest solicitud = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(clave)
                    .contentType(tipoContenido)
                    .build();

            cliente.putObject(solicitud, RequestBody.fromBytes(contenido));

            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + clave;
        } catch (SdkException e) {
            // Suele ser: credenciales vencidas (se reinició el lab) o bucket mal escrito
            throw new BLException("No se pudo subir la imagen a S3: " + e.getMessage(), e);
        }
    }

    private static String valor(String variable, ResourceBundle rb, String clave, String porDefecto) {
        String v = System.getProperty(variable, System.getenv(variable));
        if ((v == null || v.isBlank()) && rb != null && rb.containsKey(clave)) {
            v = rb.getString(clave);
        }
        return (v == null || v.isBlank()) ? porDefecto : v.trim();
    }
    // GlassFish usa su propio almacén de certificados (que puede estar vacío).
// Aquí cargamos los certificados que trae el JDK para poder conectarnos a AWS por HTTPS.
    private static TrustManager[] certificadosDelJdk() {
        Path cacerts = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
        try (InputStream in = Files.newInputStream(cacerts)) {
            KeyStore almacen = KeyStore.getInstance(KeyStore.getDefaultType());
            almacen.load(in, "changeit".toCharArray());
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(almacen);
            return tmf.getTrustManagers();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron cargar los certificados del JDK", ex);
        }
    }
}
