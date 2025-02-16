package com.example.sklepbt.Adapters;

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

public class OrderProductsAdapter extends RecyclerView.Adapter<OrderProductsAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> products;
    private List<Integer> quantities;

    public OrderProductsAdapter(Context context, List<Product> products, List<Integer> quantities) {
        this.context = context;
        this.products = products;
        this.quantities = quantities;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        int quantity = quantities.get(position);
        double totalPrice = product.getPrice() * quantity;

        holder.productName.setText(product.getName());
        holder.productQuantity.setText(context.getString(R.string.product_quantity, quantity));
        holder.productPrice.setText(context.getString(R.string.product_total_price, totalPrice));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productQuantity;
        TextView productPrice;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            productPrice = itemView.findViewById(R.id.product_price);
        }
    }
} 