package com.example.mobile_dev_project;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobile_dev_project.data.local.dao.UserDao;
import com.example.mobile_dev_project.data.local.db.AppDatabase;
import com.example.mobile_dev_project.data.local.entity.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    AppDatabase db;
    UserDao dao;

    private FirebaseAuth mAuth;

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;
    private Button registerButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);
        dao = db.userDao();

        mAuth = FirebaseAuth.getInstance();

        firstNameEditText = findViewById(R.id.first_name);
        lastNameEditText = findViewById(R.id.last_name);
        emailEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        passwordConfirmEditText = findViewById(R.id.passwordConfirm);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);

        backButton = findViewById(R.id.backButton);
        registerButton = findViewById(R.id.registerButton);

        backButton.setOnClickListener(v ->
                finish());

        registerButton.setOnClickListener(v->{
            String email = emailEditText.getText().toString().trim();
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String passwordConfirm = passwordConfirmEditText.getText().toString().trim();

            if(email.isEmpty()){
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailEditText.setError("Please enter a valid email");
                emailEditText.requestFocus();
                return;
            }
            if(firstName.isEmpty()){
                firstNameEditText.setError("First name is required");
                firstNameEditText.requestFocus();
                return;
            }
            if(lastName.isEmpty()){
                lastNameEditText.setError("Last name is required");
                lastNameEditText.requestFocus();
                return;
            }
            if(password.isEmpty()){
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
                return;
            }else if(password.length() < 6){
                passwordEditText.setError("Password has to be at least 6 symbols");
                passwordEditText.requestFocus();
                return;
            }
            if(!password.equals(passwordConfirm)){
                passwordConfirmEditText.setError("Passwords don't match");
                passwordConfirmEditText.requestFocus();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task ->{
                        if (task.isSuccessful()) {
                            FirebaseUser fu = mAuth.getCurrentUser();

                            User u = new User();
                            u.uid = fu.getUid();
                            u.uid = fu.getUid();
                            u.email = fu.getEmail();
                            u.firstName = firstNameEditText.getText().toString().trim();
                            u.lastName = lastNameEditText.getText().toString().trim();
                            u.displayName = (u.firstName + " " + u.lastName).trim();
                            long now = System.currentTimeMillis();
                            u.createdAt = now;
                            u.updatedAt = now;

                            new Thread(()->{
                                try {
                                    dao.upsert(u);
                                    runOnUiThread(() -> {
                                        // startActivity(new Intent(this, MapActivity.class));
                                        // finish();
                                        // For now, maybe just show a toast:
                                        Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show();
                                    });
                                } catch (Exception e) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(this, "DB error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                                }
                            }).start();

                        } else {
                            Exception e = task.getException();
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}