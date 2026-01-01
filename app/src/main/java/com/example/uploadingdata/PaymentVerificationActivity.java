package com.example.uploadingdata;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class PaymentVerificationActivity extends AppCompatActivity {

    private static final String TAG = "PaymentVerification";

    private RecyclerView recyclerViewOrders;
    private EditText etOrderId, etTransactionId;
    private Button btnVerifyPayment, btnMarkAsPaid, btnMarkAsFailed;
    private TextView tvOrderDetails;

    private FirebaseFirestore db;
    private OrderVerificationAdapter adapter;
    private List<Order> pendingOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_verification);

        initializeViews();
        loadPendingOrders();
        setupClickListeners();
    }

    private void initializeViews() {
        recyclerViewOrders = findViewById(R.id.recyclerViewCart);
        etOrderId = findViewById(R.id.etOrderId);
        etTransactionId = findViewById(R.id.etTransactionId);
        btnVerifyPayment = findViewById(R.id.btnVerifyPayment);
        btnMarkAsPaid = findViewById(R.id.btnMarkAsPaid);
        btnMarkAsFailed = findViewById(R.id.btnMarkAsFailed);
        tvOrderDetails = findViewById(R.id.tvOrderDetails);

        db = FirebaseFirestore.getInstance();
        pendingOrders = new ArrayList<>();

        adapter = new OrderVerificationAdapter(pendingOrders, this::onOrderSelected);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOrders.setAdapter(adapter);
    }

    private void loadPendingOrders() {
        db.collection("orders")
                .whereEqualTo("paymentStatus", "PENDING")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading orders", error);
                        return;
                    }

                    pendingOrders.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            pendingOrders.add(order);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Loaded " + pendingOrders.size() + " pending orders", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        btnVerifyPayment.setOnClickListener(v -> {
            String orderId = etOrderId.getText().toString().trim();
            if (orderId.isEmpty()) {
                Toast.makeText(this, "Please enter Order ID", Toast.LENGTH_SHORT).show();
                return;
            }
            loadOrderDetails(orderId);
        });

        btnMarkAsPaid.setOnClickListener(v -> {
            String orderId = etOrderId.getText().toString().trim();
            String transactionId = etTransactionId.getText().toString().trim();

            if (orderId.isEmpty()) {
                Toast.makeText(this, "Please enter Order ID", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePaymentStatus(orderId, true, transactionId);
        });

        btnMarkAsFailed.setOnClickListener(v -> {
            String orderId = etOrderId.getText().toString().trim();

            if (orderId.isEmpty()) {
                Toast.makeText(this, "Please enter Order ID", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePaymentStatus(orderId, false, null);
        });
    }

    private void onOrderSelected(Order order) {
        etOrderId.setText(order.getOrderId());

        StringBuilder orderDetails = new StringBuilder();
        orderDetails.append("Order ID: ").append(order.getOrderId()).append("\\n");
        orderDetails.append("Customer: ").append(order.getCustomerName()).append("\\n");
        orderDetails.append("Phone: ").append(order.getCustomerPhone()).append("\\n");
        orderDetails.append("Amount: ₹").append(String.format("%.2f", order.getTotalAmount())).append("\\n");
        orderDetails.append("Status: ").append(order.getPaymentStatus()).append("\\n");
        orderDetails.append("Items: ").append(order.getItems().size()).append(" products\\n");

        tvOrderDetails.setText(orderDetails.toString());
    }

    private void loadOrderDetails(String orderId) {
        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            onOrderSelected(order);
                        }
                    } else {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                        tvOrderDetails.setText("Order not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading order", e);
                    Toast.makeText(this, "Error loading order details", Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePaymentStatus(String orderId, boolean isSuccess, String transactionId) {
        String status = isSuccess ? "SUCCESS" : "FAILED";
        String orderStatus = isSuccess ? "Confirmed" : "Payment Failed";

        DocumentReference orderRef = db.collection("orders").document(orderId);

        if (transactionId != null && !transactionId.isEmpty()) {
            orderRef.update(
                    "paymentStatus", status,
                    "orderStatus", orderStatus,
                    "transactionId", transactionId
            );
        } else {
            orderRef.update(
                    "paymentStatus", status,
                    "orderStatus", orderStatus
            );
        }

        orderRef.update("paymentStatus", status, "orderStatus", orderStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Payment status updated successfully!", Toast.LENGTH_SHORT).show();

                    // Clear fields
                    etOrderId.setText("");
                    etTransactionId.setText("");
                    tvOrderDetails.setText("");

                    // Reload pending orders
                    loadPendingOrders();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating payment status", e);
                    Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                });
    }

    // Adapter class for pending orders
    public static class OrderVerificationAdapter extends RecyclerView.Adapter<OrderVerificationAdapter.OrderViewHolder> {

        private List<Order> orders;
        private OnOrderClickListener listener;

        public interface OnOrderClickListener {
            void onOrderClick(Order order);
        }

        public OrderVerificationAdapter(List<Order> orders, OnOrderClickListener listener) {
            this.orders = orders;
            this.listener = listener;
        }

        @Override
        public OrderViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(OrderViewHolder holder, int position) {
            Order order = orders.get(position);

            holder.text1.setText("Order: " + order.getOrderId());
            holder.text2.setText("Customer: " + order.getCustomerName() +
                    " | Amount: ₹" + String.format("%.2f", order.getTotalAmount()));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;

            OrderViewHolder(android.view.View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}
