package com.example.shendul.allowanceapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateAllowanceActivity extends AppCompatActivity {

    private static final String TAG = "CreateAllowanceActivity";
    EditText mAllowanceName;
    EditText mAllowanceAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_allowance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAllowanceName = (EditText)findViewById(R.id.nameText);
        mAllowanceAmount = (EditText)findViewById(R.id.amountText);

        findViewById(R.id.createAllowance_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked Create Allowance button");
                Log.d(TAG, mAllowanceName.getText().toString());
                Log.d(TAG, mAllowanceAmount.getText().toString());
                // create an allowance in the Firebase database
            }
        });
    }

}
