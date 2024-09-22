package cz.muni.fi.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.fi.interfaces.IStorage;
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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.Signature;

public class Storage implements IStorage {
    private final String ip;
    private final String port;

    public Storage(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    private String getOrganisationURL(String organisation) {
        return "http://" + ip + ":" + port + "/api/v1/organizations/" + organisation;
    }

    private String getDocumentURL(String organisation, String document) {
        return getOrganisationURL(organisation) + "/documents/" + document;
    }

    public void registerOrganisation(String organisation, String clientCertificate, List<String> intermediateCertificates, int clearancePeriod) throws Exception {
        String API_BASE_URL = getOrganisationURL(organisation);

        Map<String, Object> jsonMap = Map.of(
                "clientCertificate", clientCertificate,
                "intermediateCertificates", intermediateCertificates,
                "clearancePeriod", clearancePeriod
        );

        String payload = new ObjectMapper().writeValueAsString(jsonMap);

        sendPostRequest(API_BASE_URL, payload);
    }

    public void storeDocument(String organisation, PrivateKey privateKey, String document, String documentJson, int clearancePeriod) throws NoSuchAlgorithmException, SignatureException, IOException, InvalidKeySpecException, InvalidKeyException {
        String url = getDocumentURL(organisation, document);

        byte[] dataToSign = documentJson.getBytes(StandardCharsets.UTF_8);

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
        payload.put("clearancePeriod", clearancePeriod);
        payload.put("createdOn", System.currentTimeMillis());

        String payloadJson = new ObjectMapper().writeValueAsString(payload);

        sendPostRequest(url, payloadJson);
    }

    public String retrieveDocument(String organisation, String document) throws IOException, InterruptedException {
        String url =  getDocumentURL(organisation, document);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    private String sendPostRequest(String url, String json) throws IOException {
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
