package com.servicenet.ls;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import io.michaelrocks.libphonenumber.android.Phonenumber;

public class LoginRegisterActivity extends AppCompatActivity {

    CountryCodePicker ccp;

    EditText edtPhoneNumber;
    Button loginRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        edtPhoneNumber = (EditText) findViewById(R.id.phone_number_edt);

        ccp.registerPhoneNumberTextView(edtPhoneNumber);




        loginRegisterButton=findViewById(R.id.login_register_btn_id);
        loginRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Phonenumber.PhoneNumber  phoneNumber=  ccp.getPhoneNumber();
                String  countryCode= phoneNumber!=null? phoneNumber.getCountryCode()+"":"NA";
                String phoneNo=phoneNumber!=null?phoneNumber.getNationalNumber()+"":"NA";

                Toast.makeText(LoginRegisterActivity.this,"Phone Number"+phoneNumber.toString(),Toast.LENGTH_SHORT).show();
                Log.d("LocalService", "Phone Number"+phoneNumber.toString());
                Intent intent=new Intent(LoginRegisterActivity.this,AccessOtpActivity.class);
                intent.putExtra("PHONE_NUMBER",phoneNo);
                intent.putExtra("COUNTRY_CODE",countryCode);
                startActivity(intent);
                finish();


            }
        });



    }
}
