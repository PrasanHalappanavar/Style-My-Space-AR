//package com.example.uploadingdata;
//
//import android.app.AlertDialog;
//import android.content.ClipData;
//import android.content.ClipboardManager;
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class CartActivity extends AppCompatActivity {
//
//    private static final String TAG = "CartActivity";
//
//    private RecyclerView recyclerView;
//    private CartAdapter adapter;
//    private List<CartItem> cartList;
//    private FirebaseFirestore db;
//    private TextView totalAmountText;
//    private Button btnBuyNow;
//
//    private double totalAmount = 0;
//    private String currentOrderId;
//    private String selectedPaymentMethod;
//    private String customerName, customerPhone, shippingAddress;
//
//    // Replace with your personal UPI ID
//    private static final String MERCHANT_UPI_ID = "9008698595@ybl"; // Change this to your UPI ID
//    private static final String MERCHANT_NAME = "Your Store Name"; // Change this to your business name
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_cart);
//
//        initializeViews();
//        setupRecyclerView();
//        loadCartData();
//        setupBuyNowButton();
//    }
//
//    private void initializeViews() {
//        recyclerView = findViewById(R.id.recyclerViewCart);
//        totalAmountText = findViewById(R.id.totalAmountText);
//        btnBuyNow = findViewById(R.id.btnBuyNow);
//
//        cartList = new ArrayList<>();
//        db = FirebaseFirestore.getInstance();
//    }
//
//    private void setupRecyclerView() {
//        adapter = new CartAdapter(cartList, this::calculateTotal);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
//    }
//
//    private void loadCartData() {
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        db.collection("carts")
//                .document(uid)
//                .collection("items")
//                .addSnapshotListener((value, error) -> {
//                    if (error != null) {
//                        Toast.makeText(this, "Error loading cart", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    cartList.clear();
//                    if (value != null) {
//                        for (QueryDocumentSnapshot doc : value) {
//                            CartItem item = doc.toObject(CartItem.class);
//                            item.setDocId(doc.getId());
//                            cartList.add(item);
//                        }
//                    }
//
//                    adapter.notifyDataSetChanged();
//                    calculateTotal();
//                });
//    }
//
//    private void setupBuyNowButton() {
//        btnBuyNow.setOnClickListener(v -> {
//            if (cartList.isEmpty()) {
//                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (totalAmount <= 0) {
//                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            showPaymentDialog();
//        });
//    }
//
//    private void showPaymentDialog() {
//        String[] paymentOptions = {
//                "Pay with UPI ID (Manual Entry)",
//                "Pay via QR Code",
//                "Copy UPI Details",
//                "Send Payment Link"
//        };
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select Payment Method")
//                .setIcon(R.drawable.ic_payment)
//                .setSingleChoiceItems(paymentOptions, -1, (dialog, which) -> {
//                    selectedPaymentMethod = paymentOptions[which];
//                })
//                .setPositiveButton("Continue", (dialog, which) -> {
//                    if (selectedPaymentMethod != null) {
//                        showCustomerDetailsDialog();
//                    } else {
//                        Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                .setCancelable(true)
//                .show();
//    }
//
//    private void showCustomerDetailsDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Enter Customer Details");
//
//        // Create custom layout for customer details
//        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
//        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
//        layout.setPadding(50, 40, 50, 40);
//
//        final EditText nameInput = new EditText(this);
//        nameInput.setHint("Full Name");
//        nameInput.setPadding(20, 20, 20, 20);
//        layout.addView(nameInput);
//
//        // Add spacing
//        android.widget.Space space1 = new android.widget.Space(this);
//        space1.setMinimumHeight(20);
//        layout.addView(space1);
//
//        final EditText phoneInput = new EditText(this);
//        phoneInput.setHint("Phone Number");
//        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
//        phoneInput.setPadding(20, 20, 20, 20);
//        layout.addView(phoneInput);
//
//        // Add spacing
//        android.widget.Space space2 = new android.widget.Space(this);
//        space2.setMinimumHeight(20);
//        layout.addView(space2);
//
//        final EditText addressInput = new EditText(this);
//        addressInput.setHint("Shipping Address");
//        addressInput.setMinLines(3);
//        addressInput.setPadding(20, 20, 20, 20);
//        layout.addView(addressInput);
//
//        builder.setView(layout)
//                .setPositiveButton("Proceed to Payment", (dialog, which) -> {
//                    String name = nameInput.getText().toString().trim();
//                    String phone = phoneInput.getText().toString().trim();
//                    String address = addressInput.getText().toString().trim();
//
//                    if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
//                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    customerName = name;
//                    customerPhone = phone;
//                    shippingAddress = address;
//
//                    createOrderAndProceedPayment();
//                })
//                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                .show();
//    }
//
//    private void createOrderAndProceedPayment() {
//        currentOrderId = generateOrderId();
//
//        // Convert CartItems to OrderItems
//        List<Order.OrderItem> orderItems = new ArrayList<>();
//        for (CartItem cartItem : cartList) {
//            Order.OrderItem orderItem = new Order.OrderItem(
//                    cartItem.getName(),
//                    cartItem.getImageUrl(),
//                    cartItem.getPrice(),
//                    (int) cartItem.getQuantity()
//            );
//            orderItems.add(orderItem);
//        }
//
//        // Create order object
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        Order order = new Order(currentOrderId, userId, orderItems, totalAmount,
//                shippingAddress, customerName, customerPhone);
//        order.setPaymentMethod(selectedPaymentMethod);
//        order.setPaymentStatus("PENDING");
//
//        // Save order to Firestore
//        db.collection("orders")
//                .document(currentOrderId)
//                .set(order)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "Order created successfully");
//                    proceedWithSelectedPaymentMethod();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Error creating order", e);
//                    Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void proceedWithSelectedPaymentMethod() {
//        switch (selectedPaymentMethod) {
//            case "Pay with UPI ID (Manual Entry)":
//                showManualUpiPayment();
//                break;
//            case "Pay via QR Code":
//                generateQRCode();
//                break;
//            case "Copy UPI Details":
//                copyUpiDetails();
//                break;
//            case "Send Payment Link":
//                sendPaymentLink();
//                break;
//            default:
//                Toast.makeText(this, "Invalid payment method selected", Toast.LENGTH_SHORT).show();
//                break;
//        }
//    }
//
//    private void showManualUpiPayment() {
//        String transactionNote = "Order Payment - " + currentOrderId;
//        String message = "Please complete your payment using any UPI app:\\n\\n" +
//                "💳 Pay To: " + MERCHANT_NAME + "\\n" +
//                "🆔 UPI ID: " + MERCHANT_UPI_ID + "\\n" +
//                "💰 Amount: ₹" + String.format("%.2f", totalAmount) + "\\n" +
//                "📝 Note: " + transactionNote + "\\n\\n" +
//                "⚠️ Important: Use the exact amount and note for proper order processing.\\n\\n" +
//                "After payment, please take a screenshot and contact us with your order ID: " + currentOrderId;
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Complete UPI Payment")
//                .setMessage(message)
//                .setPositiveButton("Open UPI App", (dialog, which) -> {
//                    openUpiAppWithDetails();
//                })
//                .setNegativeButton("Copy Details", (dialog, which) -> {
//                    copyUpiDetails();
//                })
//                .setNeutralButton("Done", (dialog, which) -> {
//                    // User will manually verify payment later
//                    Toast.makeText(this, "Order placed! We'll verify your payment shortly.", Toast.LENGTH_LONG).show();
//                    clearCart();
//                })
//                .setCancelable(false)
//                .show();
//    }
//
//    private void openUpiAppWithDetails() {
//        // This approach opens UPI apps with a generic intent that works with personal UPI IDs
//        String transactionNote = "Order " + currentOrderId;
//
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("text/plain");
//        intent.putExtra(Intent.EXTRA_TEXT,
//                "Pay ₹" + String.format("%.2f", totalAmount) +
//                        " to " + MERCHANT_UPI_ID +
//                        " for " + transactionNote);
//
//        Intent chooserIntent = Intent.createChooser(intent, "Share Payment Details");
//
//        if (chooserIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(chooserIntent);
//        } else {
//            copyUpiDetails();
//        }
//    }
//
//    private void generateQRCode() {
//        // For QR code, you would typically use a QR code generation library
//        // Here we'll show the payment details that can be converted to QR
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("QR Code Payment")
//                .setMessage("Ask the customer to scan this QR code with their UPI app:\\n\\n" +
//                        "UPI ID: " + MERCHANT_UPI_ID + "\\n" +
//                        "Amount: ₹" + String.format("%.2f", totalAmount) + "\\n" +
//                        "Order: " + currentOrderId + "\\n\\n" +
//                        "Note: QR code generation requires additional library integration.")
//                .setPositiveButton("Generate QR", (dialog, which) -> {
//                    // Here you would integrate a QR code library like ZXing
//                    Toast.makeText(this, "QR Code feature requires ZXing library integration", Toast.LENGTH_LONG).show();
//                })
//                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
//                .show();
//    }
//
//    private void copyUpiDetails() {
//        String transactionNote = "Order " + currentOrderId;
//        String paymentDetails = "UPI Payment Details:\\n" +
//                "Pay To: " + MERCHANT_NAME + "\\n" +
//                "UPI ID: " + MERCHANT_UPI_ID + "\\n" +
//                "Amount: ₹" + String.format("%.2f", totalAmount) + "\\n" +
//                "Note: " + transactionNote + "\\n" +
//                "Order ID: " + currentOrderId;
//
//        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//        ClipData clip = ClipData.newPlainText("UPI Payment Details", paymentDetails);
//        clipboard.setPrimaryClip(clip);
//
//        Toast.makeText(this, "Payment details copied to clipboard!", Toast.LENGTH_LONG).show();
//
//        // Show confirmation dialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Payment Details Copied")
//                .setMessage("Payment details have been copied to your clipboard. Please share with the customer or use in your UPI app.\\n\\nOrder ID: " + currentOrderId)
//                .setPositiveButton("Order Placed", (dialog, which) -> {
//                    Toast.makeText(this, "Order placed! Verify payment manually.", Toast.LENGTH_LONG).show();
//                    clearCart();
//                })
//                .setNegativeButton("Cancel Order", (dialog, which) -> {
//                    // Optionally delete the order
//                    dialog.dismiss();
//                })
//                .show();
//    }
//
//    private void sendPaymentLink() {
//        String transactionNote = "Order " + currentOrderId;
//        String paymentMessage = "Hi " + customerName + ",\\n\\n" +
//                "Please complete your payment for Order #" + currentOrderId + ":\\n\\n" +
//                "💰 Amount: ₹" + String.format("%.2f", totalAmount) + "\\n" +
//                "🆔 Pay to UPI ID: " + MERCHANT_UPI_ID + "\\n" +
//                "📝 Payment Note: " + transactionNote + "\\n\\n" +
//                "Please use the exact amount and note for order processing.\\n\\n" +
//                "Thank you!\\n" + MERCHANT_NAME;
//
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, paymentMessage);
//        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Payment Details - Order #" + currentOrderId);
//
//        Intent chooserIntent = Intent.createChooser(shareIntent, "Send Payment Details");
//
//        if (chooserIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(chooserIntent);
//
//            // Update order status
//            Toast.makeText(this, "Payment details sent! Order placed.", Toast.LENGTH_LONG).show();
//            clearCart();
//        } else {
//            Toast.makeText(this, "No app available to send message", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void clearCart() {
//        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        // Delete all items from cart
//        db.collection("carts")
//                .document(uid)
//                .collection("items")
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
//                        document.getReference().delete();
//                    }
//                    Toast.makeText(this, "Cart cleared!", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> Log.e(TAG, "Error clearing cart", e));
//    }
//
//    private String generateOrderId() {
//        return "ORD" + System.currentTimeMillis();
//    }
//
//    private void calculateTotal() {
//        totalAmount = 0;
//        for (CartItem item : cartList) {
//            totalAmount += item.getPrice() * item.getQuantity();
//        }
//        totalAmountText.setText("Total Amount: ₹" + String.format("%.2f", totalAmount));
//
//        // Enable/disable buy button based on cart content
//        btnBuyNow.setEnabled(!cartList.isEmpty() && totalAmount > 0);
//    }
//
//    // Method to manually update payment status (call this when payment is verified)
//    public void updatePaymentStatus(String orderId, boolean isSuccess, String transactionId) {
//        String status = isSuccess ? "SUCCESS" : "FAILED";
//        String orderStatus = isSuccess ? "Confirmed" : "Payment Failed";
//
//        db.collection("orders")
//                .document(orderId)
//                .update(
//                        "paymentStatus", status,
//                        "orderStatus", orderStatus,
//                        "transactionId", transactionId != null ? transactionId : "MANUAL_VERIFICATION"
//                )
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "Payment status updated for order: " + orderId);
//                    Toast.makeText(this, "Payment status updated!", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> Log.e(TAG, "Error updating payment status", e));
//    }
//}



package com.example.uploadingdata;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dev.shreyaspatil.easyupipayment.EasyUpiPayment;
import dev.shreyaspatil.easyupipayment.listener.PaymentStatusListener;
import dev.shreyaspatil.easyupipayment.model.PaymentApp;
import dev.shreyaspatil.easyupipayment.model.TransactionDetails;

public class CartActivity extends AppCompatActivity implements PaymentStatusListener {

    private static final String TAG = "CartActivity";

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartList;
    private FirebaseFirestore db;
    private TextView totalAmountText;
    private Button btnBuyNow;

    private double totalAmount = 0;
    private String currentOrderId;

    private static final String PAYEE_VPA = "9164282725@axl"; // UPI ID
    private static final String PAYEE_NAME = "Style My Space"; // name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_cart);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, UserHomeActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_camera) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_cart) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else {
                return false;
            }
        });

        initializeViews();
        setupRecyclerView();
        loadCartData();
        setupBuyNowButton();

        android.widget.ImageButton btnReloadCart = findViewById(R.id.btnReloadCart);
        android.widget.ProgressBar loadingProgress = findViewById(R.id.loadingProgress);
        if (btnReloadCart != null) {
            btnReloadCart.setOnClickListener(v -> {
                if (loadingProgress != null) loadingProgress.setVisibility(android.view.View.VISIBLE);
                loadCartData();
            });
        }
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

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewCart);
        totalAmountText = findViewById(R.id.totalAmountText);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        cartList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartList, this::calculateTotal);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadCartData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        android.widget.ProgressBar loadingProgress = findViewById(R.id.loadingProgress);
        if (loadingProgress != null) loadingProgress.setVisibility(android.view.View.VISIBLE);

        db.collection("carts")
                .document(uid)
                .collection("items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading cart", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    cartList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            CartItem item = doc.toObject(CartItem.class);
                            item.setDocId(doc.getId());
                            cartList.add(item);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    calculateTotal();
                    if (loadingProgress != null) loadingProgress.setVisibility(android.view.View.GONE);
                });
    }

    private void setupBuyNowButton() {
        btnBuyNow.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (totalAmount <= 0) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            createOrderAndStartPayment();
        });
    }

    private void createOrderAndStartPayment() {
        currentOrderId = generateOrderId();

        // Convert CartItems to OrderItems
        List<Order.OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartList) {
            Order.OrderItem orderItem = new Order.OrderItem(
                    cartItem.getName(),
                    cartItem.getImageUrl(),
                    cartItem.getPrice(),
                    (int) cartItem.getQuantity()
            );
            orderItems.add(orderItem);
        }

        // Create order object
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Order order = new Order(currentOrderId, userId, orderItems, totalAmount,
                "Address pending", "Customer pending", "Phone pending");
        order.setPaymentMethod("UPI Payment");
        order.setPaymentStatus("INITIATED");

        // Save order to Firestore
        db.collection("orders")
                .document(currentOrderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order created successfully");
                    startUpiPayment();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating order", e);
                    Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show();
                });
    }

    private void startUpiPayment() {
        try {
            // Generate unique transaction ID
            String transactionId = "TXN" + System.currentTimeMillis();
            String description = "Order Payment - " + currentOrderId;

            // 🚀 ALTERNATIVE METHOD - Using Builder Pattern (if above method doesn't work)
            final EasyUpiPayment easyUpiPayment = new EasyUpiPayment.Builder(this)
                    .with(PaymentApp.ALL)
                    .setPayeeVpa(PAYEE_VPA)
                    .setPayeeName(PAYEE_NAME)
                    .setTransactionId(transactionId)
                    .setTransactionRefId(currentOrderId)
                    .setPayeeMerchantCode("000000") // Use "000000" for testing
                    .setDescription(description)
                    .setAmount(String.format("%.2f", totalAmount))
                    .build();

            // Set payment status listener
            easyUpiPayment.setPaymentStatusListener(this);

            // Update order with transaction ID
            updateOrderTransaction(transactionId);

            // Start payment
            easyUpiPayment.startPayment();

        } catch (Exception e) {
            Log.e(TAG, "Error starting payment", e);
            Toast.makeText(this, "Payment Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateOrderTransaction(String transactionId) {
        if (currentOrderId != null) {
            db.collection("orders")
                    .document(currentOrderId)
                    .update("transactionId", transactionId);
        }
    }

    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis();
    }

    private void calculateTotal() {
        totalAmount = 0;
        for (CartItem item : cartList) {
            totalAmount += item.getPrice() * item.getQuantity();
        }
        totalAmountText.setText("Total Amount: ₹" + String.format("%.2f", totalAmount));
        btnBuyNow.setEnabled(!cartList.isEmpty() && totalAmount > 0);
    }

    private void clearCart() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("carts")
                .document(uid)
                .collection("items")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference().delete();
                    }
                });
    }

    private void updateOrderStatus(String status) {
        if (currentOrderId != null) {
            String orderStatus = status.equals("SUCCESS") ? "Payment Successful" : "Payment " + status;

            db.collection("orders")
                    .document(currentOrderId)
                    .update("paymentStatus", status, "orderStatus", orderStatus);
            if ("SUCCESS".equals(status)) {
                decrementProductCountsForOrder(currentOrderId);
            }
        }
    }

    private void decrementProductCountsForOrder(String orderId) {
        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(doc -> {
                    Order order = doc.toObject(Order.class);
                    if (order == null || order.getItems() == null) return;
                    for (Order.OrderItem item : order.getItems()) {
                        String name = item.getProductName();
                        int qty = item.getQuantity();
                        if (name == null || qty <= 0) continue;
                        db.collection("products").whereEqualTo("name", name).get()
                                .addOnSuccessListener(productsSnap -> {
                                    for (com.google.firebase.firestore.DocumentSnapshot pdoc : productsSnap) {
                                        com.google.firebase.firestore.DocumentReference pref = pdoc.getReference();
                                        db.runTransaction(transaction -> {
                                            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(pref);
                                            Number current = (Number) snapshot.get("count");
                                            int newCount = (current == null ? 0 : current.intValue()) - qty;
                                            if (newCount < 0) newCount = 0;
                                            transaction.update(pref, "count", newCount);
                                            return null;
                                        });
                                    }
                                });
                    }
                });
    }

    // PaymentStatusListener implementations
    @Override
    public void onTransactionCompleted(TransactionDetails transactionDetails) {
        runOnUiThread(() -> {
            Toast.makeText(this, "✅ Payment Successful!\\nAmount: ₹" + totalAmount, Toast.LENGTH_LONG).show();
            updateOrderStatus("SUCCESS");
            clearCart();
            finish();
        });
    }

    @Override
    public void onTransactionCancelled() {
        runOnUiThread(() -> {
            Toast.makeText(this, "❌ Payment Cancelled", Toast.LENGTH_SHORT).show();
            updateOrderStatus("CANCELLED");
        });
    }

}