package com.hareesh.quotepad;

/**
 * Created by Hareesh on 9/3/2016.
 */
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
public class AuthActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    public ProgressDialog mProgressDialog;
    private static final String TAG = "AuthActivity";
    private static final int RC_SIGN_IN = 9001;
    private boolean emailSignIn;
    private boolean emailRegister;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    private GoogleApiClient mGoogleApiClient;
    private TextView titleText;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mEmailFieldRegister;
    private EditText mPasswordFieldRegister;
    private EditText mNameFieldRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !MainActivity.logout) {
            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
            startActivity(intent);
        }
        emailSignIn = false;
        emailRegister = false;

        // Views
        titleText = (TextView) findViewById(R.id.title_text);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        mEmailFieldRegister = (EditText) findViewById(R.id.field_email_register);
        mPasswordFieldRegister = (EditText) findViewById(R.id.field_password_register);
        mNameFieldRegister = (EditText) findViewById(R.id.field_name_register);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.cancel_button).setOnClickListener(this);
        findViewById(R.id.cancel_button_email).setOnClickListener(this);
        findViewById(R.id.submit_button_email).setOnClickListener(this);
        findViewById(R.id.cancel_button_email_register).setOnClickListener(this);
        findViewById(R.id.submit_button_email_register).setOnClickListener(this);


        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_application_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]
    }

    private void createAccount(String email, String password, String name) {
        Log.d(TAG, "createAccount:" + email);

        final String username = name;

        if (!validateFormRegister()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(AuthActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                // [START_EXCLUDE]
                                                hideProgressDialog();
                                                emailSignIn = false;
                                                emailRegister = false;
                                                // [END_EXCLUDE]
                                                Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                        }
                        hideProgressDialog();
                    }
                });
        Log.d(TAG, "register failed");
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateFormSignIn()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(AuthActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            emailSignIn = false;
                            emailRegister = false;
                            // [END_EXCLUDE]
                            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        hideProgressDialog();
                    }
                });
        // [END sign_in_with_email]
    }

    private boolean validateFormSignIn() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private boolean validateFormRegister() {
        boolean valid = true;

        String email = mEmailFieldRegister.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailFieldRegister.setError("Required.");
            valid = false;
        } else {
            mEmailFieldRegister.setError(null);
        }

        String name = mNameFieldRegister.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mNameFieldRegister.setError("Required.");
            valid = false;
        } else {
            mNameFieldRegister.setError(null);
        }

        String password = mPasswordFieldRegister.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordFieldRegister.setError("Required.");
            valid = false;
        } else {
            mPasswordFieldRegister.setError(null);
        }

        return valid;
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Log.d(TAG, "onAuthStateChanged:success");
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.d(TAG, "onAuthStateChanged:failure " + result.getStatus().getStatusCode());
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]

                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(AuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            // [END_EXCLUDE]
                            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        hideProgressDialog();
                    }
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        mGoogleApiClient.clearDefaultAccountAndReconnect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        // Firebase sign out
        showProgressDialog();
        mAuth.signOut();
        FirebaseAuth.getInstance().signOut();
        if(mGoogleApiClient.isConnected()) {
            // Google sign out
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            updateUI(null);
                        }
                    });
        }
        hideProgressDialog();
    }


    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            if(MainActivity.logout){
                titleText.setText("Are you sure you want to sign out?");
                titleText.setVisibility(View.VISIBLE);
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.email_sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.email_create_account_button).setVisibility(View.GONE);
                findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
                findViewById(R.id.email_editText).setVisibility(View.GONE);
                findViewById(R.id.email_register).setVisibility(View.GONE);
            }
        } else if(emailSignIn){
            titleText.setText("Enter your email and password:");
            titleText.setVisibility(View.VISIBLE);
            findViewById(R.id.email_create_account_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.email_sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.email_editText).setVisibility(View.VISIBLE);
            findViewById(R.id.email_register).setVisibility(View.GONE);
        } else if(emailRegister){
            titleText.setText("Fill in the form:");
            titleText.setVisibility(View.VISIBLE);
            findViewById(R.id.email_create_account_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.email_sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.email_editText).setVisibility(View.GONE);
            findViewById(R.id.email_register).setVisibility(View.VISIBLE);
        }
        else {
            titleText.setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.email_sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.email_create_account_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            findViewById(R.id.email_editText).setVisibility(View.GONE);
            findViewById(R.id.email_register).setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        } else if (i == R.id.email_create_account_button) {
            emailRegister = true;
            updateUI(null);
        } else if (i == R.id.email_sign_in_button) {
            emailSignIn = true;
            updateUI(null);
        } else if (i == R.id.sign_out_button) {
            if(MainActivity.logout){
                signOut();
                MainActivity.logout=false;
            }
        } else if (i == R.id.cancel_button) {
            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
            startActivity(intent);
            MainActivity.logout=false;
        } else if (i == R.id.cancel_button_email){
            emailSignIn = false;
            updateUI(null);
        } else if (i == R.id.submit_button_email){
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.cancel_button_email_register){
            emailRegister = false;
            updateUI(null);
        } else if (i == R.id.submit_button_email_register){
            createAccount(mEmailFieldRegister.getText().toString(), mPasswordFieldRegister.getText().toString(), mNameFieldRegister.getText().toString());
        }
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.email_editText).getVisibility() == View.VISIBLE){
            emailSignIn = false;
            emailRegister = false;
            updateUI(null);
        }
    }
}