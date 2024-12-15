package com.example.sklepbt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private EditText username, password;
    private Button login, register;
    private DatabaseHelper db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = new DatabaseHelper(this);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.btn_login);
        setupDynamicShortcuts();
        login.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String pass = password.getText().toString().trim();
            if (db.checkUser(user, pass)) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Store.class);
                intent.putExtra("username", user);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        register = findViewById(R.id.btn_register);
        register.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });
    }
    private void setupDynamicShortcuts() {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
            ShortcutInfo loginShortcut = new ShortcutInfo.Builder(this, "loginShortcut")
                    .setShortLabel("Login")
                    .setLongLabel("Go to Login Page")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_login))
                    .setIntent(new Intent(Intent.ACTION_VIEW)
                            .setClassName(this, MainActivity.class.getName()))
                    .build();

            ShortcutInfo registerShortcut = new ShortcutInfo.Builder(this, "registerShortcut")
                    .setShortLabel("Register")
                    .setLongLabel("Go to Register Page")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_register))
                    .setIntent(new Intent(Intent.ACTION_VIEW)
                            .setClassName(this, Register.class.getName()))
                    .build();

            shortcutManager.setDynamicShortcuts(Arrays.asList(loginShortcut, registerShortcut));
        }
    }

}