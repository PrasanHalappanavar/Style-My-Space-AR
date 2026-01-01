package com.example.uploadingdata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uploadingdata.R;
import com.example.uploadingdata.Order;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private List<Order> filteredOrders;
    private OnOrderClickListener onOrderClickListener;

    public interface OnOrderClickListener {
        void onUpdateStatusClick(Order order, int position);
        void onViewDetailsClick(Order order);
    }

    public OrdersAdapter(Context context) {
        this.context = context;
        this.orders = new ArrayList<>();
        this.filteredOrders = new ArrayList<>();
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.onOrderClickListener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        this.filteredOrders = new ArrayList<>(orders);
        notifyDataSetChanged();
    }

    public void filterOrders(String status) {
        filteredOrders.clear();
        if (status == null || status.equals("All")) {
            filteredOrders.addAll(orders);
        } else {
            for (Order order : orders) {
                if (order.getOrderStatus().equalsIgnoreCase(status)) {
                    filteredOrders.add(order);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void searchOrders(String query) {
        filteredOrders.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredOrders.addAll(orders);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Order order : orders) {
                boolean matches = false;

                // Search in order ID
                if (order.getOrderId() != null && order.getOrderId().toLowerCase().contains(lowerCaseQuery)) {
                    matches = true;
                }

                // Search in user ID
                if (order.getUserId() != null && order.getUserId().toLowerCase().contains(lowerCaseQuery)) {
                    matches = true;
                }

                // Search in transaction ID
                if (order.getTransactionId() != null && order.getTransactionId().toLowerCase().contains(lowerCaseQuery)) {
                    matches = true;
                }

                // Search in customer name
                if (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(lowerCaseQuery)) {
                    matches = true;
                }

                // Search in customer phone
                if (order.getCustomerPhone() != null && order.getCustomerPhone().toLowerCase().contains(lowerCaseQuery)) {
                    matches = true;
                }

                if (matches) {
                    filteredOrders.add(order);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = filteredOrders.get(position);
        holder.bind(order, position);
    }

    @Override
    public int getItemCount() {
        return filteredOrders.size();
    }

    public List<Order> getFilteredOrders() {
        return filteredOrders;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView orderIdText, orderStatusChip, orderDateText, totalAmountText;
        TextView customerIdText, itemsSummaryText, paymentMethodText, paymentStatusText;
        TextView transactionIdText, shippingAddressText;
        Button btnViewDetails, btnUpdateStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            orderIdText = itemView.findViewById(R.id.orderIdText);
            orderStatusChip = itemView.findViewById(R.id.orderStatusChip);
            orderDateText = itemView.findViewById(R.id.orderDateText);
            totalAmountText = itemView.findViewById(R.id.totalAmountText);
            customerIdText = itemView.findViewById(R.id.customerIdText);
            itemsSummaryText = itemView.findViewById(R.id.itemsSummaryText);
            paymentMethodText = itemView.findViewById(R.id.paymentMethodText);
            paymentStatusText = itemView.findViewById(R.id.paymentStatusText);
            transactionIdText = itemView.findViewById(R.id.transactionIdText);
            shippingAddressText = itemView.findViewById(R.id.shippingAddressText);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }

        public void bind(Order order, int position) {
            // Set basic order info
            orderIdText.setText("Order ID: " + (order.getOrderId() != null ? order.getOrderId() : "N/A"));
            totalAmountText.setText("₹" + String.format("%.2f", order.getTotalAmount()));

            // Display customer name and phone instead of just user ID
            if (order.getCustomerName() != null && !order.getCustomerName().trim().isEmpty()) {
                customerIdText.setText("Customer: " + order.getCustomerName() +
                        (order.getCustomerPhone() != null ? " (" + order.getCustomerPhone() + ")" : ""));
            } else {
                customerIdText.setText("Customer ID: " + (order.getUserId() != null ? order.getUserId() : "N/A"));
            }

            // Set payment info
            paymentMethodText.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A");
            paymentStatusText.setText(order.getPaymentStatus() != null ? order.getPaymentStatus() : "N/A");

            // Set transaction ID
            transactionIdText.setText("TXN: " + (order.getTransactionId() != null ? order.getTransactionId() : "N/A"));

            // Set shipping address
            shippingAddressText.setText("Address: " + (order.getShippingAddress() != null ? order.getShippingAddress() : "N/A"));

            // Format and set order date
            if (order.getOrderDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault());
                orderDateText.setText(sdf.format(order.getOrderDate()));
            } else {
                orderDateText.setText("Date: N/A");
            }

            // Set order status chip with appropriate color
            orderStatusChip.setText(order.getOrderStatus() != null ? order.getOrderStatus() : "Unknown");
            setStatusChipStyle(orderStatusChip, order.getOrderStatus());

            // Set payment status color
            if ("SUCCESS".equalsIgnoreCase(order.getPaymentStatus()) ||
                    "PAID".equalsIgnoreCase(order.getPaymentStatus()) ||
                    "COMPLETED".equalsIgnoreCase(order.getPaymentStatus())) {
                paymentStatusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else if ("PENDING".equalsIgnoreCase(order.getPaymentStatus())) {
                paymentStatusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
            } else {
                paymentStatusText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            }

            // Set items summary
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                StringBuilder itemsSummary = new StringBuilder();
                itemsSummary.append(order.getItems().size()).append(" item(s): ");

                for (int i = 0; i < Math.min(order.getItems().size(), 2); i++) {
                    Order.OrderItem item = order.getItems().get(i);
                    if (item.getProductName() != null) {
                        if (i > 0) itemsSummary.append(", ");
                        itemsSummary.append(item.getProductName());
                    }
                }

                if (order.getItems().size() > 2) {
                    itemsSummary.append("...");
                }

                itemsSummaryText.setText(itemsSummary.toString());
            } else {
                itemsSummaryText.setText("No items");
            }

            // Set click listeners
            btnViewDetails.setOnClickListener(v -> {
                if (onOrderClickListener != null) {
                    onOrderClickListener.onViewDetailsClick(order);
                }
            });

            btnUpdateStatus.setOnClickListener(v -> {
                if (onOrderClickListener != null) {
                    onOrderClickListener.onUpdateStatusClick(order, position);
                }
            });
        }

        private void setStatusChipStyle(TextView statusChip, String status) {
            int backgroundResource;

            if (status == null) {
                backgroundResource = R.drawable.status_chip_pending;
            } else {
                switch (status.toLowerCase()) {
                    case "placed":
                    case "pending":
                    case "payment successful":
                        backgroundResource = R.drawable.status_chip_pending;
                        break;
                    case "processing":
                    case "confirmed":
                        backgroundResource = R.drawable.status_chip_processing;
                        break;
                    case "shipped":
                    case "out for delivery":
                        backgroundResource = R.drawable.status_chip_shipped;
                        break;
                    case "delivered":
                    case "completed":
                        backgroundResource = R.drawable.status_chip_delivered;
                        break;
                    case "cancelled":
                    case "refunded":
                        backgroundResource = R.drawable.status_chip_cancelled;
                        break;
                    default:
                        backgroundResource = R.drawable.status_chip_pending;
                        break;
                }
            }

            statusChip.setBackgroundResource(backgroundResource);
        }
    }
}
