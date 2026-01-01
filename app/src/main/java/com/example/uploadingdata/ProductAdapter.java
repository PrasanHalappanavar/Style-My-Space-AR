package com.example.uploadingdata;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        Glide.with(context).load(product.getImageUrl()).into(holder.productImage);

        holder.productName.setText(product.getName());
        holder.productPrice.setText("Price: ₹" + product.getPrice());

        TextView productCountView = holder.productCount;
        if (productCountView != null) {
            productCountView.setText("Count: " + product.getCount());
        }

        holder.btnEdit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_product, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            EditText editName = dialogView.findViewById(R.id.editName);
            EditText editPrice = dialogView.findViewById(R.id.editPrice);
            EditText editType = dialogView.findViewById(R.id.editType);
            Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);

            // Set existing values
            editName.setText(product.getName());
            editPrice.setText(String.valueOf(product.getPrice()));
            editType.setText(product.getType());

            btnUpdate.setOnClickListener(btn -> {
                String name = editName.getText().toString().trim();
                double price = Double.parseDouble(editPrice.getText().toString().trim());
                String type = editType.getText().toString().trim();

                db.collection("products").document(product.getId())
                        .update("name", name, "price", price, "type", type)
                        .addOnSuccessListener(aVoid -> {
                            product.setName(name);
                            product.setPrice(price);
                            product.setType(type);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Product updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                        );
            });
        });



        holder.btnDelete.setOnClickListener(v -> {
            db.collection("products").document(product.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        productList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error deleting item", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productCount;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.textProductName);
            productPrice = itemView.findViewById(R.id.textProductPrice);
            productCount = itemView.findViewById(R.id.textProductCount);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

}

