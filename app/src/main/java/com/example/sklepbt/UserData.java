package com.example.sklepbt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sklepbt.Classes.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;

public class UserData extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private MaterialButton btnSaveChanges;
    private ShapeableImageView profileImage;
    private MaterialButton btnChangeImage;
    private DatabaseHelper db;
    private String loggedInUsername;
    private static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_data);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        loggedInUsername = getIntent().getStringExtra("username");

        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnSaveChanges = findViewById(R.id.btn_save_changes);

        profileImage = findViewById(R.id.profile_image);
        btnChangeImage = findViewById(R.id.btn_change_image);

        btnChangeImage.setOnClickListener(v -> openImagePicker());
        loadUserData();

        btnSaveChanges.setOnClickListener(v -> {
            String newEmail = editEmail.getText().toString().trim();
            String newPassword = editPassword.getText().toString().trim();

            if (!newEmail.isEmpty()) {
                saveUserEmail(newEmail);
            }
            if (!newPassword.isEmpty()) {
                saveUserPassword(newPassword);
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);

                String base64Image = convertBitmapToBase64(bitmap);
                saveUserProfileImage(base64Image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveUserProfileImage(String imagePath) {
        boolean success = db.updateUserProfileImage(loggedInUsername, imagePath);
        if (success) {
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update profile picture!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserPassword(String newPassword) {
        boolean success = db.updateUserPassword(loggedInUsername, newPassword);
        if (success){
            Toast.makeText(this, "Success! "+newPassword, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserEmail(String newEmail) {
        boolean success = db.updateUserEmail(loggedInUsername, newEmail);
        if (success){
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        User user = db.getUserData(loggedInUsername);
        if (user != null) {
            editEmail.setText(user.getEmail());
            editPassword.setText("");
            if (user.getProfileImage() != null) {
                Bitmap bitmap = convertBase64ToBitmap(user.getProfileImage());
                profileImage.setImageBitmap(bitmap);
            }

        }
    }
    private Bitmap convertBase64ToBitmap(String base64) {
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}
