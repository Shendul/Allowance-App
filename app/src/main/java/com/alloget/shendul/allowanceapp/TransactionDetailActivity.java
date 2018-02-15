package com.alloget.shendul.allowanceapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TransactionDetailActivity extends AppCompatActivity {

    private static final String TAG = "TransDetailActivity";
    EditText mTransactionDesc;
    EditText mTransactionAmount;
    TextView mTransactionCreatedBy;
    TextView mTransactionLastEditedBy;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTransactionDesc = findViewById(R.id.transaction_desc);
        mTransactionAmount = findViewById(R.id.transaction_amount);
        mTransactionCreatedBy = findViewById(R.id.created_by);
        mTransactionLastEditedBy = findViewById(R.id.edited_by);

        mTransactionAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(15,2)});

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final String allowanceID =  getIntent().getStringExtra("ALLOWANCE_ID");
        final String transID =  getIntent().getStringExtra("TRANSACTION_ID");
        final String userEmail =  getIntent().getStringExtra("EMAIL");

        DatabaseReference transRef = database.getReference("allowances/" +
                allowanceID + "/transactions/" + transID);
        // Read from the database
        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String desc = (String) dataSnapshot.child("desc").getValue();
                String amount = (String) dataSnapshot.child("amount").getValue();
                String createdBy = (String) dataSnapshot.child("createdBy").getValue();
                if (createdBy != null && !createdBy.isEmpty())
                    createdBy = FirebaseEncodingAndDecoding.decodeFromFirebaseKey(createdBy);
                String lastEditedBy = (String) dataSnapshot.child("lastEditedBy").getValue();
                if (lastEditedBy != null && !lastEditedBy.isEmpty())
                    lastEditedBy = FirebaseEncodingAndDecoding.decodeFromFirebaseKey(lastEditedBy);
                if (desc == null || amount == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                Log.d(TAG, "Amount is: " + amount);
                Log.d(TAG, "Description is: " + desc);
                Log.d(TAG, "Created by: " + createdBy);
                Log.d(TAG, "Last edited by: " + lastEditedBy);
                mTransactionAmount.setText(amount);
                mTransactionDesc.setText(desc);
                if (createdBy != null)
                    mTransactionCreatedBy.setText("Created by: " + createdBy);
                if(lastEditedBy != null)
                    mTransactionLastEditedBy.setText("Last Edited by: " + lastEditedBy);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // Make sure that the Original Goal transaction cannot be deleted.
        if (transID.equals("k0")) {
            Button delete = findViewById(R.id.delete_trans_button);
            delete.setVisibility(View.GONE);
        }
        findViewById(R.id.delete_trans_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked Delete Transaction button");
                AlertDialog.Builder builder = new AlertDialog.Builder(TransactionDetailActivity.this);
                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        Log.d(TAG, "Clicked ok to delete");
                        mDatabase = FirebaseDatabase.getInstance().getReference();

                        // remove transaction from the database.
                        mDatabase.child("allowances")
                                .child(allowanceID)
                                .child("transactions")
                                .child(transID)
                                .removeValue();

                        // once the transaction has been deleted, exit the activity.
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Log.d(TAG, "Clicked Cancel Delete Transaction button");
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

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
                String amount = mTransactionAmount.getText().toString();

                if (amount.equals("")) {
                    // show error message to user
                } else if (desc.equals("Desc") || desc.equals("")) {
                    // show error message to user.
                } else {
                    // update last edited by.
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("transactions")
                            .child(transID)
                            .child("lastEditedBy")
                            .setValue(userEmail);
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
