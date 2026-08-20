package com.example.adbserver;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdbLogger {

    private static final String TAG = "ADBServer";
    private static final String LOG_FILE_NAME = "adbserver.log";
    // 日志文件超过这个大小就清空重写，避免无限增长。
    private static final long MAX_LOG_SIZE_BYTES = 256 * 1024;

    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);

    public static void d(Context context, String msg) {
        Log.d(TAG, msg);
        writeToFile(context, "D", msg);
    }

    public static void e(Context context, String msg, Throwable t) {
        Log.e(TAG, msg, t);
        String extra = t != null ? (msg + " | " + t.getClass().getSimpleName() + ": " + t.getMessage()) : msg;
        writeToFile(context, "E", extra);
    }

    private static synchronized void writeToFile(Context context, String level, String msg) {
        try {
            File f = new File(context.getFilesDir(), LOG_FILE_NAME);
            if (f.exists() && f.length() > MAX_LOG_SIZE_BYTES) {
                f.delete();
            }
            String line = TIME_FORMAT.format(new Date()) + " " + level + " " + msg + "\n";
            try (FileWriter fw = new FileWriter(f, true)) {
                fw.write(line);
            }
        } catch (Exception ignored) {
            // 日志本身写失败就算了，不能因为记日志又崩溃。
        }
    }

    public static String readLog(Context context) {
        File f = new File(context.getFilesDir(), LOG_FILE_NAME);
        if (!f.exists()) {
            return "(还没有任何日志)";
        }
        try {
            byte[] data = new byte[(int) f.length()];
            try (java.io.FileInputStream fis = new java.io.FileInputStream(f)) {
                fis.read(data);
            }
            return new String(data, "UTF-8");
        } catch (Exception e) {
            return "读取日志失败: " + e.getMessage();
        }
    }
}