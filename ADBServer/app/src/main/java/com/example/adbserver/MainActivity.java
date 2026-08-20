package com.example.adbserver;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 80, 32, 32);

        TextView header = new TextView(this);
        header.setText(
                "ADBServer ready.\n\n"
//                        +
//                        "从其他 App（如 AutoJs6）发送广播即可执行 adb 命令：\n" +
//                        "action: com.example.adbserver.RUN_COMMAND\n" +
//                        "extra:  cmd = \"<adb 的参数，例如 version>\"\n\n" +
//                        "结果通过广播返回：\n" +
//                        "action: com.example.adbserver.RESULT\n" +
//                        "extra:  result（短结果）或 result_file（长结果的文件路径）"
        );
        header.setTextIsSelectable(true);
        root.addView(header);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("刷新日志");
        refreshBtn.setOnClickListener(v -> refreshLog());
        root.addView(refreshBtn);

        Button clearBtn = new Button(this);
        clearBtn.setText("清空日志");
        clearBtn.setOnClickListener(v -> {
            new java.io.File(getFilesDir(), "adbserver.log").delete();
            refreshLog();
        });
        root.addView(clearBtn);

        logView = new TextView(this);
        logView.setTextIsSelectable(true);
        logView.setPadding(0, 24, 0, 0);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(logView);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        refreshLog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLog();
    }

    private void refreshLog() {
        logView.setText(AdbLogger.readLog(this));
    }
}