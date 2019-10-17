package de.mclonips.clubadministration.capella.controller;

import de.mclonips.clubadministration.test.data.DataRegister;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CapellaControllerTest {

    @Autowired
    private CapellaController sut;

    @BeforeEach
    public void setUp(){
        assertNotNull(sut);
    }

    @Test
    void isValid_Null() {
        assertFalse(sut.isValid(null));
    }

    @Test
    void isValid_FilenameNull() {
        final MultipartFile mock = Mockito.mock(MultipartFile.class);

        assertNotNull(mock);

        Mockito.doReturn(null).when(mock).getOriginalFilename();

        assertFalse(sut.isValid(mock));
    }

    @Test
    void isValid_FilenameNoCapx() {
        final MultipartFile mock = Mockito.mock(MultipartFile.class);

        assertNotNull(mock);

        Mockito.doReturn(UUID.randomUUID().toString()+".txt").when(mock).getOriginalFilename();

        assertFalse(sut.isValid(mock));
    }

    @Test
    void isValid() {
        final MultipartFile mock = getMockMultipartFile(DataRegister.CAPELLA_FILE.getFilePath());

        assertTrue(sut.isValid(mock));
    }

    private MockMultipartFile getMockMultipartFile(final Path path) {
        final String originalFilename = path.getFileName().toString();
        return this.getMockMultipartFile(path, originalFilename);
    }

    private MockMultipartFile getMockMultipartFile(final Path path, final String filename) {
        MockMultipartFile multipartFile = null;

        try {
            multipartFile = new MockMultipartFile("file", filename, null, Files.newInputStream(path));
        } catch (IOException ignore) {
        }

        assertNotNull(multipartFile);

        return multipartFile;
    }
}