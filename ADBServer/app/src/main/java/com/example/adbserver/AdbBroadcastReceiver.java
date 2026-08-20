package com.example.adbserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AdbBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AdbLogger.d(context, "onReceive 被触发，action=" + intent.getAction());

        // 简单的发送方校验（可选但建议开启）：
        // 只允许特定包名（比如 AutoJs6 的包名）触发命令执行，
        // 防止设备上其他恶意 App 也能给你发指令。
        // String sender = intent.getPackage(); // 需要 AutoJs6 用显式 Intent 发送才有值
        // if (sender == null || !sender.equals("org.autojs.autojs6")) return;

        String cmd = intent.getStringExtra("cmd");
        AdbLogger.d(context, "拿到 cmd extra = [" + cmd + "]");

        if (cmd == null || cmd.trim().isEmpty()) {
            AdbLogger.d(context, "cmd 为空，直接返回，不会拉起 Service");
            return;
        }

        Intent serviceIntent = new Intent(context, AdbForegroundService.class);
        serviceIntent.putExtra("cmd", cmd);

        try {
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(serviceIntent);
                AdbLogger.d(context, "已调用 startForegroundService");
            } else {
                context.startService(serviceIntent);
                AdbLogger.d(context, "已调用 startService");
            }
        } catch (Exception e) {
            // Android 12+ 后台启动前台服务有严格限制，这里捕获一下，
            // 免得静默失败看不出原因。
            AdbLogger.e(context, "启动 Service 失败", e);
        }
    }
}