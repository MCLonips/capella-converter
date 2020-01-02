package de.mclonips.clubadministration.capella.controller;

import de.mclonips.clubadministration.capella.register.DataRegister;
import de.mclonips.commons.io.PathUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tim Franken (FRTI)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CapellaControllerIT {

    private static final Logger logger = LoggerFactory.getLogger(CapellaControllerIT.class);
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    private Path targetFile;

    @BeforeEach
    public void setUp() throws IOException {
        Assertions.assertNotNull(this.wac);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        Assertions.assertNotNull(this.mockMvc);

        this.targetFile = PathUtils.createTempFile("result", ".xlsx");
    }

    @AfterEach
    public void tearDown() throws IOException {
        PathUtils.delete(this.targetFile);
    }

    @Test
    void convertFileOverHTTP() throws Exception {
        final MockMultipartFile multipartFile = this.getMockMultipartFile(DataRegister.CAPELLA_FILE);

        try {
            final MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.multipart("/convert").file(multipartFile))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            final byte[] content = mvcResult.getResponse().getContentAsByteArray();

            PathUtils.write(this.targetFile, content);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }

        Assertions.assertAll(() -> Assertions.assertNotNull(this.targetFile),
                () -> assertTrue(PathUtils.exists(this.targetFile)),
                () -> Assertions.assertTrue(PathUtils.size(this.targetFile) > 0));
    }

    private MockMultipartFile getMockMultipartFile(final DataRegister inputData) {
        final String originalFilename = inputData.getFilename();
        var input = inputData.getInputStream();

        assertNotNull(input);

        MockMultipartFile multipartFile = null;

        try {
            multipartFile = new MockMultipartFile("file", originalFilename, null, input);
        } catch (final IOException ignore) {
        }

        Assertions.assertNotNull(multipartFile);

        return multipartFile;
    }
}
