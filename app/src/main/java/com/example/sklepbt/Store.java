package com.example.sklepbt;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sklepbt.Adapters.ProductAdapter;
import com.example.sklepbt.Classes.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Store extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private DatabaseHelper db;
    private String loggedInUsername;
    private static final String LANGUAGE_PREF = "language_preference";

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Wczytaj zapisany język przed inicjalizacją widoku
        android.content.SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString(LANGUAGE_PREF, "pl"); // domyślnie polski
        setLocale(language);
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DatabaseHelper(this);
        loggedInUsername = getIntent().getStringExtra("username");

        // Sprawdź czy są produkty, jeśli nie - zwiększ wersję bazy danych aby wymusić dodanie domyślnych
        if (!db.hasProducts()) {
            db.onUpgrade(db.getWritableDatabase(), 0, 1);
        }

        MaterialTextView cartSummary = findViewById(R.id.cart_summary);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Product> products = db.getAllProducts();
        productAdapter = new ProductAdapter(this, products);
        
        // Ustaw listener przed załadowaniem stanu koszyka
        productAdapter.setCartUpdateListener(cartSize -> {
            if (cartSize > 0) {
                cartSummary.setVisibility(View.VISIBLE);
                cartSummary.setText(getString(R.string.cart, cartSize));
            } else {
                cartSummary.setVisibility(View.GONE);
            }
        });

        recyclerView.setAdapter(productAdapter);
        
        // Wczytaj stan koszyka po utworzeniu adaptera
        productAdapter.loadCartState();
        
        // Ręcznie zaktualizuj widoczność koszyka po załadowaniu stanu
        int cartSize = productAdapter.getTotalCartSize();
        if (cartSize > 0) {
            cartSummary.setVisibility(View.VISIBLE);
            cartSummary.setText(getString(R.string.cart, cartSize));
        }

        FloatingActionButton fab = findViewById(R.id.fab_add_product);
        if ("admin".equals(loggedInUsername)) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(Store.this, AddProduct.class);
                startActivity(intent);
            });
        } else {
            fab.setVisibility(View.GONE);
        }

        cartSummary.setOnClickListener(v -> {
            Intent intent = new Intent(Store.this, OrderActivity.class);
            intent.putExtra("cartItems", new HashMap<>(productAdapter.getProductQuantities()));
            intent.putExtra("username", loggedInUsername);
            startActivityForResult(intent, 1);
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.account:
                Intent accountIntent = new Intent(Store.this, UserData.class);
                accountIntent.putExtra("username", loggedInUsername);
                startActivity(accountIntent);
                return true;
            case R.id.orders:
                Intent ordersIntent = new Intent(this, UserOrders.class);
                ordersIntent.putExtra("username", loggedInUsername);
                startActivity(ordersIntent);
                return true;
            case R.id.sendSms:
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, 1);
                } else {
                    sendSmsWithCartContents();
                }
                return true;
            case R.id.share:
                shareCartContents();
                return true;
            case R.id.changeLanguage:
                showLanguageDialog();
                return true;
            case R.id.about:
                about();
                return true;
            case R.id.logout:
                logoutUser();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void showLanguageDialog() {
        final String[] languages = {"English", "Polski"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz język / Choose Language");
        builder.setItems(languages, (dialog, which) -> {
            switch (which) {
                case 0:
                    setLocale("en");
                    break;
                case 1:
                    setLocale("pl");
                    break;
            }
        });
        builder.show();
    }
    private void setLocale(String langCode) {
        // Zapisz wybrany język w preferencjach
        android.content.SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LANGUAGE_PREF, langCode);
        editor.apply();

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        
        getBaseContext().getResources().updateConfiguration(config, 
            getBaseContext().getResources().getDisplayMetrics());

        recreate();
    }

    private void about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.autor, "Bartosz Tecmer\n"));
        builder.setTitle(R.string.about);
        builder.setCancelable(false);

        builder.setPositiveButton("OK", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void logoutUser() {
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Store.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void shareCartContents() {
        Map<Product, Integer> cart = productAdapter.getProductQuantities();
        StringBuilder shareContent = new StringBuilder("Mój koszyk:\n");

        if (cart.isEmpty()) {
            shareContent.append("Twój koszyk jest pusty.");
        } else {
            for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                shareContent.append(product.getName())
                        .append(" - ilość: ")
                        .append(quantity)
                        .append("\n");
            }
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent.toString());
        startActivity(Intent.createChooser(shareIntent, "Udostępnij za pomocą"));
    }


    private void sendSmsWithCartContents() {
        Map<Product, Integer> cart = productAdapter.getProductQuantities();
        StringBuilder smsContent = new StringBuilder("Mój koszyk:\n");

        if (cart.isEmpty()) {
            smsContent.append("Twój koszyk jest pusty.");
        } else {
            for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                smsContent.append(product.getName())
                        .append(" - ilość: ")
                        .append(quantity)
                        .append("\n");
            }
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("123456789", null, smsContent.toString(), null, null);
            Toast.makeText(this, "SMS wysłany!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Nie udało się wysłać SMS-a", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendSmsWithCartContents();
        } else {
            Toast.makeText(this, "Permission denied to send SMS", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("clearCart", false)) {
                // Wyczyść koszyk w adapterze
                productAdapter.clearCart();
                
                // Ukryj podsumowanie koszyka
                MaterialTextView cartSummary = findViewById(R.id.cart_summary);
                cartSummary.setVisibility(View.GONE);

                Toast.makeText(this, "Koszyk został wyczyszczony.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Zapisz stan koszyka przy wyjściu z aktywności
        if (productAdapter != null) {
            productAdapter.saveCartState();
        }
    }

}
