package com.example.uploadingdata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uploadingdata.R;
import com.example.uploadingdata.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> itemList;
    private Runnable onChange;
    private Context context;

    public CartAdapter(List<CartItem> itemList, Runnable onChange) {
        this.itemList = itemList;
        this.onChange = onChange;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = itemList.get(position);
        holder.name.setText(item.getName());
        holder.price.setText("Price: " + item.getPrice());
        holder.quantity.setText("Quantity: " + item.getQuantity());

        Glide.with(context).load(item.getImageUrl()).into(holder.image);

        holder.btnDelete.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance()
                    .collection("carts")
                    .document(uid)
                    .collection("items")
                    .document(item.getDocId())
                    .delete()
                    .addOnSuccessListener(unused -> {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < itemList.size()) {
                            itemList.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                            Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                            onChange.run(); // only updates total, doesn't reload all
                        }
                    });
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, quantity;
        ImageButton btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cartImage);
            name = itemView.findViewById(R.id.cartName);
            price = itemView.findViewById(R.id.cartPrice);
            quantity = itemView.findViewById(R.id.cartQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
