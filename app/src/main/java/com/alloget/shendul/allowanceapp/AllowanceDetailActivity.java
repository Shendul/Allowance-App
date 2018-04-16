package com.alloget.shendul.allowanceapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.internal.NavigationMenu;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

// TODO: BUG_LIST:
// 1. First time clicking an allowance seems to not populate the transaction list. #POSSIBLY SQUASHED, REQ MORE TESTING.

class TransactionListItem {
    public TwoLineListItem twoLineListItem;
    public long timeInMilliseconds;
    public String key;

    public TransactionListItem(TwoLineListItem twoLineListItem, long timeInMilliseconds, String key){
        this.twoLineListItem = twoLineListItem;
        this.timeInMilliseconds = timeInMilliseconds;
        this.key = key;
    }
}

public class AllowanceDetailActivity extends AppCompatActivity {

    private static final String TAG = "AllowanceDetailActivity";
    TextView mAllowanceName;
    TextView mAllowanceBalance;
    private DatabaseReference mDatabase;
    ArrayList<String> transArray = new ArrayList<>();
    ArrayList<TwoLineListItem> transDescArray = new ArrayList<>();
    HashMap<String,TransactionListItem> transactionListItemHashmap = new HashMap<>();
    ArrayList<String> usersToRemoveFromAllowanceArray = new ArrayList<>();
    private TwoItemListAdapter mAdapter;

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

        mAdapter = new TwoItemListAdapter(this, transDescArray, false);
        ListView listView = findViewById(R.id.transactions_list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                String id = transArray.get(position);
                Log.e(TAG, "Clicked " + id);
                startTransactionDetailActivity(id);

            }
        });

        QueryForTransactionData(database, allowanceID);

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
                Log.d(TAG, "onCreate UsersToRemove is: " + usersToRemove);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        FabSpeedDial fabSpeedDial = findViewById(R.id.fab_speed_dial);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }
        });
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                if(menuItem.getTitle().equals("Add Money")){
                    startCreateTransactionActivity(true);
                    Log.d(TAG, "Clicked add money!");
                } else {
                    startCreateTransactionActivity(false);
                    Log.d(TAG, "Clicked subtract money!");
                }
                return false;
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
                        // once the allowance has been deleted, exit the activity.
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
            case R.id.leave_allowance:
                Log.d(TAG, "Clicked Leave Allowance button");
                AlertDialog.Builder builder2 = new AlertDialog.Builder(AllowanceDetailActivity.this);
                builder2.setMessage(R.string.allowance_leave_dialog_message)
                        .setTitle(R.string.allowance_leave_dialog_title);

                builder2.setPositiveButton(R.string.allowance_leave_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        Log.d(TAG, "Clicked ok to leave allowance");
                        mDatabase = FirebaseDatabase.getInstance().getReference();

                        //remove the allowance id from this user.
                        String userID = getIntent().getStringExtra("USER_ID");
                        mDatabase.child(userID)
                                .child("allowances")
                                .child(allowanceID)
                                .removeValue();

                        // remove the user from the allowance.
                        mDatabase.child("allowances")
                                .child(allowanceID)
                                .child("users")
                                .child(userID)
                                .removeValue();

                        // check and delete allowance if it has no users.
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference allowanceUsersRef = database.getReference("allowances/" +
                                allowanceID + "/users");
                        allowanceUsersRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<String, String> usersCheck = (HashMap<String, String>) dataSnapshot.getValue();
                                if (usersCheck == null) {
                                    //TODO: display message.
                                    Log.e(TAG, "Database is empty, delete allowance");
                                    // remove allowance and all of its data from the database.
                                    mDatabase.child("allowances")
                                            .child(allowanceID)
                                            .removeValue();
                                    return;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.w(TAG, "Failed to read value.", error.toException());
                            }
                        });
                        // once the allowance has been left, exit the activity.
                        finish();
                    }
                });
                builder2.setNegativeButton(R.string.allowance_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Log.d(TAG, "Clicked Cancel Delete Allowance button");
                    }
                });
                AlertDialog dialog2 = builder2.create();
                dialog2.show();
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String allowanceID =  getIntent().getStringExtra("ALLOWANCE_ID");

        QueryForTransactionData(database, allowanceID);
    }

    private void QueryForTransactionData(FirebaseDatabase database, String allowanceID) {
        // get allowance total.
        DatabaseReference allowTotalRef = database.getReference("allowances/" +
                allowanceID + "/total");
        allowTotalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = (String) dataSnapshot.getValue();
                if (value == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                if (value.charAt(0) == '-'){
                    value = "-$" + value.substring(1);
                } else {
                    value = "$" + value;
                }
                mAllowanceBalance.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        // grab all transactions from firebase.
        DatabaseReference transRef = database.getReference("allowances/" +
                allowanceID + "/transactions");


        transRef.orderByKey().limitToLast(20).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                Log.d(TAG, "onCreate onChildAdded key is: " + dataSnapshot.getKey());
                updateTransList(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onCreate onChildRemoved key is: " + dataSnapshot.getKey());
                updateTransList(dataSnapshot);
                transactionListItemHashmap.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "onCreate onChildChanged key is: " + dataSnapshot.getKey());
                updateTransList(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.d(TAG, "onCreate onChildMoved key is: " + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void updateTransList(DataSnapshot dataSnapshot){
        transDescArray.clear();
        transArray.clear();
        Long transDesc = (Long) dataSnapshot
                .child("desc").getValue();
        String transAmount = (String) dataSnapshot
                .child("amount").getValue();
        if (transDesc == null) {
            //TODO: display message.
            Log.e(TAG, "Database is empty");
            return;
        }
        // Convert long into timeAgo
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(transDesc, new Date().getTime(), DateUtils.MINUTE_IN_MILLIS);
        TwoLineListItem transTag = new TwoLineListItem(timeAgo, transAmount);
        Log.d(TAG, "Added/updated tag for: " + dataSnapshot.getKey());
        TransactionListItem transactionItem = new TransactionListItem(transTag,transDesc, dataSnapshot.getKey());
        transactionListItemHashmap.put(dataSnapshot.getKey(), transactionItem);
        final List<TransactionListItem> sortedTransactionListItems = new ArrayList<>();
        for (String key: transactionListItemHashmap.keySet()) {
            sortedTransactionListItems.add(transactionListItemHashmap.get(key));
        }

        Collections.sort(sortedTransactionListItems, new Comparator<TransactionListItem>() {
            @Override
            public int compare(TransactionListItem o1, TransactionListItem o2) {
                return Long.compare(o1.timeInMilliseconds, o2.timeInMilliseconds);
            }
        });
        Collections.reverse(sortedTransactionListItems);
        for (TransactionListItem item : sortedTransactionListItems) {
            Log.d(TAG, "Sorted Transaction list item = " + item.timeInMilliseconds);
            transArray.add(item.key);
            transDescArray.add(item.twoLineListItem);
        }

        for (TwoLineListItem item : transDescArray){
            Log.d(TAG, "Item Left = " + item.getLeftLine());
            Log.d(TAG, "Item Right = " + item.getRightLine());
        }

        mAdapter.notifyDataSetChanged();

    }

    private void startCreateTransactionActivity(boolean isAdd) {
        Intent intent = new Intent(this, CreateTransactionActivity.class);
        intent.putExtra("USER_ID", getIntent().getStringExtra("USER_ID"));
        intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
        intent.putExtra("ALLOWANCE_NAME",getIntent().getStringExtra("ALLOWANCE_NAME"));
        intent.putExtra("ALLOWANCE_ID",getIntent().getStringExtra("ALLOWANCE_ID"));
        intent.putExtra("IS_ADD", isAdd);
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
