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
        InstructionSetBranch node1 = (InstructionSetBranch) val.getInstructionSet().getEntries().get(1);
        InstructionSetLeaf node2 = (InstructionSetLeaf) node1.getEntries().get(0);
        Assert.assertNotNull(node2.getFile());
        Assert.assertNotNull(node2.getFile().getFileId());


        is.close();
    }
}