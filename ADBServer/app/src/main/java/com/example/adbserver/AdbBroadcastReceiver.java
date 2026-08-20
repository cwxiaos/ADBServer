package com.example.adbserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AdbBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 简单的发送方校验（可选但建议开启）：
        // 只允许特定包名（比如 AutoJs6 的包名）触发命令执行，
        // 防止设备上其他恶意 App 也能给你发指令。
        // String sender = intent.getPackage(); // 需要 AutoJs6 用显式 Intent 发送才有值
        // if (sender == null || !sender.equals("org.autojs.autojs6")) return;

        String cmd = intent.getStringExtra("cmd");
        if (cmd == null || cmd.trim().isEmpty()) {
            return;
        }

        Intent serviceIntent = new Intent(context, AdbForegroundService.class);
        serviceIntent.putExtra("cmd", cmd);

        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
