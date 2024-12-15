package com.example.sklepbt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddProduct extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText productNameInput, productDescriptionInput, productPriceInput;
    private ImageView productImageView;
    private Button addProductButton, chooseImageButton;
    private DatabaseHelper db;
    private String selectedImageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        productNameInput = findViewById(R.id.product_name_input);
        productDescriptionInput = findViewById(R.id.product_description_input);
        productPriceInput = findViewById(R.id.product_price_input);
        productImageView = findViewById(R.id.product_image_view);
        addProductButton = findViewById(R.id.btn_add_product);
        chooseImageButton = findViewById(R.id.btn_choose_image);

        chooseImageButton.setOnClickListener(v -> openImageChooser());

        addProductButton.setOnClickListener(v -> {
            String name = productNameInput.getText().toString().trim();
            String description = productDescriptionInput.getText().toString().trim();
            String priceString = productPriceInput.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(priceString) || selectedImageUri == null) {
                Toast.makeText(AddProduct.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            int price;
            try {
                price = Integer.parseInt(priceString);
            } catch (NumberFormatException e) {
                Toast.makeText(AddProduct.this, "Invalid price format", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean result = db.addProduct(name, description, selectedImageUri, price);
            if (result) {
                Toast.makeText(AddProduct.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddProduct.this, Store.class);
                intent.putExtra("username", "admin");
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(AddProduct.this, "Failed to add product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            selectedImageUri = imageUri.toString();
            productImageView.setImageURI(imageUri);
        }
    }
}
