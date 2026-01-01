package com.example.uploadingdata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder> {

    private Context context;
    private List<Product> items;

    public RecyclerviewAdapter(Context context, List<Product> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_thumbnail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = items.get(position);
        holder.textName.setText(product.getName());

        Glide.with(context)
                .load(product.getImageUrl())
                .into(holder.imgThumb);

        holder.itemView.setOnClickListener(v -> {
            Common.selectedModelUrl = product.getModelUrl();  // ✅ Correct
            Toast.makeText(context, "Model Selected: " + product.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView textName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.thumb_image);
            textName = itemView.findViewById(R.id.thumb_name);
        }
    }
}
