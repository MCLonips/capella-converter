package de.mclonips.clubadministration.capella.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Tim Franken (FRTI)
 */
@Controller
public class CapellaGuiController  {
    @GetMapping(
              path = "/convert",
              produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String convert() {
        return "convert";
    }

}
