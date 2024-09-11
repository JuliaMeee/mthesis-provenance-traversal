package cz.muni.fi;

import cz.muni.fi.storage.FilesLoader;
import cz.muni.fi.storage.Storage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);

        // FilesLoader.retrieveFile("train");
        // FilesLoader.retrieveFile("train");
    }
}