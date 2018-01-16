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

public class TransactionDetailActivity extends AppCompatActivity {

    private static final String TAG = "TransDetailActivity";
    EditText mTransactionDesc;
    EditText mTransactionAmount;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTransactionDesc = (EditText) findViewById(R.id.transaction_desc);
        mTransactionAmount = (EditText)findViewById(R.id.transaction_amount);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final String allowanceID =  getIntent().getStringExtra("ALLOWANCE_ID");
        Log.d(TAG, "AllowanceID is: " + allowanceID);
        final String transID =  getIntent().getStringExtra("TRANSACTION_ID");
        Log.d(TAG, "TransID is: " + transID);

        DatabaseReference transRef = database.getReference("allowances/" +
                allowanceID + "/transactions/" + transID);
        // Read from the database
        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String desc = (String) dataSnapshot.child("desc").getValue();
                String amount = (String) dataSnapshot.child("amount").getValue();
                if (desc == null || amount == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                Log.d(TAG, "Amount is: " + amount);
                Log.d(TAG, "Description is: " + desc);
                mTransactionAmount.setText("$" + amount);
                mTransactionDesc.setText(desc);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        findViewById(R.id.edit_trans_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked Edit Transaction button");
                Log.d(TAG, mTransactionDesc.getText().toString());
                Log.d(TAG, mTransactionAmount.getText().toString());
                // edit a transaction in the Firebase database.

                mDatabase = FirebaseDatabase.getInstance().getReference();
                String desc = mTransactionDesc.getText().toString();
                String amount = mTransactionAmount.getText().toString().substring(1);

                if (amount.equals("")) {
                    // show error message to user TODO: maybe have this delete the transaction.
                } else if (desc.equals("Desc") || desc.equals("")) {
                    // show error message to user.
                } else {
                    // update transaction amount.
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(transID)
                            .child("amount")
                            .setValue(amount);
                    // update transaction description.
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(transID)
                            .child("desc")
                            .setValue(desc);

                    // once the transaction has been edited, exit the activity.
                    finish();
                }
            }
        });

    }

}
