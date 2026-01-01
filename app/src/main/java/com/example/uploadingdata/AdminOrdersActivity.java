package com.example.uploadingdata;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminOrdersActivity extends AppCompatActivity implements OrdersAdapter.OnOrderClickListener {

    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private EditText searchEditText;
    private TextView ordersCountText;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;

    // Filter buttons
    private Button btnAllOrders, btnPending, btnProcessing, btnShipped, btnDelivered, btnCancelled;
    private Button currentSelectedFilter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Order> allOrders;
    private String currentFilter = "All";

    private final String[] statusOptions = {"Placed", "Processing", "Shipped", "Delivered", "Cancelled"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_orders_layout);

        mAuth = FirebaseAuth.getInstance();
        initializeViews();
        setupRecyclerView();
        setupFilterButtons();
        setupSearchBar();
        loadOrders();

        android.widget.ImageButton btnReloadOrders = findViewById(R.id.btnReloadOrders);
        if (btnReloadOrders != null) {
            btnReloadOrders.setOnClickListener(v -> loadOrders());
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_orders);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, AdminDashboard.class));
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(this, Add_products.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true;
            } else if (itemId == R.id.nav_logout) {
                getSharedPreferences("auth", MODE_PRIVATE).edit().putBoolean("admin_logged_in", false).apply();
                Intent intent = new Intent(AdminOrdersActivity.this, AdminLoginActivity.class);
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

    private void initializeViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        ordersCountText = findViewById(R.id.ordersCountText);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        btnAllOrders = findViewById(R.id.btnAllOrders);
        btnPending = findViewById(R.id.btnPending);
        btnProcessing = findViewById(R.id.btnProcessing);
        btnShipped = findViewById(R.id.btnShipped);
        btnDelivered = findViewById(R.id.btnDelivered);
        btnCancelled = findViewById(R.id.btnCancelled);

        currentSelectedFilter = btnAllOrders;
        db = FirebaseFirestore.getInstance();
        allOrders = new ArrayList<>();
    }

    private void setupRecyclerView() {
        ordersAdapter = new OrdersAdapter(this);
        ordersAdapter.setOnOrderClickListener(this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(ordersAdapter);
    }

    private void setupFilterButtons() {
        btnAllOrders.setOnClickListener(v -> applyFilter("All", btnAllOrders));
        btnPending.setOnClickListener(v -> applyFilter("Placed", btnPending));
        btnProcessing.setOnClickListener(v -> applyFilter("Processing", btnProcessing));
        btnShipped.setOnClickListener(v -> applyFilter("Shipped", btnShipped));
        btnDelivered.setOnClickListener(v -> applyFilter("Delivered", btnDelivered));
        btnCancelled.setOnClickListener(v -> applyFilter("Cancelled", btnCancelled));
    }

    private void setupSearchBar() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ordersAdapter.searchOrders(s.toString());
                updateOrdersCount();
            }
        });
    }

    private void applyFilter(String filter, Button selectedButton) {
        resetFilterButtonStyles();
        selectedButton.setBackgroundResource(R.drawable.filter_button_selected);
        selectedButton.setTextColor(getResources().getColor(android.R.color.white));
        currentSelectedFilter = selectedButton;

        currentFilter = filter;
        if (filter.equals("All")) {
            ordersAdapter.filterOrders(null);
        } else {
            ordersAdapter.filterOrders(filter);
        }
        updateOrdersCount();
    }

    private void resetFilterButtonStyles() {
        Button[] buttons = {btnAllOrders, btnPending, btnProcessing, btnShipped, btnDelivered, btnCancelled};
        for (Button button : buttons) {
            button.setBackgroundResource(R.drawable.filter_button_unselected);
            button.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void loadOrders() {
        showLoading(true);
        db.collection("orders")
                .orderBy("orderDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    allOrders.clear();
                    for (QueryDocumentSnapshot document : query) {
                        try {
                            Order order = document.toObject(Order.class);
                            if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                                order.setOrderId(document.getId());
                            }
                            allOrders.add(order);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ordersAdapter.setOrders(allOrders);
                    updateOrdersCount();
                    showLoading(false);
                    showEmptyState(allOrders.isEmpty());
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState(true);
                    Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateOrdersCount() {
        List<Order> filteredOrders = ordersAdapter.getFilteredOrders();
        String countText = (currentFilter.equals("All"))
                ? "Total Orders: " + filteredOrders.size()
                : currentFilter + " Orders: " + filteredOrders.size();
        ordersCountText.setText(countText);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        ordersRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        ordersRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onUpdateStatusClick(Order order, int position) {
        showUpdateStatusDialog(order, position);
    }

    @Override
    public void onViewDetailsClick(Order order) {
        showOrderDetailsDialog(order);
    }

    // ✅ Show dialog for status update
    private void showUpdateStatusDialog(Order order, int position) {
        int currentStatusIndex = Arrays.asList(statusOptions).indexOf(order.getOrderStatus());
        if (currentStatusIndex == -1) currentStatusIndex = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Order Status");
        builder.setSingleChoiceItems(statusOptions, currentStatusIndex, null);
        builder.setPositiveButton("Update", (dialog, which) -> {
            ListView listView = ((AlertDialog) dialog).getListView();
            int selectedIndex = listView.getCheckedItemPosition();
            if (selectedIndex != -1) {
                String newStatus = statusOptions[selectedIndex];
                updateOrderStatus(order, newStatus, position);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ✅ Update Firestore + UI
    private void updateOrderStatus(Order order, String newStatus, int position) {
        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
            Toast.makeText(this, "Cannot update: Missing Order ID", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating order status...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("orders").document(order.getOrderId())
                .update("orderStatus", newStatus)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    order.setOrderStatus(newStatus);
                    ordersAdapter.notifyItemChanged(position);
                    Toast.makeText(this, "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showOrderDetailsDialog(Order order) {
        StringBuilder details = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault());

        details.append("Order ID: ").append(order.getOrderId()).append("\n\n")
                .append("Status: ").append(order.getOrderStatus()).append("\n\n")
                .append("Customer: ").append(order.getCustomerName()).append("\n")
                .append("Phone: ").append(order.getCustomerPhone()).append("\n")
                .append("User ID: ").append(order.getUserId()).append("\n\n")
                .append("Order Date: ").append(order.getOrderDate() != null ? sdf.format(order.getOrderDate()) : "N/A").append("\n\n")
                .append("Total Amount: ₹").append(order.getTotalAmount()).append("\n")
                .append("Payment Method: ").append(order.getPaymentMethod()).append("\n")
                .append("Payment Status: ").append(order.getPaymentStatus()).append("\n\n")
                .append("Address:\n").append(order.getShippingAddress()).append("\n\n");

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            details.append("Items (").append(order.getItems().size()).append("):\n");
            for (int i = 0; i < order.getItems().size(); i++) {
                Order.OrderItem item = order.getItems().get(i);
                details.append(i + 1).append(". ").append(item.getProductName()).append("\n")
                        .append("   Qty: ").append(item.getQuantity()).append("\n")
                        .append("   Price: ₹").append(item.getPrice()).append("\n\n");
            }
        } else {
            details.append("Items: None\n");
        }

        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(details.toString());
        textView.setPadding(24, 24, 24, 24);
        textView.setTextSize(14f);
        scrollView.addView(textView);

        new AlertDialog.Builder(this)
                .setTitle("Order Details")
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .setNeutralButton("Update Status", (dialog, which) -> showUpdateStatusDialog(order, -1))
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
