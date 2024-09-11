package cz.muni.fi.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.security.Signature;

public class Storage {
    private static final String STORAGE_IP = "172.17.0.3";
    private static final String STORAGE_PORT = "8000";
    private static final int CLEARANCE_PERIOD = 30;

    private static String getOrganisationURL(String organisation) {
        return "http://" + STORAGE_IP + ":" + STORAGE_PORT + "/api/v1/organizations/" + organisation;
    }

    private static String getDocumentURL(String organisation, String document) {
        return getOrganisationURL(organisation) + "/documents/" + document;
    }

    public static void registerOrganisation(String organisation, String certificatesPayloadJson) throws Exception {
        String API_BASE_URL = getOrganisationURL(organisation);
        String response = sendPostRequest(API_BASE_URL, certificatesPayloadJson);

        System.out.println(response);
    }

    public static boolean storeDocumentJava(String organisation, Path privateKeyPath, String documentName, Path documentPath) throws NoSuchAlgorithmException, SignatureException, IOException, InvalidKeySpecException, InvalidKeyException {
        String url = getDocumentURL(organisation, documentName);

        String documentJson = Files.readString(documentPath);
        // documentJson = documentJson.replace("PLACEHOLDER", "172.17.0.3"); // todo remove

        byte[] dataToSign = documentJson.getBytes(StandardCharsets.UTF_8);
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(dataToSign);
        byte[] signedData = signature.sign();

        String encodedDocument = Base64.getEncoder().encodeToString(dataToSign);
        String encodedSignature = Base64.getEncoder().encodeToString(signedData);

        Map<String, Object> payload = new HashMap<>();
        payload.put("document", encodedDocument);
        payload.put("documentFormat", "json");
        payload.put("signature", encodedSignature);
        payload.put("clearancePeriod", CLEARANCE_PERIOD);
        payload.put("createdOn", 123); // was 123 in test file

        String payloadJson = new ObjectMapper().writeValueAsString(payload);

        String response = sendPostRequest(url, payloadJson);

        System.out.println(response);

        return true;

    }

    public static String retrieveDocument(String organisation, String documentName) throws IOException, InterruptedException {
        String url =  getDocumentURL(organisation, documentName);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
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

    private static String sendPostRequest(String url, String json) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(json));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }
}
