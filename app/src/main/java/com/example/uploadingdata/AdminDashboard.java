package com.example.uploadingdata;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private android.widget.LinearLayout loadingOverlay;
    private android.widget.ProgressBar loadingProgress;
    private android.widget.ImageButton btnReloadAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingProgress = findViewById(R.id.loadingProgress);
        btnReloadAdmin = findViewById(R.id.btnReloadAdmin);
        if (btnReloadAdmin != null) {
            btnReloadAdmin.setOnClickListener(v -> fetchProducts());
        }

        fetchProducts();


        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(this, Add_products.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, AdminOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_logout){
                getSharedPreferences("auth", MODE_PRIVATE).edit().putBoolean("admin_logged_in", false).apply();
                Intent intent = new Intent(AdminDashboard.this, AdminLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            return false;
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean loggedIn = getSharedPreferences("auth", MODE_PRIVATE).getBoolean("admin_logged_in", false);
        if (!loggedIn) {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void fetchProducts() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(android.view.View.VISIBLE);
        if (loadingProgress != null) loadingProgress.setVisibility(android.view.View.VISIBLE);
        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        productList.add(product);
                    }
                    adapter.notifyDataSetChanged();
                    if (loadingOverlay != null) loadingOverlay.setVisibility(android.view.View.GONE);
                    if (loadingProgress != null) loadingProgress.setVisibility(android.view.View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading data", e);
                    Toast.makeText(AdminDashboard.this, "Failed to load images.", Toast.LENGTH_SHORT).show();
                    if (loadingOverlay != null) loadingOverlay.setVisibility(android.view.View.GONE);
                    if (loadingProgress != null) loadingProgress.setVisibility(android.view.View.GONE);
                });
    }
}
