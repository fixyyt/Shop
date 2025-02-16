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
    private static final String CART_PREFS = "cart_preferences";


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
                if (currentQuantity == 1) {
                    productQuantities.remove(product);
                } else {
                    productQuantities.put(product, currentQuantity - 1);
                }
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onCartUpdated(getTotalCartSize());
                }
            }
        });
    }

    public int getTotalCartSize() {
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

    public void clearCart() {
        productQuantities.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onCartUpdated(0);
        }
        
        // Wyczyść zapisany stan koszyka
        android.content.SharedPreferences prefs = context.getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    // Metoda do zapisywania stanu koszyka
    public void saveCartState() {
        android.content.SharedPreferences prefs = context.getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        
        // Konwertuj mapę na format JSON
        org.json.JSONObject cartJson = new org.json.JSONObject();
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            try {
                Product product = entry.getKey();
                org.json.JSONObject productJson = new org.json.JSONObject();
                productJson.put("id", product.getId());
                productJson.put("quantity", entry.getValue());
                cartJson.put(String.valueOf(product.getId()), productJson);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }
        
        editor.putString("cart", cartJson.toString());
        editor.apply();
    }

    // Metoda do wczytywania stanu koszyka
    public void loadCartState() {
        android.content.SharedPreferences prefs = context.getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE);
        String cartJson = prefs.getString("cart", "{}");
        
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(cartJson);
            productQuantities.clear();
            
            // Iteruj po zapisanych produktach
            org.json.JSONArray keys = jsonObject.names();
            if (keys != null) {
                for (int i = 0; i < keys.length(); i++) {
                    String key = keys.getString(i);
                    org.json.JSONObject productJson = jsonObject.getJSONObject(key);
                    int productId = productJson.getInt("id");
                    int quantity = productJson.getInt("quantity");
                    
                    // Znajdź produkt w liście produktów
                    for (Product product : productList) {
                        if (product.getId() == productId) {
                            productQuantities.put(product, quantity);
                            break;
                        }
                    }
                }
            }
            
            notifyDataSetChanged();
            if (listener != null) {
                listener.onCartUpdated(getTotalCartSize());
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

}
