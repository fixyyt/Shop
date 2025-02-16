package com.example.sklepbt;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sklepbt.Adapters.UserOrdersAdapter;
import com.example.sklepbt.Classes.Order;

import java.util.List;

public class UserOrders extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView ordersRecyclerView;
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
        
        // Pobierz ID użytkownika
        int userId = db.getUserData(loggedInUsername).getId();

        ordersRecyclerView = findViewById(R.id.recyclerViewOrders);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pobierz listę zamówień
        List<Order> userOrders = db.getUserOrders(userId);
        
        // Użyj nowego adaptera
        UserOrdersAdapter orderAdapter = new UserOrdersAdapter(this, userOrders);
        ordersRecyclerView.setAdapter(orderAdapter);
    }
}
