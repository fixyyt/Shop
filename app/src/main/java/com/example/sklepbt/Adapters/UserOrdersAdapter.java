package com.example.sklepbt.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sklepbt.Classes.Order;
import com.example.sklepbt.R;

import java.util.List;

public class UserOrdersAdapter extends RecyclerView.Adapter<UserOrdersAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orders;

    public UserOrdersAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        
        holder.orderNumber.setText(context.getString(R.string.order_number, order.getId()));
        holder.orderDate.setText(context.getString(R.string.order_date, order.getOrderDate()));
        holder.orderTotal.setText(context.getString(R.string.order_total, order.getTotalPrice()));

        // Konfiguracja RecyclerView dla produktów w zamówieniu
        OrderProductsAdapter productsAdapter = new OrderProductsAdapter(context, 
            order.getProducts(), order.getQuantities());
        holder.productsRecyclerView.setAdapter(productsAdapter);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderNumber;
        TextView orderDate;
        TextView orderTotal;
        RecyclerView productsRecyclerView;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderNumber = itemView.findViewById(R.id.order_number);
            orderDate = itemView.findViewById(R.id.order_date);
            orderTotal = itemView.findViewById(R.id.order_total);
            productsRecyclerView = itemView.findViewById(R.id.order_products_recycler_view);
            productsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }
} 