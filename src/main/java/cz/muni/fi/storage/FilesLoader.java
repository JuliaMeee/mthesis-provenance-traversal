package cz.muni.fi.storage;

import cz.muni.fi.ServiceRegister;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class FilesLoader {
    private static final String ORGANISATION = "ORG";
    private static final Path CLIENT_CERTIFICATE_PATH = Paths.get("src/main/resources/certificates/client_certificate.pem");
    private static final List<Path> INTERMEDIATE_CERTIFICATE_PATHS = List.of(
            Paths.get("src/main/resources/certificates/intermediate_certificate_1.pem"),
            Paths.get("src/main/resources/certificates/intermediate_certificate_2.pem")
    );
    private static final Path PRIVATE_KEY_PATH = Paths.get("src/main/resources/signing_key.pem");
    private static final Path FILES_DIRECTORY = Paths.get("src/main/resources/provenance_data");
    private static final List<String> FILE_NAMES = List.of(
            "eval.json",
            "preprocEval.json",
            "preprocTrain.json",
            "train.json"
    );
    private static final int CLEARANCE_PERIOD = 30;

    public static void registerOrganisation() throws Exception {
        String clientCertificate = new String(Files.readAllBytes(CLIENT_CERTIFICATE_PATH));
        List<String> intermediateCertificates = INTERMEDIATE_CERTIFICATE_PATHS.stream()
                .map(path -> {
                    try {
                        return new String(Files.readAllBytes(path));
                    } catch (IOException e) {
                        return null;
                    }
                })
                .toList();

        ServiceRegister.storage.registerOrganisation(ORGANISATION, clientCertificate, intermediateCertificates, CLEARANCE_PERIOD);
    }

    public static Path getFilePath(String fileName){
        return Paths.get(FILES_DIRECTORY + "/" + fileName);
    }

    public static void storeFiles() throws Exception {

        for (String fileName : FILE_NAMES) {
            storeFile(fileName.replace(".json", ""), getFilePath(fileName));
        }
    }

    public static void storeFile(String documentName, Path documentPath) throws Exception {

        String documentJson = Files.readString(documentPath);

        PrivateKey privateKey = loadPrivateKey(PRIVATE_KEY_PATH);

        ServiceRegister.storage.storeDocument(ORGANISATION, privateKey, documentName, documentJson, CLEARANCE_PERIOD);

    }

    public static void retrieveFiles() throws Exception{
        for (String fileName : FILE_NAMES) {
            retrieveFile(fileName.replace(".json", ""));
        }
    }

    public static String retrieveFile(String name) throws Exception {
        return ServiceRegister.storage.retrieveDocument(ORGANISATION, name);
    }

    private static PrivateKey loadPrivateKey(Path keyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String key = Files.readString(keyPath);

        key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
