package com.example.adbserver;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText(
                "ADBServer ready.\n\n" +
                "从其他 App（如 AutoJs6）发送广播即可执行 adb 命令：\n\n" +
                "action: com.example.adbserver.RUN_COMMAND\n" +
                "extra:  cmd = \"<adb 的参数，例如 version>\"\n\n" +
                "结果通过广播返回：\n" +
                "action: com.example.adbserver.RESULT\n" +
                "extra:  result（短结果）或 result_file（长结果的文件路径）"
        );
        tv.setPadding(48, 120, 48, 48);
        tv.setTextIsSelectable(true);
        setContentView(tv);
    }
}
