package com.alloget.shendul.allowanceapp;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ShareAllowanceActivity extends AppCompatActivity {

    private static final String TAG = "ShareAllowanceActivity";
    EditText mNewUserName;
    TextView mSharedUsers;
    private DatabaseReference mDatabase;
    ArrayList<String> sharedWithArray = new ArrayList<>();
    HashMap<String, String> sharedWithEmails = new HashMap<>();
    String UID = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_allowance);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNewUserName = findViewById(R.id.new_user);
        mSharedUsers = findViewById(R.id.users_shared_with);

        final String allowanceID = getIntent().getStringExtra("ALLOWANCE_ID");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference allowanceUsersRef = database.getReference("allowances/" +
                allowanceID + "/users");

        // TODO: Make this query each user for their email.
        allowanceUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> sharedWith = (HashMap<String, String>) dataSnapshot.getValue();
                sharedWithArray.clear();
                if (sharedWith == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                sharedWithArray.addAll(sharedWith.keySet());
                Log.d(TAG, "Allowance is shared with : " + sharedWith);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        DatabaseReference emailToUIDRef = database.getReference("EmailToUID");

        emailToUIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sharedWithEmails.clear();
                sharedWithEmails = (HashMap<String, String>) dataSnapshot.getValue();
                if (sharedWithEmails == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                if (sharedWithArray != null && sharedWithEmails != null) {
                    String sharedUsers = "";
                    for (int i = 0; i < sharedWithArray.size(); i++) {
                        // if the last user, don't add a comma.
                        if (i == sharedWithArray.size() - 1)
                            sharedUsers += getKeyFromValue(sharedWithEmails, sharedWithArray.get(i));
                        else
                            sharedUsers += getKeyFromValue(sharedWithEmails, sharedWithArray.get(i)) + ", ";
                        Log.d(TAG, "SharedUsers is: " + sharedUsers);
                    }
                    sharedUsers = FirebaseEncodingAndDecoding.decodeFromFirebaseKey(sharedUsers);

                    mSharedUsers.setText(sharedUsers);
                } else {
                    // empty or error.
                    mSharedUsers.setText("");
                    Log.d(TAG, "SharedUsers is: Empty.");
                }
                Log.d(TAG, "Allowance is shared with these emails : " + sharedWithEmails);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mNewUserName.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == event.KEYCODE_ENTER){
                    addUser(database, allowanceID);
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.add_user_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser(database, allowanceID);
            }
        });
    }

    // helper function to get the key from a hash table when value is known.
    public static String getKeyFromValue(Map<String, String> hm, String value) {
        for (String key : hm.keySet()) {
            if (hm.get(key).equals(value)) {
                return key;
            }
        }
        return null;
    }

    public void addUser(FirebaseDatabase database, final String allowanceID) {
        Log.d(TAG, "Clicked Add User button");
        String email = this.mNewUserName.getText().toString();
        final String finalEmail = FirebaseEncodingAndDecoding.encodeForFirebaseKey(email);
        Log.d(TAG, "UserEmail is " + email);

        if (!finalEmail.isEmpty()) {

            Log.d(TAG, "email is not empty");

            mDatabase = database.getReference();
            DatabaseReference userRef = mDatabase.child("EmailToUID").child(finalEmail);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UID = (String) dataSnapshot.getValue();
                    if (UID == null) {
                        //TODO: display message.
                        Log.e(TAG, "Database is empty");
                        return;
                    }
                    Log.d(TAG, "UID : " + UID);
                    if (UID.isEmpty()) {
                        // show error message to user
                        Log.d(TAG, "UID doesn't exist");
                    } else if (!finalEmail.equals("allowances")) {

                        mDatabase.child(UID)
                                .child("allowances")
                                .child(allowanceID)
                                .setValue("");
                        mDatabase.child("allowances")
                                .child(allowanceID)
                                .child("users")
                                .child(UID)
                                .setValue("");

                        // once the allowance is shared, exit the activity.
                        finish();
                        Log.d(TAG, "User should have been added to the firebase database");
                    } else {
                        //display error message.
                        Log.d(TAG, "Something went wrong with the add user button");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });


        } else {
            Log.d(TAG, "Empty Email");
        }
    }

}
