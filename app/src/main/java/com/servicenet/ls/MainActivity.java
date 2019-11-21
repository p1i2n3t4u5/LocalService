package com.servicenet.ls;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // open login register screen with a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(new Intent(MainActivity.this,LoginRegisterActivity.class));
                finish();
            }
        }, 3000);
    }
}
