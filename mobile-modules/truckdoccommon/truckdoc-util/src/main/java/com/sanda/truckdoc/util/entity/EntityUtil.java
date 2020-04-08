package com.sanda.truckdoc.util.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexei Osipov
 */
public class EntityUtil {
    private EntityUtil() {
    }

    public static <ID extends Number> List<Integer> getIntegerEntityIds(Collection<? extends EntityWithId<ID>> entities) {
        List<Integer> ids = new ArrayList<Integer>(entities.size());
        for (EntityWithId<ID> entity : entities) {
            // TODO: Get rid of that .intValue()
            ids.add(entity.getId().intValue());
        }
        return ids;
    }

    public static <ID> List<ID> getEntityIds(Collection<? extends EntityWithId<ID>> entities) {
        List<ID> ids = new ArrayList<ID>(entities.size());
        for (EntityWithId<ID> entity : entities) {
            ids.add(entity.getId());
        }
        return ids;
    }

    public static <ID> Set<ID> getEntityIdsSet(Collection<? extends EntityWithId<ID>> entities) {
        Set<ID> ids = new HashSet<ID>(entities.size());
        for (EntityWithId<ID> entity : entities) {
            ids.add(entity.getId());
        }
        return ids;
    }

    public static <ID, Entity extends EntityWithId<ID>> Map<ID, Entity> getIdMap(Iterable<Entity> collection) {
        Map<ID, Entity> result = new HashMap<ID, Entity>();
        for (Entity entity : collection) {
            result.put(entity.getId(), entity);
        }
        return result;
    }

    public static <ID, Entity extends EntityWithId<ID>> List<Entity> getByIdFromMap(Collection<ID> ids, Map<ID, Entity> entityByIdMap) {
        List<Entity> result = new ArrayList<>(ids.size());
        for (ID id : ids) {
            Entity value = entityByIdMap.get(id);
            if (value == null) {
                throw new IllegalArgumentException("Id=" + id + " not found in provided map");
            }
            result.add(value);
        }
        return result;
    }

    public static <ID, Entity extends EntityWithId<ID>> List<Entity> filterById(Collection<ID> ids, Iterable<Entity> collection) {
        Map<ID, Entity> idMap = getIdMap(collection);
        return getByIdFromMap(ids, idMap);
    }

    public static <ID, Entity extends EntityWithId<ID>> List<Entity> orderByIds(List<ID> ids, Iterable<Entity> collection) {
        Map<ID, Entity> idMap = EntityUtil.getIdMap(collection);
        List<Entity> result = getByIdFromMap(ids, idMap);
        if (result.size() != ids.size()) {
            throw new AssertionError("Result size should match id count");
        }
        return result;
    }
}
