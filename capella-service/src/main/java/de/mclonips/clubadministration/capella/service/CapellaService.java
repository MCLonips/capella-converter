package de.mclonips.clubadministration.capella.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import de.mclonips.clubadministration.capella.entity.Element;
import de.mclonips.clubadministration.capella.entity.Note;
import de.mclonips.clubadministration.capella.entity.Repetition;
import de.mclonips.clubadministration.capella.entity.Rest;
import de.mclonips.clubadministration.capella.entity.type.RepetitionTyp;
import de.mclonips.commons.io.PathUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

@Service
@Getter
public class CapellaService {

    @Getter(value = AccessLevel.NONE)
    private Logger logger = LoggerFactory.getLogger(CapellaService.class);

    private CapellaService() {

    }

    public Path convert(final Path zipFile) throws ParserConfigurationException, IOException, SAXException {
        //Extract given ZIP-File
        final Collection<Path> extractedPaths = this.extractZipFile(zipFile);

        PathUtils.delete(zipFile);

        //Get 'score.xml'
        final Path inputFile;

        final Optional<Path> optionalPath = extractedPaths
                                                      .stream()
                                                      .filter(p -> p.toString().endsWith("score.xml"))
                                                      .findFirst();

        if (!(optionalPath.isPresent())) {
            throw new IOException();
        }

        inputFile = optionalPath.get();

        final List<Element> elements = this.parseXml(inputFile);

        //Remove extracted files
        PathUtils.delete(extractedPaths);

        return this.writeFile(elements, zipFile.getFileName().toString());
    }

    Collection<Path> extractZipFile(final Path path) {
        if (path == null) {
            return Lists.newArrayList();
        }//if

        try {
            final Path extractionDir = PathUtils.createTempDir();
            return PathUtils.simpleExtract(path, extractionDir);
        } catch (final IOException ioe) {
            this.logger.error(ioe.getMessage(), ioe);
            throw new InputMismatchException("Not able to extract the given path on the local filesystem!");
        }
    }//extractZipFile()

    List<Element> parseXml(final Path file) throws ParserConfigurationException, IOException, SAXException {
        //Configure factory to get DocumentBuilder
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setValidating(false);

        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document document = builder.parse(file.toFile());

        final List<Element> elements = Lists.newArrayList();

        //Get all 'noteObjects' from XML. Every 'noteObjects' is one line in the Capella File
        final NodeList noteObjects = document.getElementsByTagName("noteObjects");

        for (int index = 0; index < noteObjects.getLength(); index++) {
            final Node item = noteObjects.item(index);

            final NodeList childNodes = item.getChildNodes();
            for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {
                final Node childItem = childNodes.item(childIndex);

                final Element element;

                final String nodeName = childItem.getNodeName();
                switch (nodeName) {
                    case "chord":
                        element = this.readNote(childItem);
                        break;
                    case "rest":
                        element = this.readRest(childItem);
                        break;
                    case "barline":
                        element = this.readRepetition(childItem);
                        break;
                    case "keySign":
                        // value = 4 bedeutet 4 kreuze am Anfang de Zeile
                        // Dieser Fall muss nicht beachtet werden, da die Noten im <head>-tag transponiert werden
                        element = null;
                        break;
                    default:
                        this.log("main", nodeName);
                        continue;
                }//switch

                if (element != null) {
                    elements.add(element);
                }//if
            }
        }

        return elements;
    }

    private Path writeFile(final List<Element> elements, final String filename) throws IOException {
        final int defaultColumnWidth = 1;
        final int maxElementesPerRow = 73;

        final Path outputFile = PathUtils.createTempFile(filename);

        //Create the workbook
        try (final Workbook workbook = new XSSFWorkbook()) {
            //Create am sheet and configure it
            final Sheet sheet = workbook.createSheet();
            sheet.setDefaultColumnWidth(defaultColumnWidth);
            sheet.setMargin(Sheet.LeftMargin, 0.3); // Rand links
            sheet.setMargin(Sheet.RightMargin, 0.3); //Rand rechts

            final PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setLandscape(true); //Querformat

            //Set Header Line
            final Header header = sheet.getHeader();
            header.setCenter(HSSFHeader.font("Stencil-Normal", "Italic")
                                       + HSSFHeader.fontSize((short) 20) + filename.split("\\.capx")[0]);

            int i = 0;
            int oldRowindex = 0;

            Row headerRow = sheet.createRow(0);
            Row valueRow = sheet.createRow(1);

            for (final Element element : elements) {

                final int rowindex = (i / maxElementesPerRow) * 2;
                final int cellindex = (i % maxElementesPerRow);

                //Reihe gewechselt..
                if (rowindex > oldRowindex) {
                    oldRowindex = rowindex;
                    headerRow = sheet.createRow(rowindex);
                    valueRow = sheet.createRow(rowindex + 1);
                }

                final Cell headerCell = headerRow.createCell(cellindex);
                final Cell valueCell = valueRow.createCell(cellindex);

                element.addToValueCell(valueCell, workbook.createCellStyle());
                element.addToHeaderCell(headerCell, workbook.createCellStyle());

                i++;
            }

            workbook.write(Files.newOutputStream(outputFile.toAbsolutePath()));
        }

        return outputFile;
    }

    private Element readNote(final Node node) {

        if (node == null) {
            return null;
        }

        String duration = null;
        String pitch = null;
        boolean hasDot = false;

        final NodeList childNodes = node.getChildNodes();
        for (int index = 0; index < childNodes.getLength(); index++) {
            final Node item = childNodes.item(index);

            final String nodeName = item.getNodeName();
            switch (nodeName) {
                case "duration":
                    duration = ((org.w3c.dom.Element) item).getAttribute("base");

                    final String dots = ((org.w3c.dom.Element) item).getAttribute("dots");
                    hasDot = (dots != null && dots.equals("1"));
                    break;
                case "heads":
                    final org.w3c.dom.Element childNode = (org.w3c.dom.Element) item.getFirstChild().getNextSibling();
                    pitch = childNode.getAttribute("pitch");

                    if (childNode.getChildNodes().getLength() != 0) {
                        if (pitch.contains("#")) {
                            this.logger.info(String.format("Hier hätte ich nie landen dürfen! Laenge: %d, Value: %s", childNode.getChildNodes().getLength(), pitch));
                        } else {
                            //try to look if Note has a cross <alter step="1"/>
                            final NodeList cn = childNode.getChildNodes();
                            for (int i = 0; i < cn.getLength(); i++) {
                                final String nodeValue = cn.item(i).getNodeName();
                                if (nodeValue != null && nodeValue.contains("alter")) {
                                    pitch += '#';
                                }
                            }
                        }
                    }

                    break;
                case "drawObjects":
                    //TODO Hier können entweder die Nummer in der Wiederholungsklammer (1, 2, etc.) oder der Text des Lieds stehen
                    break;
                default:
                    this.log("note", nodeName);
            }
        }

        if (Strings.isNullOrEmpty(duration) || Strings.isNullOrEmpty(pitch)) {
            return null;
        }

        return new Note(duration, pitch, hasDot);
    }

    private Element readRest(final Node node) {
        if (node == null) {
            return null;
        }


        String duration = null;

        final NodeList childNodes = node.getChildNodes();
        for (int index = 0; index < childNodes.getLength(); index++) {
            final Node item = childNodes.item(index);

            final String nodeName = item.getNodeName();
            switch (nodeName) {
                case "duration":
                    duration = ((org.w3c.dom.Element) item).getAttribute("base");
                    break;
                case "drawObjects":
                    // TODO Hier könnte auch mal Text stehen
                    break;
                default:
                    this.log("rest", nodeName);
            }//switch
        }//for

        if (Strings.isNullOrEmpty(duration)) {
            return null;
        }

        return new Rest(duration);
    }

    private Repetition readRepetition(final Node node) {
        if (node == null) {
            return null;
        }
        final RepetitionTyp typ;

        final String nodeValue = node.getAttributes().getNamedItem("type").getNodeValue();
        switch (nodeValue) {
            case "repBegin":
                typ = RepetitionTyp.START;
                break;
            case "repEnd":
                typ = RepetitionTyp.END;
                break;
            default:
                this.log("Repetition", nodeValue);
                return null;
        }

        return new Repetition(typ);
    }

    private void log(final String place, final String element) {
        if (element.contains("text") || element.contains("clefSign") ||
                      element.contains("timeSign") || element.contains("beam") || element.contains("double")
                      || element.contains("display") || element.contains("end")) {
            return;
        }

        this.logger.info(String.format("Unknown element at %s: %s!", place, element));
    }
}
