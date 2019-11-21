package com.servicenet.ls;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;


public class AccessOtpActivity extends AppCompatActivity {

    private EditText firstDigitEditText,secondDigitEditText,thirdDigitEditText,fourthDigitEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_otp);

        Intent intent = getIntent();
        String code = intent.getStringExtra("COUNTRY_CODE");
        String phoneNumber = intent.getStringExtra("PHONE_NUMBER");

        TextView textView = findViewById(R.id.phoneno_textview_id);
        textView.setText("+"+code +" "+phoneNumber);

        firstDigitEditText=findViewById(R.id.first_digit_id);
        secondDigitEditText=findViewById(R.id.second_digit_id);
        thirdDigitEditText=findViewById(R.id.third_digit_id);
        fourthDigitEditText=findViewById(R.id.fourth_digit_id);


        firstDigitEditText.setText("1");
        secondDigitEditText.setText("2");
        thirdDigitEditText.setText("3");
        fourthDigitEditText.setText("4");


        // open login register screen with a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(AccessOtpActivity.this,HomeActivity.class));
                finish();
            }
        }, 3000);




    }
}
