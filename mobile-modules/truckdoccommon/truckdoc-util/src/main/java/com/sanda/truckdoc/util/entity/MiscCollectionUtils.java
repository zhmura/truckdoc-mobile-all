package com.sanda.truckdoc.util.entity;

import java.util.Collection;

/**
 * Some simple collection operations.
 * See {@link org.apache.commons.collections.CollectionUtils}
 *
 * @author Alexei Osipov
 */
public class MiscCollectionUtils {
    private MiscCollectionUtils() {
    }

    public static boolean isEmpty(Collection coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNotEmpty(Collection coll) {
        return !MiscCollectionUtils.isEmpty(coll);
    }
}
