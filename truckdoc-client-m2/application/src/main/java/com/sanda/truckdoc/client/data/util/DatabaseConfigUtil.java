package com.sanda.truckdoc.client.data.util;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

/**
 * @author Alexei Osipov
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    public static void main(String[] args) throws Exception {
        writeConfigFile("ormlite_config.txt");
    }
}
