package de.mclonips.clubadministration.capella.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Tim Franken (FRTI)
 */
@Api(value = "CapellaService", description = "Service to convert a valid Capella .capx-file to an excel file")
public interface ICapellaController {

    @PostMapping(
            path = "/convert",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "Converts a given and valid .capx-file to an excel file")
    ResponseEntity<ByteArrayResource> convertFile(@RequestParam("file") final MultipartFile file, final RedirectAttributes redirectAttributes);

    @GetMapping(path = "/version", produces = MediaType.TEXT_PLAIN_VALUE)
    String version();
}
