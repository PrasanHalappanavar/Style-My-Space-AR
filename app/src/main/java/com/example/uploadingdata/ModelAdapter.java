package com.example.uploadingdata;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

//public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ModelViewHolder> {
//    private List<ModelItem> modelList;
//    private OnItemClickListener listener;
//
//    public interface OnItemClickListener {
//        void onItemClick(ModelItem model);
//    }
//
//    public ModelAdapter(List<ModelItem> modelList, OnItemClickListener listener) {
//        this.modelList = modelList;
//        this.listener = listener;
//    }
//
//    public class ModelViewHolder extends RecyclerView.ViewHolder {
//        ImageView imageView;
//        TextView name;
//
//        public ModelViewHolder(View view) {
//            super(view);
//            imageView = view.findViewById(R.id.thumbnail);
//            name = view.findViewById(R.id.name);
//        }
//
//        public void bind(final ModelItem item) {
//            name.setText(item.getName());
//            Glide.with(imageView.getContext()).load(item.getThumbnailUrl()).into(imageView);
//            itemView.setOnClickListener(v -> listener.onItemClick(item));
//        }
//    }
//
//    @Override
//    public ModelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.model_item, parent, false);
//        return new ModelViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(ModelViewHolder holder, int position) {
//        holder.bind(modelList.get(position));
//    }
//
//    @Override
//    public int getItemCount() {
//        return modelList.size();
//    }
//}
//


public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ModelViewHolder> {
    private List<ModelItem> models;
    private Context context;

    public ModelAdapter(Context context, List<ModelItem> models) {
        this.context = context;
        this.models = models;
    }

    @NonNull
    @Override
    public ModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_model, parent, false);
        return new ModelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModelViewHolder holder, int position) {
        ModelItem item = models.get(position);
        Glide.with(context).load(item.getImageUrl()).into(holder.productImage);

        holder.itemView.setOnClickListener(v -> {
            Common.selectedModelUrl = item.getModelUrl();
            Toast.makeText(context, "Model selected!", Toast.LENGTH_SHORT).show();

            // Display info popup
            View view = LayoutInflater.from(context).inflate(R.layout.model_info_popup, null);
            TextView modelName = view.findViewById(R.id.infoModelName);
            TextView modelDesc = view.findViewById(R.id.infoModelDesc);
            modelName.setText(item.getName());
            modelDesc.setText(item.getDescription());

            BottomSheetDialog dialog = new BottomSheetDialog(context);
            dialog.setContentView(view);
            dialog.show();
        });

    }


    @Override
    public int getItemCount() {
        return models.size();
    }

    static class ModelViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;

        public ModelViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
        }
    }
}
