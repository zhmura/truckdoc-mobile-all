package com.sanda.truckdoc.client.to.data;

import android.util.Log;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by k.natallie on 23.02.2016.
 */
public class TOInfoParser {

    public static String covertToJson(TOInfo info) {
        ObjectMapper mapper = new ObjectMapper();
        String result = null;
        try {
            result = mapper.writeValueAsString(info);

            // JsonNode rootNode = mapper.createObjectNode(); // will be of type ObjectNode
            // ((ObjectNode) rootNode).put("to", info.getItems());

        } catch (IOException ex) {
            Log.e(TOInfoParser.class.getName(), ex.getMessage());
        }
        Log.e(TOInfoParser.class.getName(), result + " ");
        return result;
    }

}
