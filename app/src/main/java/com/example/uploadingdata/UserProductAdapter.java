package com.example.uploadingdata;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class UserProductAdapter extends RecyclerView.Adapter<UserProductAdapter.ProductViewHolder> implements Filterable {

    private List<Product> productList;
    private List<Product> productListFull;
    private Context context;

    public UserProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
        this.context = context;
        this.fullList = new ArrayList<>(productList);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.textPrice.setText("₹" + product.getPrice());
        Glide.with(context).load(product.getImageUrl()).into(holder.imageProduct);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("productName", product.getName());
            intent.putExtra("productType", product.getType());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("productImageUrl", product.getImageUrl());
            intent.putExtra("productModelUrl", product.getModelUrl());
            intent.putExtra("productDetails", product.getDetails());
            context.startActivity(intent);
        });

//        holder.btnAddToCart.setOnClickListener(v -> {
//            if (actionListener != null) {
//                actionListener.onAddToCart(product);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private final Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Product> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(productListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Product product : productListFull) {
                    if (product.getName().toLowerCase().contains(filterPattern)
                            || product.getType().toLowerCase().contains(filterPattern)) {
                        filteredList.add(product);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productList.clear();
            productList.addAll((List<Product>) results.values);
            notifyDataSetChanged();
        }
    };

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textPrice;
        TextView productName;
//        MaterialButton btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textPrice = itemView.findViewById(R.id.textPrice);
            productName = itemView.findViewById(R.id.productName);
//            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }

    private List<Product> fullList;

    public void setFullList(List<Product> fullList) {
        this.fullList = new ArrayList<>(fullList);
    }

    public void filter(String text) {
        if (fullList == null) {
            fullList = new ArrayList<>(productList);
        }
        List<Product> filteredList = new ArrayList<>();
        String q = text == null ? "" : text.toLowerCase();
        for (Product item : fullList) {
            String name = item.getName() != null ? item.getName().toLowerCase() : "";
            String type = item.getType() != null ? item.getType().toLowerCase() : "";
            if (name.contains(q) || type.contains(q)) {
                filteredList.add(item);
            }
        }
        productList = filteredList;
        notifyDataSetChanged();
    }
}
