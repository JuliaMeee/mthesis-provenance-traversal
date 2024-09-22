package cz.muni.fi.interfaces;

import jdk.jshell.spi.ExecutionControl;

import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public interface IStorage {
    /***
     * Registers an organisation with the given certificates.
     * @param organisation - organisation identifier
     * @param clientCertificate - the client certificate of the organisation in pem format
     * @param intermediateCertificates - intermediate certificates in pem format
     * @throws Exception - if an error occurs
     */
    void registerOrganisation(String organisation, String clientCertificate, List<String> intermediateCertificates, int clearancePeriod) throws Exception;

    /***
     * Stores a document in the storage under specified organisation.
     * @param organisation - identifier of the organisation under which document will be stored
     * @param privateKey - the private key of the organisation to be used to sign the document
     * @param document - document identifier
     * @param documentJson - document json
     * @throws Exception - if an error occurs
     */
    void storeDocument(String organisation, PrivateKey privateKey, String document, String documentJson, int clearancePeriod) throws Exception;

    /***
     * Retrieves a document from the storage.
     * @param organisation - identifier of the organisation under which document is stored
     * @param document - document identifier
     * @return - json of the document
     * @throws Exception - if an error occurs
     */
    String retrieveDocument(String organisation, String document) throws Exception;
}
