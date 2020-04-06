package com.sanda.truckdoc.client.util;

import android.content.Context;
import android.os.Environment;

import com.sanda.truckdoc.client.Consts;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.data.model.ServerMessage;
import com.sanda.truckdoc.client.util.commons.FileUtils;
import com.sanda.truckdoc.client.util.timber.L;

import org.joda.time.DateTime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.sanda.truckdoc.client.util.timber.FileLoggingTree.LOG_STORAGE;

public class FileHelper {

    private static final String ROOT_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String PARENT_DIRECTORY = ROOT_DIRECTORY + "/TruckDoc/";
    public static final String INPUT_DIRECTORY = ROOT_DIRECTORY + "/TruckDoc/Outcome/";
    public static final String OUTPUT_DIRECTORY_PICS = ROOT_DIRECTORY + "/TruckDoc/Income/";
    public static final String OUTPUT_DIRECTORY_DOCS = ROOT_DIRECTORY + "/TruckDoc/Income/";

    public static File getIncomeDirectory(Integer messageId, String extension) {
        File file;
        if (extension.toLowerCase().contains("*")) {
            file = new File(OUTPUT_DIRECTORY_DOCS + messageId);
        } else if (extension.toLowerCase().contains("txt") || extension.toLowerCase().contains("pdf")) {
            file = new File(OUTPUT_DIRECTORY_DOCS + messageId + "/docs/");
        } else {
            file = new File(OUTPUT_DIRECTORY_PICS + messageId + "/pics/");
        }
        if (!file.exists() && !file.mkdirs()) {
            throw new UnexpectedException("Failed to create directory");
        }
        return file;
    }

    public static File getOutcomeDirectory(Long roleType, boolean isForDoc) {
        File file = new File(INPUT_DIRECTORY + (isForDoc ? "docs/" : "landscape/") +
                (roleType == 3 ? "mechanic/" : "") +
                (roleType == 4 ? "epi/" : ""));
        if (!file.exists() && !file.mkdirs()) {
            throw new UnexpectedException("Failed to create directory");
        }
        return file;
    }

    public static File getOutcomeDirectory() {
        File file = new File(INPUT_DIRECTORY);
        if (!file.exists() && !file.mkdirs()) {
            throw new UnexpectedException("Failed to create directory");
        }
        return file;
    }

    public static File getParentDirectory() {
        File file = new File(PARENT_DIRECTORY);
        if (!file.exists() && !file.mkdirs()) {
            throw new UnexpectedException("Failed to create directory");
        }
        return file;
    }

    public static File getOutcomeDirForAccidents(boolean isTemporary) {
        File file = FileUtils.getFile(ROOT_DIRECTORY, "TruckDoc", isTemporary ? "Temp/Accidents" : "Accidents");
        if (!file.exists() && !file.mkdirs()) {
            throw new UnexpectedException("Failed to create directory");
        }
        return file;
    }

    public static void deleteAllAppFiles() {
        File file = FileUtils.getFile(ROOT_DIRECTORY, "TruckDoc");
        if (file.exists() && file.isDirectory()) {
            for (File local : file.listFiles()) {
                local.delete();
            }
        }
    }

    public static File archiveLogFiles() {
        int BUFFER = 2048;
        File logArchive = null;
        try {
            final String logDirectory = LOG_STORAGE;
            BufferedInputStream origin = null;
            String fileName = new SimpleDateFormat("yyyyMMddHHmm'.zip'").format(new Date());

            logArchive = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TruckDoc/" + fileName);
            if (!logArchive.exists()) logArchive.createNewFile();
            FileOutputStream dest = new FileOutputStream(logArchive);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte[] data = new byte[BUFFER];

            File subDir = new File(logDirectory);
            String[] subdirList = subDir.list();
            for (String sd : subdirList) {
                // get a list of files from current directory
                File f = new File(logDirectory + "/" + sd);
                if (f.isDirectory()) {
                    String[] files = f.list();

                    for (String file : files) {
                        System.out.println("Adding: " + file);
                        FileInputStream fi = new FileInputStream(logDirectory + "/" + sd + "/" + file);
                        origin = new BufferedInputStream(fi, BUFFER);
                        ZipEntry entry = new ZipEntry(sd + "/" + file);
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                            out.flush();
                        }
                    }
                } else {
                    FileInputStream fi = new FileInputStream(f);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(sd);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                        out.flush();
                    }

                }
            }
            FileUtils.closeInputStream(origin);
            out.flush();
            FileUtils.closeOutputStream(out);
        } catch (Exception e) {
            L.e(e);
        }

        return logArchive;
    }


    public static void exportMessages(Context context, List<ServerMessage> messages) {
        File truckDoc = new File(ROOT_DIRECTORY, "TruckDoc");
        if (!truckDoc.exists() && !truckDoc.mkdirs()) {
            throw new UnexpectedException("Failed to create directory");
        }
        File target = new File(truckDoc, "messages-" + DateTime.now().toString(Consts.DATE_FORMAT) + ".csv");
        try {
            CSVWriter w = new CSVWriter(new FileWriter(target));
            w.writeNext(context.getString(R.string.csv_target),
                    context.getString(R.string.csv_text),
                    context.getString(R.string.csv_inout),
                    context.getString(R.string.csv_hidden),
                    context.getString(R.string.csv_sent),
                    context.getString(R.string.csv_date));
            for (ServerMessage m : messages) {
                String sender;
                if (m.isOutgoing()) {
                    sender = RoleTypeMapper.convert(RoleTypeMapper.OUT, m.getRecipientId(), context);
                } else {
                    sender = RoleTypeMapper.convert(RoleTypeMapper.IN, m.getSenderRoleId(), context);
                }

                w.writeNext(sender,
                        m.getText(),
                        m.isOutgoing() ? context.getString(R.string.csv_out) : context.getString(R.string.csv_in),
                        m.isHidden() ? context.getString(R.string.csv_is_hidden) : context.getString(R.string.csv_is_not_hidden),
                        m.isSent() ? context.getString(R.string.csv_is_sent) : context.getString(R.string.csv_is_not_sent),
                        m.getSavedDate().toString(Consts.DATE_TIME_FORMAT));
            }
            w.close();
        } catch (IOException e) {
            L.e(e);
        }
    }
}
