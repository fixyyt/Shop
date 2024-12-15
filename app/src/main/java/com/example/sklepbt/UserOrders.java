package com.example.sklepbt;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sklepbt.Adapters.OrderAdapter;
import com.example.sklepbt.Classes.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserOrders extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_orders);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        loggedInUsername = getIntent().getStringExtra("username");

        int userId = db.getUserData(loggedInUsername).getId();

        Map<Product, Integer> userOrders = getUserOrdersMap(userId);

        ordersRecyclerView = findViewById(R.id.recyclerViewOrders);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, userOrders);
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private Map<Product, Integer> getUserOrdersMap(int userId) {
        List<Product> orderedProducts = db.getUserOrderedProducts(userId);
        Map<Product, Integer> ordersMap = new HashMap<>();

        for (Product product : orderedProducts) {
            int quantity = db.getOrderQuantity(userId, product.getId());
            ordersMap.put(product, quantity);
        }

        return ordersMap;
    }
}
