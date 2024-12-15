package com.example.sklepbt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sklepbt.DatabaseHelper;
import com.example.sklepbt.R;

import java.io.ByteArrayOutputStream;

public class Register extends AppCompatActivity {
    private EditText registerUsername, registerEmail, registerPassword;
    private Button confirm;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        db = new DatabaseHelper(this);
        registerUsername = findViewById(R.id.register_username);
        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);
        confirm = findViewById(R.id.btn_register_confirm);

        confirm.setOnClickListener(v -> {
            String username = registerUsername.getText().toString().trim();
            String email = registerEmail.getText().toString().trim();
            String password = registerPassword.getText().toString().trim();

            // Get default profile picture as Base64
            String defaultAvatarBase64 = getDefaultAvatarBase64();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (db.registerUser(username, password, email, defaultAvatarBase64)) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getDefaultAvatarBase64() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_picture);
        return convertBitmapToBase64(bitmap);
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
