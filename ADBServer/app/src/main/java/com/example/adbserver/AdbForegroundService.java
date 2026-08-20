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
        AdbLogger.d(this, "Service onStartCommand 被调用");
        startForegroundCompat();

        String cmd = intent != null ? intent.getStringExtra("cmd") : null;
        AdbLogger.d(this, "Service 拿到 cmd = [" + cmd + "]");

        if (cmd != null) {
            new Thread(() -> runAdbAndReport(cmd)).start();
        } else {
            AdbLogger.d(this, "cmd 为 null，stopSelf");
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void startForegroundCompat() {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
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
                AdbLogger.d(this, "startForeground 成功");
            } catch (Exception e) {
                // Android 12+ 对后台启动前台服务卡得很严，这里是最容易
                // 静默失败的地方，抓出来看具体是什么异常（很可能是
                // ForegroundServiceStartNotAllowedException）。
                AdbLogger.e(this, "startForeground 失败", e);
            }
        }
    }

    private void runAdbAndReport(String cmdArgs) {
        String adbPath = getApplicationInfo().nativeLibraryDir + "/libadb.so";
        StringBuilder output = new StringBuilder();

        File adbFile = new File(adbPath);
        AdbLogger.d(this, "adb 路径 = " + adbPath
                + " exists=" + adbFile.exists()
                + " canExecute=" + adbFile.canExecute()
                + " length=" + adbFile.length());

        try {
            String[] parts = cmdArgs.trim().split("\\s+");
            String[] fullCmd = new String[parts.length + 1];
            fullCmd[0] = adbPath;
            System.arraycopy(parts, 0, fullCmd, 1, parts.length);
            AdbLogger.d(this, "即将执行: " + String.join(" ", fullCmd));

            ProcessBuilder pb = new ProcessBuilder(fullCmd);
            pb.redirectErrorStream(true);
            pb.environment().put("HOME", getFilesDir().getAbsolutePath());
            pb.environment().put("TMPDIR", getCacheDir().getAbsolutePath());
            // adb 需要一个可写目录存放 adbkey（RSA 认证密钥对），
            // 默认会去 $HOME/.android/，指向 App 私有目录避免权限问题。

            Process process = pb.start();
            AdbLogger.d(this, "进程已启动");

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            AdbLogger.d(this, "ADB输出 \n" + output);
            AdbLogger.d(this, "进程结束，exitCode=" + exitCode + "，输出长度=" + output.length());
        } catch (Exception e) {
            AdbLogger.e(this, "执行 adb 时抛出异常", e);
            output.append("ERROR: ").append(e.getClass().getSimpleName())
                    .append(": ").append(e.getMessage());
        }

        String result = output.toString();
        File resultFile = writeResultFile(result);
        broadcastResult(result, resultFile);
        AdbLogger.d(this, "已广播结果，stopSelf");
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