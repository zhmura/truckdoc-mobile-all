package com.sanda.truckdoc.client.util.timber;

import android.os.Environment;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.StatusPrinter;
import timber.log.Timber;

public class FileLoggingTree implements Timber.Tree {
    public static final String LOG_STORAGE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TruckDoc/logs/";
    private Logger mLogger = LoggerFactory.getLogger(FileLoggingTree.class);
    private static final String LOG_PREFIX = "truckdoc-log";


    public FileLoggingTree() {
        //async here
        final String logDirectory = LOG_STORAGE;
        createFileIfNotExists(logDirectory, true);
        configureLogger(logDirectory);
    }

    private void createFileIfNotExists(String logDirectory, boolean isDirectory) {
        File theFile = new File(logDirectory);
        if (!theFile.exists()) {
            System.out.println("creating file: " + theFile.getName());

            try {
                if (isDirectory) {
                    theFile.mkdir();
                } else {
                    theFile.createNewFile();
                }
            } catch (SecurityException | IOException se) {
                System.out.print(se.getLocalizedMessage());
            }
        }
    }

    private void configureLogger(String logDirectory) {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setFile(logDirectory + "/" + LOG_PREFIX + "-latest.html");
        createFileIfNotExists(logDirectory + "/" + LOG_PREFIX + "-latest.html", false);

        SizeAndTimeBasedFNATP<ILoggingEvent> fileNamingPolicy = new SizeAndTimeBasedFNATP<>();
        fileNamingPolicy.setContext(loggerContext);
        fileNamingPolicy.setMaxFileSize(FileSize.valueOf("1mb"));

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setFileNamePattern(logDirectory + "/" + LOG_PREFIX + ".%d{yyyy-MM-dd}.%i.html");
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(fileNamingPolicy);
        rollingPolicy.setParent(rollingFileAppender);  // parent and context required!
        rollingPolicy.start();

        HTMLLayout htmlLayout = new HTMLLayout();
        htmlLayout.setContext(loggerContext);
        htmlLayout.setPattern("%d{HH:mm:ss.SSS}%level%thread%msg");
        htmlLayout.start();

        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(loggerContext);
        encoder.setLayout(htmlLayout);
        encoder.start();

        // Alternative text encoder - very clean pattern, takes up less space
//        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
//        encoder.setContext(loggerContext);
//        encoder.setCharset(Charset.forName("UTF-8"));
//        encoder.setPattern("%date %level [%thread] %msg%n");
//        encoder.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.start();

        // add the newly created appenders to the root logger;
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        root.addAppender(rollingFileAppender);

        // print any status messages (warnings, etc) encountered in logback config
        StatusPrinter.print(loggerContext);
    }

    public void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE) {
            return;
        }

        String logMessage = tag + ": " + message;
        switch (priority) {
            case Log.DEBUG:
                mLogger.debug(logMessage);
                break;
            case Log.INFO:
                mLogger.info(logMessage);
                break;
            case Log.WARN:
                mLogger.warn(logMessage);
                break;
            case Log.ERROR:
                mLogger.error(logMessage);
                break;
        }
    }

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
        log(2, createTag(), formatString(message, args), null);
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        log(2, createTag(), formatString(message, args), t);
    }

    @Override
    public void d(String message, Object... args) {
        log(3, createTag(), formatString(message, args), null);
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        log(3, createTag(), formatString(message, args), t);
    }

    @Override
    public void i(String message, Object... args) {
        log(4, createTag(), formatString(message, args), null);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        log(4, createTag(), formatString(message, args), t);
    }

    @Override
    public void w(String message, Object... args) {
        log(5, createTag(), formatString(message, args), null);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        log(5, createTag(), formatString(message, args), t);
    }

    @Override
    public void e(String message, Object... args) {
        log(6, createTag(), formatString(message, args), null);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        log(6, createTag(), formatString(message, args), t);
    }

    public void tag(String tag) {
        NEXT_TAG.set(tag);
    }
}