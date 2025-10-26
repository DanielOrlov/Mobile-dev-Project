package com.example.mobile_dev_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobile_dev_project.data.local.dao.UserDao;
import com.example.mobile_dev_project.data.local.db.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class
UserProfileActivity extends BaseActivity {
    private AppDatabase db;
    private UserDao userDao;

    TextView displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        displayName = findViewById(R.id.displayNameId);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(fUser == null){
            displayName.setText("Not logged in");
            return;
        }

        db = AppDatabase.getInstance(this);
        userDao = db.userDao();

        userDao.observeByUid(fUser.getUid()).observe(this, user ->{
            if(user == null){
                String label = (fUser.getEmail() != null) ? fUser.getEmail() : fUser.getUid();
                displayName.setText(label);
            }
            else {
                String name = (user.displayName != null && !user.displayName.trim().isEmpty())
                        ? user.displayName
                        : ((user.email != null) ? user.email : user.uid);
                displayName.setText(name);
            }
        });


        Button logoutButton = findViewById(R.id.logoutId);


        logoutButton.setOnClickListener(v ->{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
}