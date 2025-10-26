
package com.example.mobile_dev_project;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mobile_dev_project.databinding.ActivityLoginBinding;
import com.example.mobile_dev_project.MapActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginActivity extends AppCompatActivity {


    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // auto in google-services.json
                .requestEmail()
                .build();


        googleClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EditText usernameEditText = binding.username;
        EditText passwordEditText = binding.password;
        Button loginButton = binding.login;
        Button registerButton = binding.register;
        ProgressBar loadingProgressBar = binding.loading;

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            } else {
                                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Google sign-in error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Google sign-in canceled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();  // treat username as email for Firebase
            String password = passwordEditText.getText().toString();

            if (email.isEmpty()) { usernameEditText.setError("Email required"); return; }
            if (password.isEmpty()) { passwordEditText.setError("Password required"); return; }

            loadingProgressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        loadingProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            passwordEditText.setError("Invalid credentials");
                            updateUI(null);
                        }
                    });
        });

        findViewById(R.id.btnGoogle).setOnClickListener(v -> {
            Intent signInIntent = googleClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(this, MapActivity.class));
            finish();
        } else {
            // Stay here; you could also show a message or clear errors.
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Toast.makeText(this, "Missing ID token", Toast.LENGTH_SHORT).show();
            return;
        }
        AuthCredential cred = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(cred)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
                        // (Optional) upsert to Room like you did for email/password:
                        //  - uid = fu.getUid()
                        //  - email = fu.getEmail()
                        //  - displayName = fu.getDisplayName()
                        //  - photoUrl = fu.getPhotoUrl() != null ? fu.getPhotoUrl().toString() : null
                        //  - timestamps...
                        //  - dao.upsert(user) on background thread

                        // navigate to home
                        // startActivity(new Intent(this, MapActivity.class));
                        // finish();
                        Toast.makeText(this, "Signed in with Google", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MapActivity.class));
                    } else {
                        String msg = (task.getException()!=null)? task.getException().getMessage() : "Sign-in failed";
                        Toast.makeText(this, "Firebase auth failed: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

}
