package com.example.sklepbt.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sklepbt.Classes.Product;
import com.example.sklepbt.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {


    public interface CartUpdateListener {
        void onCartUpdated(int cartSize);
    }
    private List<Product> productList;
    private Context context;
    private CartUpdateListener listener;
    private List<Product> cart;
    private Map<Product, Integer> productQuantities = new HashMap<>();


    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.cart = new ArrayList<>();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list, parent, false);
        return new ProductViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productDescription.setText(product.getDescription());
        holder.productPrice.setText(String.format("%d PLN", product.getPrice()));

        Glide.with(context)
                .load(product.getImagePath())
                .placeholder(R.drawable.profile_picture)
                .into(holder.productImage);

        int quantity = productQuantities.getOrDefault(product, 0);
        holder.cartQuantity.setText(String.valueOf(quantity));
        holder.cartQuantity.setVisibility(quantity > 0 ? View.VISIBLE : View.GONE);
        holder.removeFromCart.setVisibility(quantity > 0 ? View.VISIBLE : View.GONE);

        holder.addToCart.setOnClickListener(v -> {
            int currentQuantity = productQuantities.getOrDefault(product, 0);
            productQuantities.put(product, currentQuantity + 1);
            notifyItemChanged(position);
            if (listener != null) {
                listener.onCartUpdated(getTotalCartSize());
            }
        });

        holder.removeFromCart.setOnClickListener(v -> {
            int currentQuantity = productQuantities.getOrDefault(product, 0);
            if (currentQuantity > 0) {
                productQuantities.put(product, currentQuantity - 1);
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onCartUpdated(getTotalCartSize());
                }
            }
        });
    }

    private int getTotalCartSize() {
        int total = 0;
        for (int quantity : productQuantities.values()) {
            total += quantity;
        }
        return total;
    }



    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productDescription, productPrice, cartQuantity;
        ImageView productImage;
        View addToCartButton;
        Button addToCart, removeFromCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productDescription = itemView.findViewById(R.id.product_description);
            productPrice = itemView.findViewById(R.id.product_price);
            productImage = itemView.findViewById(R.id.product_image);
            addToCart = itemView.findViewById(R.id.add_to_cart);
            removeFromCart = itemView.findViewById(R.id.remove_from_cart);
            cartQuantity = itemView.findViewById(R.id.cart_quantity);

        }
    }
    public void setCartUpdateListener(CartUpdateListener listener) {
        this.listener = listener;
        this.cart = new ArrayList<>();
    }

    public Map<Product, Integer> getProductQuantities() {
        return new HashMap<>(productQuantities);
    }
    public void updateProducts(List<Product> updatedProducts) {
        this.productList.clear();
        this.productList.addAll(updatedProducts);
        notifyDataSetChanged();
    }

}
