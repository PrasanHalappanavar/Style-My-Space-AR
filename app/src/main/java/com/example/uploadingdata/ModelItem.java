package com.example.uploadingdata;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class ModelItem implements Parcelable {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String modelUrl;
    private String category;
    private double price;
    private float popularity;
    private List<String> tags;
    private String style;
    private String material;
    private Dimensions dimensions;
    private boolean isFeatured;
    private long timestamp;

    public ModelItem() {
        // Default constructor for Firebase
    }

    public ModelItem(String imageUrl, String modelUrl, String name, String description) {
        this.imageUrl = imageUrl;
        this.modelUrl = modelUrl;
        this.name = name;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }

    // Parcelable implementation
    protected ModelItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        modelUrl = in.readString();
        category = in.readString();
        price = in.readDouble();
        popularity = in.readFloat();
        tags = in.createStringArrayList();
        style = in.readString();
        material = in.readString();
        dimensions = in.readParcelable(Dimensions.class.getClassLoader());
        isFeatured = in.readByte() != 0;
        timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(modelUrl);
        dest.writeString(category);
        dest.writeDouble(price);
        dest.writeFloat(popularity);
        dest.writeStringList(tags);
        dest.writeString(style);
        dest.writeString(material);
        dest.writeParcelable(dimensions, flags);
        dest.writeByte((byte) (isFeatured ? 1 : 0));
        dest.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ModelItem> CREATOR = new Creator<ModelItem>() {
        @Override
        public ModelItem createFromParcel(Parcel in) {
            return new ModelItem(in);
        }

        @Override
        public ModelItem[] newArray(int size) {
            return new ModelItem[size];
        }
    };

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getModelUrl() { return modelUrl; }
    public void setModelUrl(String modelUrl) { this.modelUrl = modelUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public float getPopularity() { return popularity; }
    public void setPopularity(float popularity) { this.popularity = popularity; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public Dimensions getDimensions() { return dimensions; }
    public void setDimensions(Dimensions dimensions) { this.dimensions = dimensions; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public static class Dimensions implements Parcelable {
        private float width;
        private float height;
        private float depth;

        public Dimensions() {}

        public Dimensions(float width, float height, float depth) {
            this.width = width;
            this.height = height;
            this.depth = depth;
        }

        protected Dimensions(Parcel in) {
            width = in.readFloat();
            height = in.readFloat();
            depth = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(width);
            dest.writeFloat(height);
            dest.writeFloat(depth);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Dimensions> CREATOR = new Creator<Dimensions>() {
            @Override
            public Dimensions createFromParcel(Parcel in) {
                return new Dimensions(in);
            }

            @Override
            public Dimensions[] newArray(int size) {
                return new Dimensions[size];
            }
        };

        // Getters and Setters
        public float getWidth() { return width; }
        public void setWidth(float width) { this.width = width; }

        public float getHeight() { return height; }
        public void setHeight(float height) { this.height = height; }

        public float getDepth() { return depth; }
        public void setDepth(float depth) { this.depth = depth; }
    }
}