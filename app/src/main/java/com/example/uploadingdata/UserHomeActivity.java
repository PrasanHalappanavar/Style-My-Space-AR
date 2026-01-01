package com.example.uploadingdata;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class UserHomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextInputEditText searchEditText;
    private UserProductAdapter adapter;
    private ArrayList<Product> productList;
    private FirebaseFirestore db;
    private android.widget.ProgressBar loadingProgress;
    private android.widget.ImageButton btnReload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        recyclerView = findViewById(R.id.recyclerViewUserProducts);
        searchEditText = findViewById(R.id.searchEditText);
        loadingProgress = findViewById(R.id.loadingProgress);
        btnReload = findViewById(R.id.btnReload);
        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        adapter = new UserProductAdapter(productList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setAdapter(adapter);

        btnReload.setOnClickListener(v -> loadProducts());
        loadProducts();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_home);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_camera) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void loadProducts() {
        if (loadingProgress != null) loadingProgress.setVisibility(android.view.View.VISIBLE);
        db.collection("products")
                .get()
                .addOnSuccessListener(snapshots -> {
                    productList.clear();
                    Log.d("Firestore", "Document count: " + snapshots.size());
                    for (DocumentSnapshot doc : snapshots) {
                        Log.d("Firestore", "Document data: " + doc.getData());

                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            productList.add(product);
                        } else {
                            Log.e("Firestore", "Product is null for doc ID: " + doc.getId());
                        }
                    }

                    adapter.setFullList(productList);
                    adapter.notifyDataSetChanged();

                    if (loadingProgress != null) loadingProgress.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Loaded " + productList.size() + " products", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to fetch products", e);
                    if (loadingProgress != null) loadingProgress.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Error loading products", Toast.LENGTH_SHORT).show();
                });
    }

    
}
