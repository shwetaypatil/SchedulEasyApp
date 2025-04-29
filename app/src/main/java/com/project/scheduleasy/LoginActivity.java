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
    private AuthManager authManager;

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

        authManager = AuthManager.getInstance(this);

        // Check if coming from logout
        boolean fromLogout = getIntent().getBooleanExtra("LOGOUT", false);

        //auth-login check
        if (authManager.isFacultyLoggedIn() && !fromLogout) {
            startMainActivity("FACULTY");
            finish();
            return;
        }

        loginButton.setOnClickListener(v -> handleFacultyLogin());
        studLogin.setOnClickListener(v -> handleStudentLogin());
    }

    private void handleFacultyLogin() {
        String username = userName.getText().toString().trim();
        String password = passWord.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        if (isValidFaculty(username, password)) {
            authManager.saveFacultyLogin(username);
            startMainActivity("FACULTY");
        } else {
            showToast("Invalid faculty credentials");
        }
    }

    private void handleStudentLogin() {
        startMainActivity("STUDENT");
    }

    private void startMainActivity(String userType) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_TYPE", userType);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Hardcoded faculty check (Replace with Firebase/Database later)
    private boolean isValidFaculty(String username, String password) {
        return username.equals("DYPSN") && password.equals("DYP@123");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}