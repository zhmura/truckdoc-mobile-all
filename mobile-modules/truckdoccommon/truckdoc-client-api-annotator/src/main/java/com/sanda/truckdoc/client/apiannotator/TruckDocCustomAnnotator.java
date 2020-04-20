package com.sanda.truckdoc.client.apiannotator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import org.jsonschema2pojo.AbstractAnnotator;

/**
 * @author Alexei Osipov
 */
public class TruckDocCustomAnnotator extends AbstractAnnotator implements org.jsonschema2pojo.Annotator {
    @Override
    public void typeInfo(JDefinedClass clazz, JsonNode schema) {
        super.typeInfo(clazz, schema);

        if (clazz.name().equals("InstructionSetNode")) {
            JAnnotationUse ti = clazz.annotate(JsonTypeInfo.class);
            ti.param("use", JsonTypeInfo.Id.NAME);
            ti.param("include", JsonTypeInfo.As.PROPERTY);
            ti.param("property", "type");


            JAnnotationArrayMember subtypes = clazz.annotate(JsonSubTypes.class).paramArray("value");

            subtypes
                    .annotate(JsonSubTypes.Type.class)
                    .param("name", "leaf")
                    .param("value", clazz.owner().directClass("com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSetLeaf"));

            subtypes
                    .annotate(JsonSubTypes.Type.class)
                    .param("name", "branch")
                    .param("value", clazz.owner().directClass("com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSetBranch"));

        }

    }
}
