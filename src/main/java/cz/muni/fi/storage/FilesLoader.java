package cz.muni.fi.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesLoader {
    private static final String ORGANISATION = "ORG";
    private static final Path CERTIFICATES_PATH = Paths.get("src/main/resources/organisation_certificates.json");
    private static final Path PRIVATE_KEY_PATH = Paths.get("src/main/resources/signing_key.pem");
    private static final Path FILES_DIRECTORY = Paths.get("src/main/resources/provenance_data");
    private static final String[] FILE_NAMES = {
            "eval.json",
            "preprocEval.json",
            "preprocTrain.json",
            "train.json"
    };
    private static final String TEST_FILE_NAME = "01_sample_acquisition.json";

    public static void registerOrganisation() throws Exception {
        String certificates = Files.readString(CERTIFICATES_PATH);
        Storage.registerOrganisation(ORGANISATION, certificates);
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

        Storage.storeDocumentJava(ORGANISATION, PRIVATE_KEY_PATH, documentName, documentPath);

    }

    public static void retrieveFiles() throws IOException, InterruptedException {
        for (String fileName : FILE_NAMES) {
            retrieveFile(fileName.replace(".json", ""));
        }
    }

    public static String retrieveFile(String name) throws IOException, InterruptedException {
        String document = Storage.retrieveDocument(ORGANISATION, name);
        System.out.println(document);
        return document;
    }
}
