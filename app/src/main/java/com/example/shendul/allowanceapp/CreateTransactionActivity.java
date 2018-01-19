package com.example.shendul.allowanceapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateTransactionActivity extends AppCompatActivity {

    private static final String TAG = "CreateTransActivity";
    EditText mTransactionAmount;
    EditText mDescription;
    private DatabaseReference mDatabase;
    String mTransID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transaction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTransactionAmount = (EditText)findViewById(R.id.amountText);
        mDescription = (EditText)findViewById(R.id.descText);

        final String user = getIntent().getStringExtra("USER_NAME");
        final String allowanceID = getIntent().getStringExtra("ALLOWANCE_ID");

        // grab all transactions from firebase and get their sum.
        DatabaseReference transRef = FirebaseDatabase.getInstance()
                .getReference("allowances/" + allowanceID + "/nextTransID");

        // Read from the database
        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String id = (String) dataSnapshot.getValue();
                if (id == null){
                    //TODO: display message.
                    Log.e(TAG, "no transactions found");
                    return;
                }
                Log.d(TAG, "ID is: " + id);
                mTransID = id;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        findViewById(R.id.create_trans_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked Create Transaction button");
                Log.d(TAG, "Amount = " + mTransactionAmount.getText().toString());
                Log.d(TAG, "Description is: " + mDescription.getText().toString());

                // create a transaction in the Firebase database

                mDatabase = FirebaseDatabase.getInstance().getReference();
                String desc = mDescription.getText().toString();

                //TODO: maybe add in which user created the transaction since sharing is now allowed.

                if (mTransactionAmount.getText().toString().equals("") || mTransID.isEmpty()) {
                    // show error message to user
                } else if (desc.equals("")) {
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(mTransID)
                            .child("createdBy")
                            .setValue(user);
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(mTransID)
                            .child("amount")
                            .setValue(mTransactionAmount.getText().toString());
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(mTransID)
                            .child("desc")
                            .setValue("Transaction: " + mTransID);
                    mTransID = "k" + (Integer.parseInt(mTransID.substring(1)) + 1);
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("nextTransID")
                            .setValue(mTransID);

                    // once the allowance is created, exit the activity.
                    finish();
                } else {
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(mTransID)
                            .child("createdBy")
                            .setValue(user);
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(mTransID)
                            .child("amount")
                            .setValue(mTransactionAmount.getText().toString());
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(mTransID)
                            .child("desc")
                            .setValue(desc);
                    mTransID = "k" + (Integer.parseInt(mTransID.substring(1)) + 1);
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("nextTransID")
                            .setValue(mTransID);

                    // once the allowance is created, exit the activity.
                    finish();
                }

            }
        });
    }

}
