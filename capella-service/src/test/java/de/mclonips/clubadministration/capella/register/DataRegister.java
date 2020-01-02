package de.mclonips.clubadministration.capella.register;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Tim Franken (FRTI)
 */
public enum DataRegister {

    CAPELLA_FILE("capella/capellaFile.capx"),
    SCORE_XML("capella/score.xml"),
    REST_XML("capella/rest.xml"),
    NOTE_XML("capella/note.xml"),
    REPETITION_XML("capella/repetition.xml");

    private final Path ROOT = Paths.get("src/test/resources/");
    private final Path filePath;
    private final String resourceName;

    DataRegister(final String filePath) {
        this.resourceName = filePath;
        this.filePath = this.ROOT.resolve(this.resourceName);
    }

    @SneakyThrows
    public InputStream getInputStream() {
        return this.getClass().getClassLoader().getResourceAsStream(this.resourceName);
    }

    public Path getFilePath() {
        return this.filePath;
    }

    public String getFilename() {
        return this.filePath.getFileName().toString();
    }

    public String getFilenameWithoutEnding(){
        String fullName = this.getFilename();
        int indexOfDot = fullName.lastIndexOf('.');
        if (indexOfDot != -1) {
            fullName = fullName.substring(0, indexOfDot);
        }
        return fullName;
    }

}
