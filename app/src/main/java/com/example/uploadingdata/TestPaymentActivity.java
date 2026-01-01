package com.example.uploadingdata;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import dev.shreyaspatil.easyupipayment.EasyUpiPayment;
import dev.shreyaspatil.easyupipayment.listener.PaymentStatusListener;
import dev.shreyaspatil.easyupipayment.model.PaymentApp;
import dev.shreyaspatil.easyupipayment.model.TransactionDetails;

public class TestPaymentActivity extends AppCompatActivity implements PaymentStatusListener {

    private static final String TAG = "TestPayment";

    private EditText etAmount, etUpiId, etName, etDescription;
    private Button btnPay;

    // REPLACE WITH YOUR DETAILS
    private static final String DEFAULT_UPI_ID = "9164282725@axl";
    private static final String DEFAULT_NAME = "Your Store Name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_payment);

        initViews();
        setDefaultValues();
        setupPayButton();
    }

    private void initViews() {
        etAmount = findViewById(R.id.etAmount);
        etUpiId = findViewById(R.id.etUpiId);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        btnPay = findViewById(R.id.btnPay);
    }

    private void setDefaultValues() {
        etAmount.setText("1.00"); // Test with ₹1
        etUpiId.setText(DEFAULT_UPI_ID);
        etName.setText(DEFAULT_NAME);
        etDescription.setText("Test Payment");
    }

    private void setupPayButton() {
        btnPay.setOnClickListener(v -> {
            String amount = etAmount.getText().toString().trim();
            String upiId = etUpiId.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (amount.isEmpty() || upiId.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amountValue = Double.parseDouble(amount);
                if (amountValue <= 0) {
                    Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            startPayment(amount, upiId, name, description);
        });
    }

    private void startPayment(String amount, String upiId, String name, String description) {
        try {
            // Generate transaction ID
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault());
            String transactionId = dateFormat.format(calendar.getTime());

            Log.d(TAG, "Starting payment with amount: " + amount + ", UPI: " + upiId);

            // Create payment
            EasyUpiPayment easyUpiPayment = new EasyUpiPayment.Builder(this)
//                    .setPaymentApp(PaymentApp.ALL)
                    .setPayeeVpa(upiId)
                    .setPayeeName(name)
                    .setTransactionId(transactionId)
                    .setTransactionRefId("REF" + transactionId)
                    .setDescription(description.isEmpty() ? "Test Payment" : description)
                    .setAmount(amount)
                    .build();

            // Set listener
            easyUpiPayment.setPaymentStatusListener(this);

            // Start payment
            easyUpiPayment.startPayment();

        } catch (Exception e) {
            Log.e(TAG, "Error starting payment", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTransactionCompleted(TransactionDetails transactionDetails) {
        Log.d(TAG, "Payment Success: " + transactionDetails.toString());

        runOnUiThread(() -> {
            Toast.makeText(this, "Payment Successful!\\n" +
                    "Transaction ID: " + transactionDetails.getTransactionId() + "\\n" +
                    "Amount: ₹" + transactionDetails.getAmount(), Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onTransactionCancelled() {
        Log.d(TAG, "Payment Cancelled");

        runOnUiThread(() -> {
            Toast.makeText(this, "Payment Cancelled by user", Toast.LENGTH_SHORT).show();
        });
    }

}