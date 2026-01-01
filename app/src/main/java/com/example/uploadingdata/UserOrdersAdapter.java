package com.example.uploadingdata;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class UserOrdersAdapter extends RecyclerView.Adapter<UserOrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    public UserOrdersAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        Order order = orderList.get(position);

        holder.txtOrderId.setText("Order ID: " + order.getOrderId());
        holder.txtTotal.setText("₹" + order.getTotalAmount());
        holder.txtPayment.setText("Payment: " + order.getPaymentStatus());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        holder.txtDate.setText("Date: " + sdf.format(order.getOrderDate()));

        String status = order.getOrderStatus();
        holder.txtStatus.setText("Status: " + status);

        // COLORS
        switch (status.toLowerCase()) {
            case "order cancelled":
                holder.txtStatus.setTextColor(Color.RED);
                break;
            case "delivered":
            case "completed":
                holder.txtStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "shipped":
            case "out for delivery":
                holder.txtStatus.setTextColor(Color.parseColor("#1565C0"));
                break;
            case "processing":
                holder.txtStatus.setTextColor(Color.parseColor("#FF8F00"));
                break;
            default:
                holder.txtStatus.setTextColor(Color.BLACK);
        }

        // CALCULATE cancel eligibility ONLY ONCE
        boolean isCancellable =
                !(status.toLowerCase().contains("shipped")
                        || status.toLowerCase().contains("delivered")
                        || status.toLowerCase().contains("completed")
                        || status.toLowerCase().contains("cancel"));

        // UI enable/disable
        holder.btnCancelOrder.setEnabled(isCancellable);
        holder.btnCancelOrder.setAlpha(isCancellable ? 1f : 0.4f);

        // FINAL position variable
        final int pos = position;

        // FIX: recalc inside click (so no error)
        holder.btnCancelOrder.setOnClickListener(v -> {

            // RECHECK HERE (so variable becomes effectively final)
            String stat = order.getOrderStatus().toLowerCase();
            boolean canCancel =
                    !(stat.contains("shipped")
                            || stat.contains("delivered")
                            || stat.contains("completed")
                            || stat.contains("cancel"));

            if (!canCancel) return;

            new AlertDialog.Builder(context)
                    .setTitle("Cancel Order")
                    .setMessage("Are you sure you want to cancel this order?")
                    .setPositiveButton("Yes", (dialog, which) -> cancelOrder(order, pos))
                    .setNegativeButton("No", null)
                    .show();
        });
    }



    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void cancelOrder(Order order, int position) {

        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Cancelling order...");
        pd.show();

        db.collection("orders")
                .document(order.getOrderId())
                .update("orderStatus", "Order Cancelled")
                .addOnSuccessListener(aVoid -> {
                    pd.dismiss();

                    // Update UI
                    order.setOrderStatus("Order Cancelled");
                    notifyItemChanged(position);

                    Toast.makeText(context, "Order Cancelled", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView txtOrderId, txtTotal, txtStatus, txtPayment, txtDate;
        Button btnCancelOrder;
        CardView orderCard;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            orderCard = itemView.findViewById(R.id.orderCard);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtPayment = itemView.findViewById(R.id.txtPayment);
            txtDate = itemView.findViewById(R.id.txtDate);

            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
        }
    }
}
