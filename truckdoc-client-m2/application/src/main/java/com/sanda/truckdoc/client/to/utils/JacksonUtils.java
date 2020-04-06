package com.sanda.truckdoc.client.to.utils;

import android.util.Log;

import com.sanda.truckdoc.client.api.v3.sync.checklist.model.ChecklistResult;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by k.natallie on 21.03.2016.
 */
public class JacksonUtils {
    private static ObjectMapper mapper = new ObjectMapper();


    public static String getJsonChecklistResultString(ChecklistResult result) {
        try {
            return mapper.writeValueAsString(result);
        } catch (IOException ex) {
            Log.e("error", ex.getMessage());
        }
        return "";
    }

    public static ChecklistResult restoreStartedTo(String jsonString) {
        try {
            return mapper.readValue(jsonString, ChecklistResult.class);
        } catch (IOException ex) {
            Log.e("www", ex.getMessage());
        }
        return null;
    }


}
