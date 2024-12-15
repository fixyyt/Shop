package com.example.sklepbt.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sklepbt.Classes.Product;
import com.example.sklepbt.R;

import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Map.Entry<Product, Integer>> cartItems;

    public OrderAdapter(Context context, Map<Product, Integer> cartItems) {
        this.context = context;
        this.cartItems = List.copyOf(cartItems.entrySet());
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_list, parent, false);
        return new OrderViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Map.Entry<Product, Integer> item = cartItems.get(position);
        Product product = item.getKey();
        int quantity = item.getValue();

        holder.productName.setText(product.getName());
        holder.productQuantity.setText(context.getString(R.string.quantity_order, quantity));
        holder.productPrice.setText(context.getString(R.string.price_order, (double)product.getPrice() * quantity));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity, productPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.order_product_name);
            productQuantity = itemView.findViewById(R.id.order_product_quantity);
            productPrice = itemView.findViewById(R.id.order_product_price);
        }
    }
}
