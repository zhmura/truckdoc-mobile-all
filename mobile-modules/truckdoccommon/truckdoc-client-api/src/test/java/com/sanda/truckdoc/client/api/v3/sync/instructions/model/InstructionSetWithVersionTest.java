package com.sanda.truckdoc.client.api.v3.sync.instructions.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Alexei Osipov
 */
public class InstructionSetWithVersionTest {

    @Test
    public void testSampleFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        InputStream is = InstructionSetWithVersionTest.class.getResourceAsStream("/instruction_set_sample.json");

        InstructionSetWithVersion val = objectMapper.readValue(is, InstructionSetWithVersion.class);
        Assert.assertNotNull(val);

        is.close();
    }
}