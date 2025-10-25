
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

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EditText usernameEditText = binding.username;
        EditText passwordEditText = binding.password;
        Button loginButton = binding.login;
        ProgressBar loadingProgressBar = binding.loading;

        loginButton.setOnClickListener(v -> loadingProgressBar.setVisibility(View.VISIBLE));
        binding.buttonToGoHome.setOnClickListener(v -> {
            finish();
        });

        }
    }
