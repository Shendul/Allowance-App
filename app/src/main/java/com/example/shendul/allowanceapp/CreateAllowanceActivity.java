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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAllowanceActivity extends AppCompatActivity {

    private static final String TAG = "CreateAllowanceActivity";
    EditText mAllowanceName;
    EditText mAllowanceAmount;
    private DatabaseReference mDatabase;

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

                mDatabase = FirebaseDatabase.getInstance().getReference();
                String user = getIntent().getStringExtra("USER_NAME");
                // TODO: there is a bug that if the google user has the same exact name it will infact
                // treat them as one user. Also if an allowance is created with the same name it will
                // overwrite the previous one, this may not be so much a bug but a feature, depending
                // on if we want that to be part of the design.

                // TODO: add some checking.
                if (mAllowanceAmount.getText().toString().equals("")) {
                    // show error message
                } else {
                    mDatabase.child(user).child("allowance")
                            .child(mAllowanceName.getText().toString())
                            .setValue(mAllowanceAmount.getText().toString());

                    // once the allowance is created, exit the activity.
                    finish();
                }
            }
        });
    }

}
