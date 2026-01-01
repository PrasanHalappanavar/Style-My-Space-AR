package com.example.uploadingdata;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import okhttp3.*;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Add_products extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_MODEL_REQUEST = 2;

    private Uri imageUri, modelUri;
    private ImageView imageView;
    private FirebaseAuth mAuth;
    private TextView txtModelName;
    private EditText nameInput, priceInput, typeInput, detailsInput, countInput;
    private String imageUrl = null, modelUrl = null;
    private android.app.ProgressDialog progressDialog;
    private int pendingUploads = 0;

    private final String CLOUD_NAME = "doxv0gpjo";
    private final String UPLOAD_PRESET = "mymodels";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_products);

        mAuth = FirebaseAuth.getInstance();
        imageView = findViewById(R.id.imageupload);
        txtModelName = findViewById(R.id.txtModelName);
        nameInput = findViewById(R.id.name);
        priceInput = findViewById(R.id.price);
        typeInput = findViewById(R.id.type);
        detailsInput = findViewById(R.id.details);
        countInput = findViewById(R.id.count);

        findViewById(R.id.btnchooseImage).setOnClickListener(v -> openFileChooser(PICK_IMAGE_REQUEST));
        findViewById(R.id.btnchooseModel).setOnClickListener(v -> openFileChooser(PICK_MODEL_REQUEST));
        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            pendingUploads = 0;
            if (imageUri != null) pendingUploads++;
            if (modelUri != null) pendingUploads++;
            if (pendingUploads == 0) {
                Toast.makeText(this, "Select image and model to upload", Toast.LENGTH_SHORT).show();
                return;
            }
            progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setMessage("Uploading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            if (imageUri != null) uploadToCloudinary(imageUri, "image");
            if (modelUri != null) uploadToCloudinary(modelUri, "model");
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_add);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, AdminDashboard.class));
                return true;
            } else if (itemId == R.id.nav_add) {
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, AdminOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_logout) {
                getSharedPreferences("auth", MODE_PRIVATE).edit().putBoolean("admin_logged_in", false).apply();
                Intent intent = new Intent(Add_products.this, AdminLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean loggedIn = getSharedPreferences("auth", MODE_PRIVATE).getBoolean("admin_logged_in", false);
        if (!loggedIn) {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void openFileChooser(int type) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type == PICK_IMAGE_REQUEST ? "image/*" : "*/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), type);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            String fileName = FileUtils.getFileName(this, fileUri);

            if (requestCode == PICK_IMAGE_REQUEST) {
                if (fileName != null && (
                        fileName.toLowerCase().endsWith(".jpg") ||
                                fileName.toLowerCase().endsWith(".jpeg") ||
                                fileName.toLowerCase().endsWith(".png"))) {

                    imageUri = fileUri;
                    imageView.setImageURI(imageUri);
                } else {
                    Toast.makeText(this, "Only .jpg, .jpeg, or .png images are allowed", Toast.LENGTH_SHORT).show();
                    imageUri = null;
                    imageView.setImageDrawable(null);
                }

            } else if (requestCode == PICK_MODEL_REQUEST) {
                if (fileName != null && (fileName.toLowerCase().endsWith(".glb") || fileName.toLowerCase().endsWith(".sfb"))) {
                    modelUri = fileUri;
                    txtModelName.setText(fileName);
                } else {
                    Toast.makeText(this, "Only .glb or .sfb model files are allowed", Toast.LENGTH_SHORT).show();
                    modelUri = null;
                    txtModelName.setText("");
                }
            }
        }
    }

    private void uploadToCloudinary(Uri fileUri, String type) {
        File file = new File(FileUtils.getPath(this, fileUri));
        OkHttpClient client = new OkHttpClient();

        String path = FileUtils.getPath(this, fileUri);
        String lower = path != null ? path.toLowerCase() : "";
        MediaType mediaType;
        if (type.equals("model")) {
            if (lower.endsWith(".glb")) mediaType = MediaType.parse("model/gltf-binary");
            else mediaType = MediaType.parse("application/octet-stream");
        } else {
            mediaType = MediaType.parse("image/*");
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, mediaType))
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build();

        Request request = new Request.Builder()
                .url("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/auto/upload")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(Add_products.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    pendingUploads = Math.max(0, pendingUploads - 1);
                    if (progressDialog != null && progressDialog.isShowing() && pendingUploads == 0) {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String result = response.body().string();
                    String uploadedUrl = new JSONObject(result).getString("secure_url");
                    if (type.equals("image")) imageUrl = uploadedUrl;
                    else modelUrl = uploadedUrl;

                    pendingUploads = Math.max(0, pendingUploads - 1);
                    if (imageUrl != null && modelUrl != null) {
                        saveToFirestore();
                    } else if (pendingUploads == 0) {
                        runOnUiThread(() -> {
                            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                        });
                    }

                } catch (Exception e) {
                    Log.e("CloudinaryError", "Upload Error", e);
                    runOnUiThread(() -> {
                        Toast.makeText(Add_products.this, "Upload Error", Toast.LENGTH_SHORT).show();
                        pendingUploads = Math.max(0, pendingUploads - 1);
                        if (progressDialog != null && progressDialog.isShowing() && pendingUploads == 0) {
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void saveToFirestore() {
        String name = nameInput.getText().toString().trim();
        String price = priceInput.getText().toString().trim();
        String type = typeInput.getText().toString().trim();
        String details = detailsInput.getText().toString().trim();
        String countStr = countInput.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty() || type.isEmpty() || details.isEmpty() || countStr.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double priceN = Double.parseDouble(price);
        int countN;
        try { countN = Integer.parseInt(countStr); } catch (Exception e) { countN = 0; }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("price", priceN);
        data.put("type", type);
        data.put("details", details);
        data.put("count", countN);
        data.put("imageUrl", imageUrl);
        data.put("modelUrl", modelUrl);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Product Data Saved Successfully", Toast.LENGTH_SHORT).show();
                    if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

                    // Clear fields
                    nameInput.setText("");
                    priceInput.setText("");
                    typeInput.setText("");
                    detailsInput.setText("");
                    countInput.setText("");
                    imageView.setImageResource(0);
                    txtModelName.setText("");
                    imageUri = null;
                    modelUri = null;
                    imageUrl = null;
                    modelUrl = null;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                });
    }
}
