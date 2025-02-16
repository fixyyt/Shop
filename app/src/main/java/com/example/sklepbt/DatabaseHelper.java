package com.example.sklepbt;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sklepbt.Classes.Order;
import com.example.sklepbt.Classes.Product;
import com.example.sklepbt.Classes.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sklepbt.db";

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_EMAIL = "email";
    private static final String COL_PROFILE_IMAGE = "profile_image";

    // Product table
    private static final String TABLE_PRODUCTS = "products";
    private static final String COL_PRODUCT_ID = "id";
    private static final String COL_PRODUCT_NAME = "name";
    private static final String COL_PRODUCT_DESCRIPTION = "description";
    private static final String COL_PRODUCT_IMAGE = "image";
    private static final String COL_PRODUCT_PRICE = "price";

    // Orders table
    private static final String TABLE_ORDERS = "orders";
    private static final String COL_ORDER_ID = "order_id";
    private static final String COL_ORDER_USER_ID = "user_id";
    private static final String COL_ORDER_DATE = "order_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 7);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT, " +
                COL_PASSWORD + " TEXT, " +
                COL_EMAIL + " TEXT, " +
                COL_PROFILE_IMAGE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PRODUCT_NAME + " TEXT, " +
                COL_PRODUCT_DESCRIPTION + " TEXT, " +
                COL_PRODUCT_IMAGE + " TEXT, " +
                COL_PRODUCT_PRICE + " INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_USER_ID + " INTEGER, " +
                COL_ORDER_DATE + " DATETIME, " +
                "FOREIGN KEY(" + COL_ORDER_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))");

        db.execSQL("CREATE TABLE OrderDetails (" +
                "order_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER, " +
                "FOREIGN KEY(order_id) REFERENCES orders(order_id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id))");

        addDefaultProducts(db);
    }

    private void addDefaultProducts(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        
        // Produkt 1
        values.put(COL_PRODUCT_NAME, "Komputer");
        values.put(COL_PRODUCT_DESCRIPTION, "Szybki komputer gamingowy");
        values.put(COL_PRODUCT_IMAGE, "android.resource://com.example.sklepbt/drawable/komputer");
        values.put(COL_PRODUCT_PRICE, 4999);
        db.insert(TABLE_PRODUCTS, null, values);
        
        // Produkt 2
        values.clear();
        values.put(COL_PRODUCT_NAME, "Myszka");
        values.put(COL_PRODUCT_DESCRIPTION, "Mysz komputerowa.");
        values.put(COL_PRODUCT_IMAGE, "android.resource://com.example.sklepbt/drawable/mysz");
        values.put(COL_PRODUCT_PRICE, 3);
        db.insert(TABLE_PRODUCTS, null, values);
        
        // Produkt 3
        values.clear();
        values.put(COL_PRODUCT_NAME, "Klawiatura");
        values.put(COL_PRODUCT_DESCRIPTION, "Świeci sie, że to szok.");
        values.put(COL_PRODUCT_IMAGE, "android.resource://com.example.sklepbt/drawable/klawiatura");
        values.put(COL_PRODUCT_PRICE, 899);
        db.insert(TABLE_PRODUCTS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS OrderDetails");
        onCreate(db);
    }

    public User getUserData(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PROFILE_IMAGE))
            );
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }

    public boolean updateUserEmail(String username, String newEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_EMAIL, newEmail);
        return db.update(TABLE_USERS, contentValues, COL_USERNAME + " = ?", new String[]{username}) > 0;
    }

    public String getUserEmailByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT email FROM users WHERE username = ?", new String[]{username});
        if (cursor != null && cursor.moveToFirst()) {
            String email = cursor.getString(0);
            cursor.close();
            return email;
        }
        return null;
    }

    public boolean updateUserPassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PASSWORD, newPassword);
        return db.update(TABLE_USERS, contentValues, COL_USERNAME + " = ?", new String[]{username}) > 0;
    }
    public long saveOrder(String username, List<Product> products, List<Integer> quantities) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();

            ContentValues orderValues = new ContentValues();
            orderValues.put(COL_ORDER_USER_ID, userId);
            String orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            orderValues.put(COL_ORDER_DATE, orderDate);
            long orderId = db.insert(TABLE_ORDERS, null, orderValues);

            if (orderId != -1) {
                for (int i = 0; i < products.size(); i++) {
                    ContentValues detailValues = new ContentValues();
                    detailValues.put("order_id", orderId);
                    detailValues.put("product_id", products.get(i).getId());
                    detailValues.put("quantity", quantities.get(i));
                    db.insert("OrderDetails", null, detailValues);
                }
                return orderId;
            }
        }
        cursor.close();
        return -1;
    }


    public boolean registerUser(String username, String password, String email, String profileImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, password);
        contentValues.put(COL_EMAIL, email);
        contentValues.put(COL_PROFILE_IMAGE, profileImage);
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "= ?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public boolean updateUserProfileImage(String username, String base64Image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PROFILE_IMAGE, base64Image);
        return db.update(TABLE_USERS, contentValues, COL_USERNAME + " = ?", new String[]{username}) > 0;
    }



    public boolean addProduct(String name, String description, String image, int price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PRODUCT_NAME, name);
        contentValues.put(COL_PRODUCT_DESCRIPTION, description);
        contentValues.put(COL_PRODUCT_IMAGE, image);
        contentValues.put(COL_PRODUCT_PRICE, price);
        long result = db.insert(TABLE_PRODUCTS, null, contentValues);
        return result != -1;
    }

    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM products", null);
        if (cursor.moveToFirst()) {
            do {
                int productId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESCRIPTION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE));
                int price = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE));

                productList.add(new Product(productId, name, description, imagePath, price));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return productList;
    }


    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PRODUCTS, COL_PRODUCT_ID + "=?", new String[]{String.valueOf(productId)}) > 0;
    }
    public Product getProductById(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COL_PRODUCT_ID + " = ?", new String[]{String.valueOf(productId)});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESCRIPTION));
            String image = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE));
            int price = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE));
            cursor.close();
            return new Product(productId, name, description, image, price);
        }

        cursor.close();
        return null;
    }
    public List<Product> getUserOrderedProducts(int userId) {
        List<Product> orderedProducts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p.id, p.name, p.description, p.image, p.price " +
                "FROM " + TABLE_PRODUCTS + " p " +
                "JOIN OrderDetails od ON p.id = od.product_id " +
                "JOIN " + TABLE_ORDERS + " o ON od.order_id = o.order_id " +
                "WHERE o.user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int productId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_DESCRIPTION));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE));
                int price = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE));

                orderedProducts.add(new Product(productId, name, description, image, price));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orderedProducts;
    }


    @SuppressLint("Range")
    public int getOrderQuantity(int userId, int productId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT od.quantity " +
                "FROM OrderDetails od " +
                "JOIN orders o ON od.order_id = o.order_id " +
                "WHERE o.user_id = ? AND od.product_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(productId)});

        int quantity = 0;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("quantity");
            if (columnIndex != -1) {
                quantity = cursor.getInt(columnIndex);
            }
        }
        cursor.close();
        return quantity;
    }

    // Dodaj metodę do sprawdzania czy są już produkty
    public boolean hasProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public List<Order> getUserOrders(int userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Najpierw pobierz wszystkie zamówienia użytkownika
        String orderQuery = "SELECT * FROM " + TABLE_ORDERS + 
                           " WHERE " + COL_ORDER_USER_ID + " = ? " +
                           " ORDER BY " + COL_ORDER_DATE + " DESC";
        
        Cursor orderCursor = db.rawQuery(orderQuery, new String[]{String.valueOf(userId)});

        while (orderCursor.moveToNext()) {
            int orderId = orderCursor.getInt(orderCursor.getColumnIndexOrThrow(COL_ORDER_ID));
            String orderDate = orderCursor.getString(orderCursor.getColumnIndexOrThrow(COL_ORDER_DATE));

            // Dla każdego zamówienia pobierz jego produkty i ilości
            String detailsQuery = "SELECT p.*, od.quantity FROM " + TABLE_PRODUCTS + " p " +
                                "JOIN OrderDetails od ON p.id = od.product_id " +
                                "WHERE od.order_id = ?";
            
            Cursor detailsCursor = db.rawQuery(detailsQuery, new String[]{String.valueOf(orderId)});

            List<Product> products = new ArrayList<>();
            List<Integer> quantities = new ArrayList<>();

            while (detailsCursor.moveToNext()) {
                int productId = detailsCursor.getInt(detailsCursor.getColumnIndexOrThrow(COL_PRODUCT_ID));
                String name = detailsCursor.getString(detailsCursor.getColumnIndexOrThrow(COL_PRODUCT_NAME));
                String description = detailsCursor.getString(detailsCursor.getColumnIndexOrThrow(COL_PRODUCT_DESCRIPTION));
                String image = detailsCursor.getString(detailsCursor.getColumnIndexOrThrow(COL_PRODUCT_IMAGE));
                int price = detailsCursor.getInt(detailsCursor.getColumnIndexOrThrow(COL_PRODUCT_PRICE));
                int quantity = detailsCursor.getInt(detailsCursor.getColumnIndexOrThrow("quantity"));

                products.add(new Product(productId, name, description, image, price));
                quantities.add(quantity);
            }
            detailsCursor.close();

            if (!products.isEmpty()) {
                orders.add(new Order(orderId, userId, products, quantities, orderDate));
            }
        }
        orderCursor.close();

        return orders;
    }
}
