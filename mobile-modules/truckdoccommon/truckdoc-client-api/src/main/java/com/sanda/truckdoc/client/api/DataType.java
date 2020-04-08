package com.sanda.truckdoc.client.api;

import com.sanda.truckdoc.util.entity.EntityUtil;
import com.sanda.truckdoc.util.entity.EntityWithId;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Alexei Osipov
 */
public enum DataType implements EntityWithId<Integer> {
    MESSAGE_FOR_CLIENT(1, DeliveryType.NEW_OBJECT_LIST),
    CONTACT_LIST(2, DeliveryType.FULL_LIST);

    private static final Map<Integer, DataType> valueMap = EntityUtil.getIdMap(Arrays.asList(values()));

    private final int id;
    private final DeliveryType deliveryType;

    private DataType(int id, DeliveryType deliveryType) {
        this.id = id;
        this.deliveryType = deliveryType;
    }

    public Integer getId() {
        return id;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public static DataType getById(Integer dataTypeId) {
        return valueMap.get(dataTypeId);
    }

    public static List<DataType> getByIds(Collection<Integer> ids) {
        return EntityUtil.getByIdFromMap(ids, valueMap);
    }

    public enum DeliveryType {
        // Server sends a list of new objects
        NEW_OBJECT_LIST,
        // Server sends a full list of objects
        FULL_LIST,
        // Server sends a single object
        SINGLE_VALUE
    }
}
