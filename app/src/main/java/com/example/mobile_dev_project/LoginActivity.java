
package com.example.mobile_dev_project;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mobile_dev_project.databinding.ActivityLoginBinding;
import com.example.mobile_dev_project.MapActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

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

        mAuth = FirebaseAuth.getInstance();

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EditText usernameEditText = binding.username;
        EditText passwordEditText = binding.password;
        Button loginButton = binding.login;
        Button registerButton = binding.register;
        ProgressBar loadingProgressBar = binding.loading;

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

}
