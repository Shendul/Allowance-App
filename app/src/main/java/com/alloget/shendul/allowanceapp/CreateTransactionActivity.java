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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public class CreateTransactionActivity extends AppCompatActivity {

    private static final String TAG = "CreateTransActivity";
    EditText mTransactionAmount;
    private DatabaseReference mDatabase;
    String mTransID = "";
    String mAllowTotal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transaction);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTransactionAmount = findViewById(R.id.amountText);

        // get current datetime.
        Date currentTime = Calendar.getInstance().getTime();
        final long currentTimeMils = currentTime.getTime();
        Log.d(TAG, "Current time: " + currentTimeMils);

        mTransactionAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(15,2)});

        final String userEmail = getIntent().getStringExtra("EMAIL");
        final String allowanceID = getIntent().getStringExtra("ALLOWANCE_ID");

        DatabaseReference transRef = FirebaseDatabase.getInstance()
                .getReference("allowances/" + allowanceID);

        // Read from the database
        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String id = (String) dataSnapshot.child("nextTransID").getValue();
                String allowTotal = (String) dataSnapshot.child("total").getValue();
                if (id == null){
                    //TODO: display message.
                    Log.e(TAG, "no transactions found");
                    return;
                }
                Log.d(TAG, "ID is: " + id);
                mTransID = id;
                mAllowTotal = allowTotal;
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

                // create a transaction in the Firebase database
                mDatabase = FirebaseDatabase.getInstance().getReference();

                if (mTransactionAmount.getText().toString().equals("") || mTransID.isEmpty()) {
                    // show error message to user
                } else {
                    // Add a minus sign to the front of mTAmount if subtract Money button was pushed.
                    String mTAmount;
                    if (getIntent().getBooleanExtra("IS_ADD", false)){
                        mTAmount = mTransactionAmount.getText().toString();
                    } else {
                        mTAmount = "-" + mTransactionAmount.getText().toString();
                    }
                    // update allowance total
                    BigDecimal fAllowTotal = new BigDecimal(mAllowTotal);
                    BigDecimal fTransAmount = new BigDecimal(mTAmount);
                    BigDecimal newT = new BigDecimal(0);
                    newT = newT.add(fAllowTotal);
                    newT = newT.add(fTransAmount);
                    String newTotal = "" + newT;
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("total")
                            .setValue(newTotal); // TODO: Add a better formatting.
                    // init who created transaction.
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(mTransID)
                            .child("createdBy")
                            .setValue(userEmail);
                    // set the transaction amount.
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(mTransID)
                            .child("amount")
                            .setValue(mTAmount);
                    // set desc as current time in milliseconds.
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(mTransID)
                            .child("desc")
                            .setValue(currentTimeMils);
                    // set up transaction id.
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
