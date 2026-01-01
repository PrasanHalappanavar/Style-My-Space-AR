package com.example.uploadingdata;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import java.util.List;

public class PaymentHelper {

    private static final String TAG = "PaymentHelper";

    // UPI App Package Names
    public static final String PHONEPE_PACKAGE = "com.phonepe.app";
    public static final String GOOGLE_PAY_PACKAGE = "com.google.android.apps.nbu.paisa.user";
    public static final String PAYTM_PACKAGE = "net.one97.paytm";
    public static final String BHIM_PACKAGE = "in.org.npci.upiapp";

    /**
     * Check if specific UPI app is installed
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get list of installed UPI apps
     */
    public static boolean hasUpiApps(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("upi://pay"));

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);

        return !activities.isEmpty();
    }

    /**
     * Build UPI payment URI
     */
    public static Uri buildUpiUri(String payeeVpa, String payeeName, String transactionNote,
                                  String amount, String transactionId) {
        return new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", payeeVpa)
                .appendQueryParameter("pn", payeeName)
                .appendQueryParameter("tn", transactionNote)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("tr", transactionId)
                .build();
    }

    /**
     * Create payment intent for specific app
     */
    public static Intent createPaymentIntent(String packageName, Uri upiUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, upiUri);
        if (packageName != null && !packageName.isEmpty()) {
            intent.setPackage(packageName);
        }
        return intent;
    }

    /**
     * Parse UPI payment response
     */
    public static PaymentResult parsePaymentResponse(String response) {
        if (response == null || response.isEmpty()) {
            return new PaymentResult("FAILED", "No response received", null, null);
        }

        Log.d(TAG, "Payment response: " + response);

        // Parse response parameters
        String[] params = response.split("&");
        String status = "UNKNOWN";
        String txnId = null;
        String responseCode = null;
        String approvalRefNo = null;

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].toLowerCase();
                String value = keyValue[1];

                switch (key) {
                    case "status":
                        status = value.toUpperCase();
                        break;
                    case "txnid":
                        txnId = value;
                        break;
                    case "responsecode":
                        responseCode = value;
                        break;
                    case "approvalrefno":
                        approvalRefNo = value;
                        break;
                }
            }
        }

        return new PaymentResult(status, null, txnId, responseCode);
    }

    /**
     * Payment result data class
     */
    public static class PaymentResult {
        private String status;
        private String message;
        private String transactionId;
        private String responseCode;

        public PaymentResult(String status, String message, String transactionId, String responseCode) {
            this.status = status;
            this.message = message;
            this.transactionId = transactionId;
            this.responseCode = responseCode;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getTransactionId() { return transactionId; }
        public String getResponseCode() { return responseCode; }

        public boolean isSuccessful() {
            return "SUCCESS".equalsIgnoreCase(status);
        }

        public boolean isPending() {
            return "PENDING".equalsIgnoreCase(status);
        }

        public boolean isFailed() {
            return "FAILURE".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status);
        }
    }
}
