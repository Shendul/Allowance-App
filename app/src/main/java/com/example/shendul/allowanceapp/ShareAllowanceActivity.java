package com.example.shendul.allowanceapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.util.ArrayList;
import java.util.HashMap;

public class ShareAllowanceActivity extends AppCompatActivity {

    private static final String TAG = "ShareAllowanceActivity";
    EditText mNewUserName;
    private DatabaseReference mDatabase;
    HashMap<String, String> users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_allowance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TODO: add a list of all users who are associated with this allowance.

        mNewUserName = (EditText)findViewById(R.id.new_user);

        final String allowanceID = getIntent().getStringExtra("ALLOWANCE_ID");

        findViewById(R.id.add_user_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked Add User button");
                final String user = mNewUserName.getText().toString();
                Log.d(TAG, "User is " + user);

                mDatabase = FirebaseDatabase.getInstance().getReference();


                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        users = (HashMap<String, String>) dataSnapshot.getValue();

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });

                if (user.equals("")) {
                    // show error message to user
                } else if (user.equals("User to be added to this allowance")) {
                    // show error message to user
                } else if (users.containsKey(user) && !user.equals("allowances")) {

                    mDatabase.child(user)
                            .child("allowances")
                            .child(allowanceID)
                            .setValue("");

                    // once the allowance is shared, exit the activity.
                    finish();
                } else {
                    //display error message.
                }
            }
        });
    }

}
