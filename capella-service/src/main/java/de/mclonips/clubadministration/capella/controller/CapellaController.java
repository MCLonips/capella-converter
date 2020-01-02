package de.mclonips.clubadministration.capella.controller;

import com.google.common.base.Strings;
import de.mclonips.clubadministration.capella.service.CapellaService;
import de.mclonips.commons.io.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Tim Franken (FRTI)
 */
@RestController
public class CapellaController implements ICapellaController {

    private static final Logger logger = LoggerFactory.getLogger(CapellaController.class);

    private final CapellaService capellaService;

    @Autowired
    public CapellaController(CapellaService capellaService) {
        this.capellaService = capellaService;
    }

    @Override
    public ResponseEntity<ByteArrayResource> convertFile(final MultipartFile file, final RedirectAttributes redirectAttributes) {
        //Check if file is valid
        if (!this.isValid(file)) {
            throw new IllegalArgumentException("Given File is not valid!");
        }

        //Save uploaded data
        final Path uploadPath;
        try {
            uploadPath = PathUtils.createTempFile(PathUtils.getFilename(file.getOriginalFilename()));
            //save file on local filesystem
            PathUtils.write(file.getInputStream(), uploadPath);
        } catch (final IOException ioe) {
            CapellaController.logger.error(ioe.getMessage(), ioe);
            throw new IllegalArgumentException("Not able to save the given file on the local filesystem!", ioe);
        }//try-catch

        //convert the file
        final Path convertedFile;

        try {
            convertedFile = this.capellaService.convert(uploadPath);
        } catch (final ParserConfigurationException | IOException | SAXException e) {
            CapellaController.logger.error(e.getMessage(), e);
            throw new IllegalArgumentException("Not able to convert the given file!", e);
        }

        //Create response...
        final ResponseEntity<ByteArrayResource> result;
        try {
            result = this.createResponse(convertedFile);
        } catch (final IOException ioe) {
            CapellaController.logger.error(ioe.getMessage(), ioe);
            throw new IllegalArgumentException(String.format("Not able to create the result path located at %s", convertedFile));
        } finally {
            //Delete converted file
            PathUtils.deleteQuietly(convertedFile);
        }

        return result;
    }

    @Override
    public String version() {
        //TODO @frti return version
        return null;
    }

    boolean isValid(final MultipartFile file) {
        //Check if File is null
        if (file == null) {
            return false;
        }//if

        //Check if originalFileName is null
        final String originalFileName = file.getOriginalFilename();
        if (Strings.isNullOrEmpty(originalFileName) || !(originalFileName.endsWith(".capx"))) {
            return false;
        }//if

        return !file.isEmpty();
    }

    /**
     * Creates a {@link ResponseEntity} containing the converted file to send it back to the caller.
     *
     * @param path {@link Path path} to the converted file
     *
     * @return {@link ResponseEntity} containing the data of the converted file
     */
    private ResponseEntity<ByteArrayResource> createResponse(final Path path) throws IOException {
        //Create Header
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName().toString().split("\\.capx")[0] + ".xlsx");

        final ByteArrayResource byteArrayResource = new ByteArrayResource(Files.readAllBytes(path));

        //Create Response-Entity
        return ResponseEntity.ok()
                             .headers(headers)
                             .contentLength(byteArrayResource.contentLength())
                             .contentType(MediaType.APPLICATION_OCTET_STREAM)
                             .body(byteArrayResource);
    }
}
