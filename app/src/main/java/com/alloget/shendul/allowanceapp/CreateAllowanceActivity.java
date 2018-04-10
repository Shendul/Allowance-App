package com.alloget.shendul.allowanceapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateAllowanceActivity extends AppCompatActivity {

    private static final String TAG = "CreateAllowanceActivity";
    EditText mAllowanceName;
    EditText mAllowanceAmount;
    private DatabaseReference mDatabase;
    String allowID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_allowance);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAllowanceName = findViewById(R.id.nameText);
        mAllowanceAmount = findViewById(R.id.amountText);

        mAllowanceAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(15,2)});

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference allowIDRef = database.getReference("allowances/nextAllowID");


        // Query for allowance ID
        allowIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allowID = (String) dataSnapshot.getValue();
                Log.d(TAG, "AllowID is: " + allowID);
                if (allowID == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        findViewById(R.id.createAllowance_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked Create Allowance button");
                Log.d(TAG, mAllowanceName.getText().toString());
                Log.d(TAG, mAllowanceAmount.getText().toString());
                // create an allowance in the Firebase database

                mDatabase = FirebaseDatabase.getInstance().getReference();
                String user = getIntent().getStringExtra("USER_NAME");
                String emailAddress = getIntent().getStringExtra("EMAIL");
                try {
                    emailAddress = URLDecoder.decode(emailAddress, "UTF-8");
                } catch (UnsupportedEncodingException exception) {
                    Log.e(TAG, "unsupportedEncodingException thrown and not handled");
                }
                
                if (mAllowanceAmount.getText().toString().equals("")) {
                    // show error message to user
                } else {
                    String zero = "k0";
                    // give user allowance id
                    mDatabase.child(user)
                            .child("allowances")
                            .child(allowID)
                            .setValue("");
                    // create name of allowance
                    mDatabase.child("allowances").child(allowID)
                            .child("name")
                            .setValue(mAllowanceName.getText().toString());
                    // create allowance total
                    mDatabase.child("allowances").child(allowID)
                            .child("total")
                            .setValue(mAllowanceAmount.getText().toString());
                    // create first transaction of allowance
                    mDatabase.child("allowances")
                            .child(allowID)
                            .child("transactions")
                            .child(zero)
                            .child("createdBy")
                            .setValue(emailAddress);
                    mDatabase.child("allowances").child(allowID)
                            .child("transactions")
                            .child(zero)
                            .child("amount")
                            .setValue(mAllowanceAmount.getText().toString());
                    // get current datetime.
                    Date currentTime = Calendar.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    //to convert Date to String, use format method of SimpleDateFormat class.
                    String strDate = dateFormat.format(currentTime);
                    Log.d(TAG, "Current time: " + strDate);
                    mDatabase.child("allowances").child(allowID)
                            .child("transactions")
                            .child(zero)
                            .child("desc")
                            .setValue(strDate); // default 1st transaction
                    // create trans id tracker
                    mDatabase.child("allowances").child(allowID)
                            .child("nextTransID")
                            .setValue("k1");
                    // add creating user to the allowance's users.
                    mDatabase.child("allowances")
                            .child(allowID)
                            .child("users")
                            .child(user)
                            .setValue("");

                    // increment allowID
                    allowID = "k" + (Integer.parseInt(allowID.substring(1)) + 1);
                    mDatabase.child("allowances")
                            .child("nextAllowID")
                            .setValue(allowID);

                    // once the allowance is created, exit the activity.
                    finish();
                }
            }
        });
    }

}
