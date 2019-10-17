package de.mclonips.clubadministration.capella;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * @author Tim Franken (FRTI)
 */
@SpringBootApplication
public class CapellaApplication {

    public static void main(final String[] args) throws IOException {
        SpringApplication.run(CapellaApplication.class);

        final Runtime rt = Runtime.getRuntime();
        final String url = "http://localhost:8080/convert";
        rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
    }

}
