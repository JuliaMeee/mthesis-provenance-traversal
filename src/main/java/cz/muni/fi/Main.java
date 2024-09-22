package cz.muni.fi;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.fi.storage.FilesLoader;
import cz.muni.fi.storage.Storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws Exception {
        var config = new ObjectMapper().readTree(new File("src/main/resources/config.json"));

        ServiceRegister.storage = new Storage(config.get("storageIP").asText(), config.get("storagePort").asText());
        
        System.out.println(ServiceRegister.storage.retrieveDocument("ORG", "eval"));

    }
}