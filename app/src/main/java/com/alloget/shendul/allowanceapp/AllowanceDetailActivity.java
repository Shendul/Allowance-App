package com.alloget.shendul.allowanceapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// TODO: BUG_LIST:
// 1. When transactions are edited this screen does not always update without a refresh. - think this has to do with the speed of writing to the database and then reading back.
// 2. First time clicking an allowance seems to not populate the transaction list. #POSSIBLY SQUASHED, REQ MORE TESTING

public class AllowanceDetailActivity extends AppCompatActivity {

    private static final String TAG = "AllowanceDetailActivity";
    TextView mAllowanceName;
    TextView mAllowanceBalance;
    private DatabaseReference mDatabase;
    ArrayList<String> transArray = new ArrayList<>();
    ArrayList<String> transDescArray = new ArrayList<>();
    ArrayList<String> usersToRemoveFromAllowanceArray = new ArrayList<>();
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allowance_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAllowanceName = findViewById(R.id.allowance_name);
        mAllowanceBalance = findViewById(R.id.allowance_balance);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        String allowanceName = getIntent().getStringExtra("ALLOWANCE_NAME");
        final String allowanceID =  getIntent().getStringExtra("ALLOWANCE_ID");
        mAllowanceName.setText(allowanceName);


        adapter = new ArrayAdapter<>(this,
                R.layout.allowance_listview, transDescArray);
        ListView listView = findViewById(R.id.transactions_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                String id = transArray.get(position);
                Log.e(TAG, "Clicked " + id);
                startTransactionDetailActivity(id);

            }
        });

        // grab all transactions from firebase and get their sum.
        DatabaseReference transRef = database.getReference("allowances/" +
                allowanceID + "/transactions");

        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BigDecimal sum = new BigDecimal(0);
                HashMap<String, String> value = (HashMap<String, String>) dataSnapshot.getValue();
                transDescArray.clear();
                transArray.clear();
                if (value == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                transArray.addAll(value.keySet());
                Log.d(TAG, "Value is: " + value);
                for (int i = 0; i <  transArray.size(); i++) {
                    String transDesc = (String) dataSnapshot
                            .child(transArray.get(i) + "/desc").getValue();
                    String transAmount = (String) dataSnapshot
                            .child( transArray.get(i) + "/amount").getValue();
                    Log.d(TAG, "Transaction amount is: " +  transAmount);
                    Log.d(TAG, "Transaction desc is: " +  transDesc);
                    if ( transAmount == null || transDesc == null) {
                        //TODO: display message.
                        Log.e(TAG, "Database is empty");
                        return;
                    }
                    transDescArray.add(transDesc);
                    BigDecimal bd = new BigDecimal(transAmount);
                    sum = sum.add(bd);
                }
                // reverse arrays so that most recent transactions are shown on top.
                Collections.reverse(transDescArray);
                Collections.reverse(transArray);
                Log.d(TAG, "Sum is: " + sum);
                mAllowanceBalance.setText("$" + sum);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        DatabaseReference allowanceUsersRef = database.getReference("allowances/" +
                allowanceID + "/users");

        allowanceUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> usersToRemove = (HashMap<String, String>) dataSnapshot.getValue();
                usersToRemoveFromAllowanceArray.clear();
                if (usersToRemove == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                usersToRemoveFromAllowanceArray.addAll(usersToRemove.keySet());
                Log.d(TAG, "UsersToRemove is: " + usersToRemove);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateTransactionActivity();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_allowance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        final String allowanceID =  getIntent().getStringExtra("ALLOWANCE_ID");
        int id = item.getItemId();

        switch(id) {
            case R.id.share_allowance:
                Log.d(TAG, "Clicked Share Allowance button for " + mAllowanceName
                        .getText().toString());
                startShareAllowanceActivity();
                break;
            case R.id.delete_allowance:
                Log.d(TAG, "Clicked Delete Allowance button");
                AlertDialog.Builder builder = new AlertDialog.Builder(AllowanceDetailActivity.this);
                builder.setMessage(R.string.allowance_dialog_message)
                        .setTitle(R.string.allowance_dialog_title);

                builder.setPositiveButton(R.string.allowance_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        Log.d(TAG, "Clicked ok to delete allowance");
                        mDatabase = FirebaseDatabase.getInstance().getReference();

                        // remove allowance and all of its data from the database.
                        mDatabase.child("allowances")
                                .child(allowanceID)
                                .removeValue();
                        // Also remove the allowance id from each of the users that it has been shared with.
                        for (int u = 0; u < usersToRemoveFromAllowanceArray.size(); u++) {
                            mDatabase.child(usersToRemoveFromAllowanceArray.get(u))
                                    .child("allowances")
                                    .child(allowanceID)
                                    .removeValue();
                        }

                        // once the transaction has been deleted, exit the activity.
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.allowance_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Log.d(TAG, "Clicked Cancel Delete Allowance button");
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String allowanceID =  getIntent().getStringExtra("ALLOWANCE_ID");

        // grab all transactions from firebase and get their sum.
        DatabaseReference transRef = database.getReference("allowances/" +
                allowanceID + "/transactions");

        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BigDecimal sum = new BigDecimal(0);
                HashMap<String, String> value = (HashMap<String, String>) dataSnapshot.getValue();
                transDescArray.clear();
                transArray.clear();
                if (value == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                transArray.addAll(value.keySet());
                Log.d(TAG, "Value is: " + value);
                for (int i = 0; i <  transArray.size(); i++) {
                    String transDesc = (String) dataSnapshot
                            .child(transArray.get(i) + "/desc").getValue();
                    String transAmount = (String) dataSnapshot
                            .child( transArray.get(i) + "/amount").getValue();
                    Log.d(TAG, "Transaction amount is: " +  transAmount);
                    Log.d(TAG, "Transaction desc is: " +  transDesc);
                    if ( transAmount == null || transDesc == null) {
                        //TODO: display message.
                        Log.e(TAG, "Database is empty");
                        return;
                    }
                    transDescArray.add(transDesc);
                    BigDecimal bd = new BigDecimal(transAmount);
                    sum = sum.add(bd);
                }
                // reverse arrays so that most recent transactions are shown on top.
                Collections.reverse(transDescArray);
                Collections.reverse(transArray);
                Log.d(TAG, "Sum is: " + sum);
                mAllowanceBalance.setText("$" + sum);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void startCreateTransactionActivity() {
        Intent intent = new Intent(this, CreateTransactionActivity.class);
        intent.putExtra("USER_NAME", getIntent().getStringExtra("USER_NAME"));
        intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
        intent.putExtra("ALLOWANCE_NAME",getIntent().getStringExtra("ALLOWANCE_NAME"));
        intent.putExtra("ALLOWANCE_ID",getIntent().getStringExtra("ALLOWANCE_ID"));
        startActivity(intent);
    }

    private void startTransactionDetailActivity(String transactionID) {
        Intent intent = new Intent(this, TransactionDetailActivity.class);
        intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
        intent.putExtra("TRANSACTION_ID",transactionID);
        intent.putExtra("ALLOWANCE_ID",getIntent().getStringExtra("ALLOWANCE_ID"));
        startActivity(intent);
    }

    private void startShareAllowanceActivity() {
        Intent intent = new Intent(this, ShareAllowanceActivity.class);
        intent.putExtra("ALLOWANCE_ID",getIntent().getStringExtra("ALLOWANCE_ID"));
        startActivity(intent);
    }

}
