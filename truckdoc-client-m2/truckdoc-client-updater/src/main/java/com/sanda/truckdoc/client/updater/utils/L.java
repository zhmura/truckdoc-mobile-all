package com.sanda.truckdoc.client.updater.utils;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

import static timber.log.Timber.Tree;

/**
 * Created by astra on 16.03.14.
 */
public class L {

    /**
     * Log a verbose message with optional format args.
     */
    public static void v(String message, Object... args) {
        Timber.v(message, args);
    }

    public static void v() {
        Timber.v("");
    }

    public static void v(Object o) {
        Timber.v("" + o);
    }

    /**
     * Log a verbose exception and a message with optional format args.
     */
    public static void v(Throwable t, String message, Object... args) {
        Timber.v(t, message, args);
    }

    /**
     * Log a debug message with optional format args.
     */
    public static void d(String message, Object... args) {
        Timber.d(message, args);
    }

    /**
     * Log a debug exception and a message with optional format args.
     */
    public static void d(Throwable t, String message, Object... args) {
        Timber.d(t, message, args);
    }

    /**
     * Log an info message with optional format args.
     */
    public static void i(String message, Object... args) {
        Timber.i(message, args);
    }

    /**
     * Log an info exception and a message with optional format args.
     */
    public static void i(Throwable t, String message, Object... args) {
        Timber.i(t, message, args);
    }

    public static void i() {
        Timber.i("");
    }

    public static void i(Object o) {
        Timber.i(String.valueOf(o));
    }

    /**
     * Log a warning message with optional format args.
     */
    public static void w(String message, Object... args) {
        Timber.w(message, args);
    }

    /**
     * Log a warning exception.
     */
    public static void w(Throwable t) {
        Timber.w(t, "");
    }

    /**
     * Log a warning exception and a message with optional format args.
     */
    public static void w(Throwable t, String message, Object... args) {
        Timber.w(t, message, args);
    }

    public static void w() {
        Timber.w("");
    }

    /**
     * Log an error message with optional format args.
     */
    public static void e(String message, Object... args) {
        Timber.e(message, args);
    }

    /**
     * Log an error exception and a message with optional format args.
     */
    public static void e(Throwable t, String message, Object... args) {
        Timber.e(t, message, args);
    }

    public static void e(Throwable t) {
        Timber.e(t, "");
    }

    /**
     * Set a one-time tag for use on the next logging call.
     */
    public static Tree tag(String tag) {
        return Timber.tag(tag);
    }

    /**
     * Add a new logging tree.
     */
    public static void plant(Tree tree) {
        Timber.plant(tree);
    }

    /**
     * A {@link Tree} for debug builds. Automatically infers the tag from the
     * calling class.
     */
    public static class DebugTree implements Timber.TaggedTree {

        private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
        private static final ThreadLocal<String> NEXT_TAG = new ThreadLocal<>();

        private static String createTag() {
            String tag = NEXT_TAG.get();
            if (tag != null) {
                NEXT_TAG.remove();
                return tag;
            }

            tag = new Throwable().getStackTrace()[5].getClassName();
            Matcher m = ANONYMOUS_CLASS.matcher(tag);
            if (m.find()) {
                tag = m.replaceAll("");
            }
            return tag.substring(tag.lastIndexOf('.') + 1);
        }

        private static String methodName() {
            return new Throwable().getStackTrace()[6].getMethodName();
        }

        private static String line() {
            return String.valueOf(new Throwable().getStackTrace()[6].getLineNumber());
        }

        private static String file() {
            return String.valueOf(new Throwable().getStackTrace()[6].getFileName());
        }

        static String formatString(String message, Object... args) {
            // If no varargs are supplied, treat it as a request to log the
            // string without formatting.
            return methodName() + ":  " + (args.length == 0 ? message : String.format(message, args)) + "(" + file() + ":" + line() + ")";
        }

        @Override
        public void v(String message, Object... args) {
            Log.v(createTag(), formatString(message, args));
        }

        @Override
        public void v(Throwable t, String message, Object... args) {
            Log.v(createTag(), formatString(message, args), t);
        }

        @Override
        public void d(String message, Object... args) {
            Log.d(createTag(), formatString(message, args));
        }

        @Override
        public void d(Throwable t, String message, Object... args) {
            Log.d(createTag(), formatString(message, args), t);
        }

        @Override
        public void i(String message, Object... args) {
            Log.i(createTag(), formatString(message, args));
        }

        @Override
        public void i(Throwable t, String message, Object... args) {
            Log.i(createTag(), formatString(message, args), t);
        }

        @Override
        public void w(String message, Object... args) {
            Log.w(createTag(), formatString(message, args));
        }

        @Override
        public void w(Throwable t, String message, Object... args) {
            Log.w(createTag(), formatString(message, args), t);
        }

        @Override
        public void e(String message, Object... args) {
            Log.e(createTag(), formatString(message, args));
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            Log.e(createTag(), formatString(message, args), t);
        }

        @Override
        public void tag(String tag) {
            NEXT_TAG.set(tag);
        }
    }
}
