package com.example.shendul.allowanceapp;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class ShareAllowanceActivity extends AppCompatActivity {

    private static final String TAG = "ShareAllowanceActivity";
    EditText mNewUserName;
    TextView mSharedUsers;
    private DatabaseReference mDatabase;
    HashMap<String, String> users;
    ArrayList<String> sharedArray = new ArrayList<>();


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
        DatabaseReference transRef = database.getReference("allowances/" +
                allowanceID + "/users");
        // Read from the database
        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> value = (HashMap<String, String>) dataSnapshot.getValue();
                sharedArray.clear();
                if (value == null) {
                    //TODO: display message.
                    Log.e(TAG, "Database is empty");
                    return;
                }
                sharedArray.addAll(value.keySet());
                Log.d(TAG, "Value is: " + value);
                if (sharedArray != null) {
                    String sharedUsers = "";
                    for (int i = 0 ; i < sharedArray.size(); i++) {
                        if (i == sharedArray.size() - 1)
                            sharedUsers += sharedArray.get(i);
                        else
                            sharedUsers += sharedArray.get(i) + ", ";
                        Log.d(TAG, "SharedUsers is: " + sharedUsers);
                    }
                    mSharedUsers.setText(sharedUsers);
                } else {
                    // empty or error.
                    mSharedUsers.setText("");
                    Log.d(TAG, "SharedUsers is: Empty.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mDatabase = database.getReference();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                users = (HashMap<String, String>) dataSnapshot.getValue();
                Log.d(TAG, "Users is " + users);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        findViewById(R.id.add_user_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked Add User button");
                String user = mNewUserName.getText().toString();
                if (user.substring(user.length() - 4).equals(".com"))
                    user = user.substring(0, user.length() - 4);
                Log.d(TAG, "User is " + user);

                if (user.equals("") && users.isEmpty()) {
                    // show error message to user
                } else if (users.containsKey(user) && !user.equals("allowances")) {

                    mDatabase.child(user)
                            .child("allowances")
                            .child(allowanceID)
                            .setValue("");
                    mDatabase.child("allowances")
                            .child(allowanceID)
                            .child("users")
                            .child(user)
                            .setValue("");

                    // once the allowance is shared, exit the activity.
                    finish();
                } else {
                    //display error message.
                    Log.d(TAG, "Something went wrong with the add user button");
                }
            }
        });
    }

}
