package com.example.shendul.allowanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: BUG_LIST:
// 1. When new transactions are added this screen does not update without a refresh.

public class AllowanceDetailActivity extends AppCompatActivity {

    private static final String TAG = "AllowanceDetailActivity";
    TextView mAllowanceName;
    TextView mAllowanceBalance;
    private DatabaseReference mDatabase;
    ArrayList<String> transArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allowance_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAllowanceName = (TextView) findViewById(R.id.allowance_name);
        mAllowanceBalance = (TextView)findViewById(R.id.allowance_balance);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        String user = getIntent().getStringExtra("USER_NAME");
        String allowanceName = getIntent().getStringExtra("ALLOWANCE_NAME");
        String allowanceID =  getIntent().getStringExtra("ALLOWANCE_ID");
        mAllowanceName.setText(allowanceName);

        final ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.allowance_listview, transArray);
        ListView listView = (ListView) findViewById(R.id.transactions_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                String id = (String) adapter.getItemAtPosition(position);
                Log.e(TAG, "Clicked " + id);
                startTransactionDetailActivity(id);

            }
        });

        // grab all transactions from firebase and get their sum.
        DatabaseReference transRef = database.getReference("allowances/" +
                allowanceID + "/transactions");
        // Read from the database
        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int sum = 0;
                HashMap<String, String> value = (HashMap<String, String>) dataSnapshot.getValue();
                transArray.clear();
                if (value == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                transArray.addAll(value.keySet());
                Log.d(TAG, "Value is: " + value);
                for (int i = 0; i <  transArray.size(); i++) {
                    Log.d(TAG, "array[i] is: " +   transArray.get(i));
                    String transAmount = (String) dataSnapshot
                            .child( transArray.get(i) + "/amount").getValue();
                    Log.d(TAG, "Transaction amount is: " +  transAmount);
                    if ( transAmount == null) {
                        //TODO: display message.
                        Log.e(TAG, "Database is empty");
                        return;
                    }
                    sum += Integer.parseInt(transAmount);
                }
                Log.d(TAG, "Sum is: " + sum);
                mAllowanceBalance.setText("$" + sum);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateTransactionActivity();
            }
        });
    }

    private void startCreateTransactionActivity() {
        Intent intent = new Intent(this, CreateTransactionActivity.class);
        intent.putExtra("USER_NAME", getIntent().getStringExtra("USER_NAME"));
        intent.putExtra("ALLOWANCE_NAME",getIntent().getStringExtra("ALLOWANCE_NAME"));
        intent.putExtra("ALLOWANCE_ID",getIntent().getStringExtra("ALLOWANCE_ID"));
        startActivity(intent);
    }

    private void startTransactionDetailActivity(String transactionID) {
        Log.e(TAG, "transID before switching activity is " + transactionID);
        Intent intent = new Intent(this, TransactionDetailActivity.class);
        intent.putExtra("TRANSACTION_ID",transactionID);
        intent.putExtra("ALLOWANCE_ID",getIntent().getStringExtra("ALLOWANCE_ID"));
        startActivity(intent);
    }

}
