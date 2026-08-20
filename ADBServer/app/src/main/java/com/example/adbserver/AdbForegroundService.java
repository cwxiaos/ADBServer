package com.example.adbserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class AdbForegroundService extends Service {

    private static final String CHANNEL_ID = "adbserver_channel";
    // 广播 Intent extra 有大小限制（binder 事务约 1MB，实际建议远小于此），
    // 超过这个长度就只广播文件路径，由接收方自己去读文件。
    private static final int INLINE_RESULT_LIMIT = 4000;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundCompat();

        String cmd = intent != null ? intent.getStringExtra("cmd") : null;
        if (cmd != null) {
            new Thread(() -> runAdbAndReport(cmd)).start();
        } else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void startForegroundCompat() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "ADB Server", NotificationManager.IMPORTANCE_MIN);
            nm.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("ADBServer")
                    .setContentText("Running adb command...")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .build();
            startForeground(1, notification);
        }
    }

    private void runAdbAndReport(String cmdArgs) {
        String adbPath = getApplicationInfo().nativeLibraryDir + "/libadb.so";
        StringBuilder output = new StringBuilder();

        try {
            String[] parts = cmdArgs.trim().split("\\s+");
            String[] fullCmd = new String[parts.length + 1];
            fullCmd[0] = adbPath;
            System.arraycopy(parts, 0, fullCmd, 1, parts.length);

            ProcessBuilder pb = new ProcessBuilder(fullCmd);
            pb.redirectErrorStream(true);
            pb.environment().put("HOME", getFilesDir().getAbsolutePath());
            // adb 需要一个可写目录存放 adbkey（RSA 认证密钥对），
            // 默认会去 $HOME/.android/，指向 App 私有目录避免权限问题。

            pb.environment().put("LD_LIBRARY_PATH", getApplicationInfo().nativeLibraryDir);
            // adb 动态链接了随包一起塞进 nativeLibraryDir 的 libcrypto.so.0，
            // 运行时得告诉动态链接器去这个目录找。

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            output.append("ERROR: ").append(e.getClass().getSimpleName())
                    .append(": ").append(e.getMessage());
        }

        String result = output.toString();
        File resultFile = writeResultFile(result);
        broadcastResult(result, resultFile);
        stopSelf();
    }

    private File writeResultFile(String result) {
        File f = new File(getFilesDir(), "adb_result.txt");
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(result);
        } catch (Exception ignored) {
        }
        return f;
    }

    private void broadcastResult(String result, File resultFile) {
        Intent resultIntent = new Intent("com.example.adbserver.RESULT");
        if (result.length() < INLINE_RESULT_LIMIT) {
            resultIntent.putExtra("result", result);
        } else {
            resultIntent.putExtra("result_file", resultFile.getAbsolutePath());
        }
        sendBroadcast(resultIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}