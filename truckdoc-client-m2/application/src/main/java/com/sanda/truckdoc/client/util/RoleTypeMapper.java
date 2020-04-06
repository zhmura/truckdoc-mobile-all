package com.sanda.truckdoc.client.util;

import android.content.Context;

import com.sanda.truckdoc.client.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * @author Alexei Osipov
 */
public class RoleTypeMapper {

    @IntDef({IN, OUT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MessageDirection {
    }

    public static final int IN = 1;
    public static final int OUT = 2;

    @IntDef({IN_COLUMN, IN_MECHANIC, IN_EXPEDITER, OUT_COLUMN, OUT_EXPEDITER, OUT_MECHANIC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RoleType {
    }

    @IntDef({OUT_COLUMN, OUT_EXPEDITER, OUT_MECHANIC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RoleTypeIn {
    }

    @IntDef({IN_COLUMN, IN_MECHANIC, IN_EXPEDITER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RoleTypeOut {
    }

    public static final int IN_COLUMN = 101;
    public static final int IN_EXPEDITER = 102;
    public static final int IN_MECHANIC = 103;

    public static final int OUT_COLUMN = 1;
    public static final int OUT_EXPEDITER = 2;
    public static final int OUT_MECHANIC = 3;

    public static String convert(@MessageDirection int direction, Integer roleTypeId, Context context) {
        if (direction == IN) {
            if (roleTypeId == null) {
                return context.getResources().getString(R.string.from_leader);
            }
            if (roleTypeId == IN_COLUMN) {
                return context.getResources().getString(R.string.from_leader);
            }
            if (roleTypeId == IN_EXPEDITER) {
                return context.getResources().getString(R.string.from_expeditor);
            }
            if (roleTypeId == IN_MECHANIC) {
                return context.getResources().getString(R.string.from_mechanic);
            }
            return context.getResources().getString(R.string.from_leader);
        }
        if (direction == OUT) {
            if (roleTypeId == null) {
                return context.getResources().getString(R.string.to_leader);
            }
            if (roleTypeId == OUT_COLUMN) {
                return context.getResources().getString(R.string.to_leader);
            }
            if (roleTypeId == OUT_EXPEDITER) {
                return context.getResources().getString(R.string.to_expeditor);
            }
            if (roleTypeId == OUT_MECHANIC) {
                return context.getResources().getString(R.string.to_mechanic);
            }
            return context.getResources().getString(R.string.to_leader);
        }
        return "";
    }
}
