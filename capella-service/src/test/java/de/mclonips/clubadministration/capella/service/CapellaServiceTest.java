package de.mclonips.clubadministration.capella.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.mclonips.clubadministration.capella.entity.*;
import de.mclonips.clubadministration.capella.entity.type.RepetitionTyp;
import de.mclonips.clubadministration.test.data.DataRegister;
import de.mclonips.commons.io.PathUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CapellaServiceTest {

    @Autowired
    private CapellaService sut;

    @Test
    void extractZipFile_WithNull() {
        final Collection<Path> result = this.sut.extractZipFile(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractZipFile_WithTextFile() throws IOException {

        final Path path = PathUtils.createTempFile("test1.txt");

        assertTrue(PathUtils.exists(path));

        assertThrows(InputMismatchException.class, () -> this.sut.extractZipFile(path));

        PathUtils.delete(path);
    }

    @Test
    void extractZipFile_NoDataFile() throws IOException {
        final Path path = PathUtils.createTempFile("noDataFile.zip");

        final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path));
        zos.putNextEntry(new ZipEntry("insideFile_1.txt"));
        zos.putNextEntry(new ZipEntry("insideFile_2.txt"));
        zos.putNextEntry(new ZipEntry("insideFile_3.txt"));

        zos.close();

        assertTrue(PathUtils.exists(path));

        final Collection<Path> results = this.sut.extractZipFile(path);

        assertNotNull(results);
        assertEquals(3, results.size());
    }

    @Test
    void extractZipFile() throws IOException {
        final Collection<Path> result = this.sut.extractZipFile(DataRegister.CAPELLA_FILE.getFilePath());

        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (final Path path : result) {
            assertTrue(PathUtils.exists(path));
            assertTrue(Files.size(path) > 0);
        }
    }

    @Test
    public void readRepetition() throws IOException, SAXException, ParserConfigurationException {
        final List<Element> result = this.sut.parseXml(DataRegister.REPETITION_XML.getFilePath());
        assertNotNull(result);
        final List<Repetition> reference = Lists.newArrayList(new Repetition(RepetitionTyp.START), new Repetition(RepetitionTyp.END));
        assertEquals(reference.size(), result.size());
        assertTrue(result.containsAll(reference));
    }

    @Test
    void parseXml() throws IOException, SAXException, ParserConfigurationException {
        final List<Element> elements = this.sut.parseXml(DataRegister.SCORE_XML.getFilePath());
        assertNotNull(elements);
        assertFalse(elements.isEmpty());
    }

    @Test
    void readRest() throws IOException, SAXException, ParserConfigurationException {
        final List<Element> result = this.sut.parseXml(DataRegister.REST_XML.getFilePath());
        assertNotNull(result);
        final List<Rest> reference = Lists.newArrayList(new Rest("1/8"), new Rest("1/1"));
        assertEquals(reference.size(), result.size());
        assertTrue(result.containsAll(reference));
    }

    @Test
    void readNote() throws IOException, SAXException, ParserConfigurationException {
        final List<Element> result = this.sut.parseXml(DataRegister.NOTE_XML.getFilePath());
        assertNotNull(result);
        final List<Note> reference = Lists.newArrayList(
                  new Note("1/4", "C6"),
                  new Note("1/4", "D6", true),
                  new Note("1/4", "F6#", true));
        assertEquals(reference.size(), result.size());
        assertTrue(result.containsAll(reference));
    }

    @Test
    void duration_getByLength() {
        final Map<String, Duration> testdata = Maps.newHashMap();
        testdata.put("1/1", Duration.FULL);
        testdata.put("1/2", Duration.HALF);
        testdata.put("1/4", Duration.QUARTER);
        testdata.put("1/8", Duration.QUAVER);
        testdata.put("1/16", Duration.SEMIQUAVER);
        testdata.put("1/32", Duration.DEMISEMIQUAVER);

        testdata.keySet().forEach(length -> {
            final Duration reference = testdata.get(length);

            assertNotNull(reference);

            final Duration result = Duration.getByLength(length);

            assertEquals(reference, result);
        });
    }
}
