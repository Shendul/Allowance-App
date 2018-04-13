package com.alloget.shendul.allowanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;


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

// TODO: BUG_LIST:
// NONE CURRENTLY KNOWN.

public class AllowanceActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "AllowanceActivity";
    private ProgressBar spinner;
    private TwoItemListAdapter mAdapter;

    ArrayList<TwoLineListItem> allowanceArray = new ArrayList<>();
    ArrayList<String> userAllowanceArray = new ArrayList<>();

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
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);


    }

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
            spinner.setVisibility(View.VISIBLE);
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

            final FirebaseDatabase database = FirebaseDatabase.getInstance(); // initialize Firebase reference.

            mAdapter = new TwoItemListAdapter(this, allowanceArray, true);

            ListView listView = findViewById(R.id.allowance_list);
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                    TwoLineListItem allowTag = allowanceArray.get(position);
                    String name = allowTag.getLeftLine();
                    Log.e(TAG, "Clicked" + name);
                    startAllowanceDetailActivity(name, position);
                }
            });

            // Get all allowance id's associated with the logged in user.
            DatabaseReference userRef = database.getReference(account.getUid() + "/allowances");
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, String> value = (HashMap<String, String>) dataSnapshot.getValue();
                    userAllowanceArray.clear();
                    if (value == null) {
                        //TODO: display message.
                        Log.e(TAG, "Database is empty");
                        spinner.setVisibility(View.GONE);
                        return;
                    }
                    userAllowanceArray.addAll(value.keySet());
                    spinner.setVisibility(View.GONE);
                    Log.d(TAG, "Value is: " + value);

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });

            // Get all allowance names and amounts, place them in the listview.
            DatabaseReference allowanceRef = database.getReference("allowances");
            allowanceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    allowanceArray.clear();
                    for (int i = 0; i < userAllowanceArray.size(); i++) {
                        Log.d(TAG, "array[i] is: " +  userAllowanceArray.get(i));
                        String allowName = (String) dataSnapshot
                                .child(userAllowanceArray.get(i) + "/name").getValue();
                        String allowTotal = (String) dataSnapshot
                                .child(userAllowanceArray.get(i) + "/total").getValue();
                        Log.d(TAG, "Allowance name is: " + allowName);
                        if (allowName == null || allowTotal == null) {
                            //TODO: display message.
                            Log.e(TAG, "Database is empty");
                            return;
                        }
                        int indexOfDec = allowTotal.indexOf('.');
                        if (indexOfDec != -1){
                            allowTotal = allowTotal.substring(0, indexOfDec);
                        }
                        TwoLineListItem allowTag = new TwoLineListItem(allowName, allowTotal);
                        allowanceArray.add(allowTag);


                    }
                    mAdapter.notifyDataSetChanged();
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
                            userEmail = FirebaseEncodingAndDecoding.encodeForFirebaseKey(userEmail);

                            // also create the user in the firebase database
                            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                            //database.child(user.getUid()).child("email").setValue(userEmail);
                            database.child("EmailToUID").child(userEmail).setValue(user.getUid());

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
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "No current user.");
            return;
        }
        String userID =  mAuth.getCurrentUser().getUid();
        String userEmail = mAuth.getCurrentUser().getEmail();
        userEmail = FirebaseEncodingAndDecoding.encodeForFirebaseKey(userEmail);
        intent.putExtra("USER_NAME", userID);
        intent.putExtra("EMAIL", userEmail);
        startActivity(intent);
    }

    private void startAllowanceDetailActivity(String allowance_name, int position) {
        Intent intent = new Intent(this, AllowanceDetailActivity.class);
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "No current user.");
            return;
        }
        String userEmail = mAuth.getCurrentUser().getEmail();
        String userID = mAuth.getCurrentUser().getUid();
        intent.putExtra("USER_ID", userID);
        intent.putExtra("EMAIL", userEmail);
        intent.putExtra("ALLOWANCE_NAME", allowance_name);
        String allowance_id = "" + userAllowanceArray.get(position);
        intent.putExtra("ALLOWANCE_ID", allowance_id);
        startActivity(intent);
    }

}
