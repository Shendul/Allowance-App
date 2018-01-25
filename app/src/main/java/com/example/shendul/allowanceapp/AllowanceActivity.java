package com.example.shendul.allowanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
//import android.view.Menu;
//import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;


public class AllowanceActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "AllowanceActivity";

    ArrayList<String> userAllowanceArray = new ArrayList<>();
    ArrayList<String> allowanceArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allowance);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);


    }
    /* Don't use the snowman for now
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_allowance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code =" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(FirebaseUser account) {
        if (account != null) {
            Log.d(TAG, "loggedIn with " + account.getDisplayName());
            SignInButton signInButton = findViewById(R.id.sign_in_button);
            signInButton.setVisibility(View.GONE);

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startCreateAllowanceActivity();
                }
            });

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            String userName = mAuth.getCurrentUser().getEmail();
            userName = userName.substring(0, userName.length() - 4);
            DatabaseReference userRef = database.getReference(userName + "/allowances");
            DatabaseReference allowanceRef = database.getReference("allowances");

            final ArrayAdapter adapter = new ArrayAdapter<>(this,
                    R.layout.allowance_listview, allowanceArray);

            ListView listView = findViewById(R.id.allowance_list);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                    String value = (String) adapter.getItemAtPosition(position);
                    Log.e(TAG, "Clicked" + value);
                    startAllowanceDetailActivity(value, position);

                }
            });

            // Read from the database
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, String> value = (HashMap<String, String>) dataSnapshot.getValue();
                    userAllowanceArray.clear();
                    if (value == null) {
                        //TODO: display message.
                        Log.e(TAG, "Database is empty");
                        return;
                    }
                    userAllowanceArray.addAll(value.keySet());
                    Log.d(TAG, "Value is: " + value);

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            // Read from the database
            allowanceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    allowanceArray.clear();
                    for (int i = 0; i < userAllowanceArray.size(); i++) {
                        Log.d(TAG, "array[i] is: " +  userAllowanceArray.get(i));
                        String allowName = (String) dataSnapshot
                                .child(userAllowanceArray.get(i) + "/name").getValue();
                        Log.d(TAG, "Allowance name is: " + allowName);
                        if (allowName == null) {
                            //TODO: display message.
                            Log.e(TAG, "Database is empty");
                            return;
                        }
                        allowanceArray.add(allowName);


                    }
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Allowance is: " + allowanceArray);


                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        } else {
            SignInButton signInButton = findViewById(R.id.sign_in_button);
            signInButton.setSize(SignInButton.SIZE_STANDARD);
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(View.GONE);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWihGoogle " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredentials:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            // get email and remove .com from it because firebase does not allow the storing of .
                            String userEmail = user.getEmail();
                            userEmail = userEmail.substring(0,userEmail.length() - 4);

                            // also create the user in the firebase database
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                            database.child(userEmail).child("exists").setValue("true");

                            updateUI(user);
                        } else {
                            Log.d(TAG, "signInWithCredentials:failed");
                            updateUI(null);
                        }

                    }
                });
    }

    private void startCreateAllowanceActivity() {
        Intent intent = new Intent(this, CreateAllowanceActivity.class);
        String user_name = mAuth.getCurrentUser().getEmail();
        intent.putExtra("USER_NAME", user_name.substring(0, user_name.length() - 4));
        startActivity(intent);
    }

    private void startAllowanceDetailActivity(String allowance_name, int position) {
        Intent intent = new Intent(this, AllowanceDetailActivity.class);
        String user_name = mAuth.getCurrentUser().getEmail();
        intent.putExtra("USER_NAME", user_name.substring(0, user_name.length() - 4));
        intent.putExtra("ALLOWANCE_NAME", allowance_name);
        String allowance_id = "" + userAllowanceArray.get(position);
        intent.putExtra("ALLOWANCE_ID", allowance_id);
        startActivity(intent);
    }

}
