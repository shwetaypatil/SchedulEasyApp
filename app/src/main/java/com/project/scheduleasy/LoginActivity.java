package com.project.scheduleasy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText userName, passWord;
    private Button loginButton, studLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userName = findViewById(R.id.userName);
        passWord = findViewById(R.id.passWord);
        loginButton = findViewById(R.id.loginButton);
        studLogin = findViewById(R.id.studLogin);

        //auth-login check
        if (AuthManager.getInstance(this).isFacultyLoggedIn()){
            startMainActivity();
            finish();
        }

        loginButton.setOnClickListener(v -> handleFacultyLogin());
        studLogin.setOnClickListener(v -> handleStudentLogin());
    }

    private void handleFacultyLogin() {
        String username = userName.getText().toString().trim();
        String password = passWord.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else if (isValidFaculty(username, password)) {
            AuthManager.getInstance(this).saveLoginState(username, "FACULTY");
            startMainActivity();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleStudentLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_TYPE", "STUDENT");
        startActivity(intent);
        finish();
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // Hardcoded faculty check (Replace with Firebase/Database later)
    private boolean isValidFaculty(String username, String password) {
        return username.equals("DYPSN") && password.equals("DYP@123");
    }
}