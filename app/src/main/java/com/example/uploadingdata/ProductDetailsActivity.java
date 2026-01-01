package com.example.uploadingdata;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productPrice, productCategory, quantityText, productDetails;
    private ImageButton btnMinus, btnPlus, btnAR;
    private Button btnAddToCart;

    private int quantity = 1;

    private String name, imageUrl, category, modelUrl, details;
    private double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // ✅ Initialize views
        productImage = findViewById(R.id.productImage);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productCategory = findViewById(R.id.productCategory);
        productDetails = findViewById(R.id.productDetails);
        quantityText = findViewById(R.id.quantityText);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAR = findViewById(R.id.btnAR);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // ✅ Get product data from intent
        Intent intent = getIntent();
        name = intent.getStringExtra("productName");
        category = intent.getStringExtra("productType");
        price = intent.getDoubleExtra("productPrice", 0.0);
        imageUrl = intent.getStringExtra("productImageUrl");
        modelUrl = intent.getStringExtra("productModelUrl");
        details = intent.getStringExtra("productDetails"); // newly added

        // ✅ Display product info
        productName.setText(name);
        productCategory.setText("Category: " + category);
        productPrice.setText("Price: ₹" + price);
        productDetails.setText(details != null ? details : "No details available");
        quantityText.setText(String.valueOf(quantity));

        // ✅ Load product image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(productImage);
        }

        // ✅ Handle quantity increase
        btnPlus.setOnClickListener(v -> {
            quantity++;
            quantityText.setText(String.valueOf(quantity));
        });

        // ✅ Handle quantity decrease
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
            }
        });

        // ✅ Add to cart logic
        btnAddToCart.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("name", name);
            cartItem.put("price", price);
            cartItem.put("category", category);
            cartItem.put("quantity", quantity);
            cartItem.put("imageUrl", imageUrl);
            cartItem.put("modelUrl", modelUrl);
            cartItem.put("details", details);

            db.collection("carts")
                    .document(userId)
                    .collection("items")
                    .add(cartItem)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(ProductDetailsActivity.this, "Added to cart", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(ProductDetailsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // ✅ Open AR View for model placement
        btnAR.setOnClickListener(v -> {
            Intent arIntent = new Intent(this, MainActivity.class);
            arIntent.putExtra("modelUrl", modelUrl);
            startActivity(arIntent);
        });
    }
}
