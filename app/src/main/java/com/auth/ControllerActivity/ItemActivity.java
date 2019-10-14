package com.auth.ControllerActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        //设置此界面为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init();
    }

   private void init() {
        Button btn_exit = (Button) findViewById(R.id.btn_exit);
        Button btn_update = (Button) findViewById(R.id.btn_update);
        //返回按钮的点击事件
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ItemActivity.this, MainActivity.class));
            }
        });

        //更改密码按钮的点击事件
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ItemActivity.this, UpdateActivity.class));
            }
        });
    }
}

