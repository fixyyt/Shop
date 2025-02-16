package com.example.sklepbt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sklepbt.Adapters.OrderAdapter;
import com.example.sklepbt.Classes.Product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView totalPrice;
    private EditText inputName, inputEmail;
    private Button submitOrder;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        recyclerView = findViewById(R.id.recycler_view_order);
        totalPrice = findViewById(R.id.total_price);
        inputName = findViewById(R.id.input_name);
        inputEmail = findViewById(R.id.input_email);
        submitOrder = findViewById(R.id.button_submit_order);

        Map<Product, Integer> cartItems = (Map<Product, Integer>) getIntent().getSerializableExtra("cartItems");

        username = getIntent().getStringExtra("username");

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String userEmail = dbHelper.getUserEmailByUsername(username);
        inputEmail.setText(userEmail);

        if (cartItems != null && !cartItems.isEmpty()) {
            OrderAdapter orderAdapter = new OrderAdapter(this, cartItems);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(orderAdapter);

            int total = 0;
            StringBuilder orderSummary = new StringBuilder();
            for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
                Product product = entry.getKey();
                int quantity = entry.getValue();
                total += product.getPrice() * quantity;
                orderSummary.append(product.getName())
                        .append(" x")
                        .append(quantity)
                        .append(" = ")
                        .append(product.getPrice() * quantity)
                        .append(" zł\n");
            }
            totalPrice.setText(getString(R.string.total_price, (double)total));

            int finalTotal = total;
            submitOrder.setOnClickListener(v -> {
                String name = inputName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Proszę wypełnić wszystkie pola.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                List<Product> products = new ArrayList<>();
                List<Integer> quantities = new ArrayList<>();
                for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
                    products.add(entry.getKey());
                    quantities.add(entry.getValue());
                }
                long orderId = dbHelper.saveOrder(username, products, quantities);

                if (orderId != -1) {
                    StringBuilder orderSummary2 = new StringBuilder();
                    for (int i = 0; i < products.size(); i++) {
                        Product product = products.get(i);
                        int quantity = quantities.get(i);
                        orderSummary2.append(product.getName())
                                .append(" x")
                                .append(quantity)
                                .append(" = ")
                                .append(product.getPrice() * quantity)
                                .append(" zł\n");
                    }

                    String fullOrderDetails = "Data zamówienia: " + orderDate + "\n" +
                            "Produkty:\n" + orderSummary2.toString() +
                            "\nRazem: " + finalTotal + " zł\n" +
                            "\nZamawiający: " + name;

                    sendOrderEmail(email, orderDate, orderId, fullOrderDetails);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("clearCart", true);
                    setResult(RESULT_OK, resultIntent);
                    
                    Toast.makeText(this, "Zamówienie zostało złożone pomyślnie!", Toast.LENGTH_SHORT).show();
                    
                    finish();
                } else {
                    Toast.makeText(this, "Nie udało się zapisać zamówienia.", Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            Toast.makeText(this, "Koszyk jest pusty", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void sendOrderEmail(String email, String orderDate, long orderId, String orderDetails) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Zamówienie " + orderId);
        emailIntent.putExtra(Intent.EXTRA_TEXT, orderDetails);

        try {
            startActivity(Intent.createChooser(emailIntent, "Wybierz aplikację do wysyłki e-maila"));
            Toast.makeText(this, "Zamówienie wysłane.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Nie udało się wysłać e-maila.", Toast.LENGTH_SHORT).show();
        }
    }

}