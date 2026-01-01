package com.example.uploadingdata;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.PixelCopy;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ImageButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.Stack;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private Button btnRemoveAll, btnScreenshot, btnResetModel, btnToggleInfo;
    private ImageButton btnUndo, btnRedo, btnReloadModels;
    private RecyclerView recyclerView;
    private ModelAdapter adapter;
    private TextView dimensionsDisplay, modelInfoText, lightingControl;

    private List<ModelItem> modelList = new ArrayList<>();
    private final List<AnchorNode> placedAnchorNodes = new ArrayList<>();
    private FirebaseFirestore db;
    private TransformableNode lastPlacedNode;
    private Node dimensionLabelNode;
    private final Stack<ModelState> undoStack = new Stack<>();
    private final Stack<ModelState> redoStack = new Stack<>();

    private static class ModelState {
        Pose pose;
        String modelUrl;
        Vector3 scale;
        ModelState(Pose p, String u, Vector3 s) { this.pose = p; this.modelUrl = u; this.scale = s; }
    }

    // UI Control Variables
    private boolean showDimensions = true;
    private boolean showModelInfo = true;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        setupRecyclerView();
        setupBottomNavigation();
        setupARFragment();
        setupControlButtons();
        setupSceneUpdateListener();
        String incomingModelUrl = getIntent().getStringExtra("modelUrl");
        if (incomingModelUrl != null && !incomingModelUrl.isEmpty()) {
            Common.selectedModelUrl = incomingModelUrl;
            Toast.makeText(this, "Model selected. Tap on a plane to place.", Toast.LENGTH_SHORT).show();
        }
        fetchModels();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void initializeComponents() {
        db = FirebaseFirestore.getInstance();
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        btnRemoveAll = findViewById(R.id.btnRemoveAll);
        btnScreenshot = findViewById(R.id.btnScreenshot);
        btnResetModel = findViewById(R.id.btnResetModel);
        btnToggleInfo = findViewById(R.id.btnToggleInfo);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnReloadModels = findViewById(R.id.btnReloadModels);
        recyclerView = findViewById(R.id.modelRecyclerView);
        dimensionsDisplay = findViewById(R.id.dimensionsDisplay);
        modelInfoText = findViewById(R.id.modelInfoText);
        lightingControl = findViewById(R.id.lightingControl);
        if (btnReloadModels != null) btnReloadModels.setOnClickListener(v -> fetchModels());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new ModelAdapter(this, modelList);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_camera);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, UserHomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_camera) {
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupARFragment() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (Common.selectedModelUrl == null) {
                Toast.makeText(this, "Please select a model first", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Loading model...");
            dialog.setCancelable(false);
            dialog.show();

            Anchor anchor = hitResult.createAnchor();
            String url = Common.selectedModelUrl;
            boolean isSfb = url.toLowerCase().endsWith(".sfb");

            if (isSfb) {
                placeSfbModel(url, anchor, dialog);
            } else {
                ModelRenderable.builder()
                        .setSource(this, android.net.Uri.parse(url))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(renderable -> {
                            dialog.dismiss();
                            addModelToScene(anchor, renderable);
                        })
                        .exceptionally(err -> {
                            dialog.dismiss();
                            runOnUiThread(() -> Toast.makeText(this, "Failed to load model: " + err.getMessage(), Toast.LENGTH_LONG).show());
                            Log.e("ARModel", "Error loading GLB", err);
                            return null;
                        });
            }
        });
    }

    private void setupControlButtons() {
        btnRemoveAll.setOnClickListener(v -> removeAllAnchorNodes());
        btnScreenshot.setOnClickListener(v -> takeScreenshot());
        btnResetModel.setOnClickListener(v -> resetLastModel());
        btnToggleInfo.setOnClickListener(v -> toggleInfoDisplay());
        if (btnUndo != null) btnUndo.setOnClickListener(v -> undoLast());
        if (btnRedo != null) btnRedo.setOnClickListener(v -> redoLast());
        lightingControl.setOnClickListener(v -> adjustLighting());
    }

    private void fetchModels() {
        db.collection("products").get()
                .addOnSuccessListener(query -> {
                    modelList.clear();
                    for (DocumentSnapshot doc : query) {
                        String imageUrl = doc.getString("imageUrl");
                        String modelUrl = doc.getString("modelUrl");
                        String name = doc.getString("name");
                        String desc = doc.getString("type");
                        if (imageUrl != null && modelUrl != null) {
                            modelList.add(new ModelItem(imageUrl, modelUrl, name, desc));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching products", e));
    }

    private void addModelToScene(Anchor anchor, ModelRenderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());

        node.getScaleController().setMinScale(0.1f);
        node.getScaleController().setMaxScale(3.0f);
        node.getRotationController().setEnabled(true);
        node.getTranslationController().setEnabled(true);

        node.setParent(anchorNode);
        node.setRenderable(renderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();

        placedAnchorNodes.add(anchorNode);
        lastPlacedNode = node;
        node.setName(Common.selectedModelUrl);
        undoStack.push(new ModelState(anchor.getPose(), Common.selectedModelUrl, node.getWorldScale()));
        redoStack.clear();

        addModelLabel(node);

        if (showDimensions) {
            addDimensionDisplay(node);
        }

        updateDimensionsDisplay();
        updateModelInfo();
    }

    private void setupSceneUpdateListener() {
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (lastPlacedNode != null) {
                updateDimensionsDisplay();
                if (dimensionLabelNode != null && dimensionLabelNode.getRenderable() instanceof ViewRenderable) {
                    ViewRenderable vr = (ViewRenderable) dimensionLabelNode.getRenderable();
                    updateDimensionLabel(vr);
                }
            }
        });
    }

    private void undoLast() {
        if (placedAnchorNodes.isEmpty()) return;
        AnchorNode anchorNode = placedAnchorNodes.remove(placedAnchorNodes.size() - 1);
        TransformableNode node = null;
        List<Node> children = anchorNode.getChildren();
        if (!children.isEmpty()) {
            Node child = children.get(0);
            if (child instanceof TransformableNode) node = (TransformableNode) child;
        }
        Vector3 currentScale = node != null ? node.getWorldScale() : new Vector3(1f,1f,1f);
        String url = node != null ? node.getName() : Common.selectedModelUrl;
        Pose pose = anchorNode.getAnchor() != null ? anchorNode.getAnchor().getPose() : null;
        if (pose != null && url != null) {
            redoStack.push(new ModelState(pose, url, currentScale));
        }
        if (anchorNode.getAnchor() != null) anchorNode.getAnchor().detach();
        anchorNode.setParent(null);
        arFragment.getArSceneView().getScene().removeChild(anchorNode);
        lastPlacedNode = placedAnchorNodes.isEmpty() ? null : getLastTransformable();
        updateModelInfo();
        updateDimensionsDisplay();
    }

    private TransformableNode getLastTransformable() {
        if (placedAnchorNodes.isEmpty()) return null;
        AnchorNode an = placedAnchorNodes.get(placedAnchorNodes.size() - 1);
        List<Node> children = an.getChildren();
        if (!children.isEmpty() && children.get(0) instanceof TransformableNode) {
            return (TransformableNode) children.get(0);
        }
        return null;
    }

    private void redoLast() {
        if (redoStack.isEmpty()) return;
        ModelState s = redoStack.pop();
        try {
            Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(s.pose);
            String url = s.modelUrl;
            boolean isSfb = url.toLowerCase().endsWith(".sfb");
            if (isSfb) {
                placeSfbModel(url, anchor, null);
            } else {
                ModelRenderable.builder()
                        .setSource(this, android.net.Uri.parse(url))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(renderable -> {
                            addModelToScene(anchor, renderable);
                            if (lastPlacedNode != null && s.scale != null) {
                                lastPlacedNode.setLocalScale(s.scale);
                            }
                        });
            }
        } catch (Exception ignored) {}
    }

    private void placeSfbModel(String url, Anchor anchor, ProgressDialog dialog) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        if (dialog != null) dialog.dismiss();
                        Toast.makeText(MainActivity.this, "Failed to download .sfb model", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                File dir = new File(getCacheDir(), "models");
                if (!dir.exists()) dir.mkdirs();

                String name = url.substring(url.lastIndexOf('/') + 1);
                File outFile = new File(dir, name);

                try (InputStream in = response.body().byteStream();
                     FileOutputStream out = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = in.read(buf)) != -1) {
                        out.write(buf, 0, r);
                    }
                }

                runOnUiThread(() -> {
                    try {
                        ModelRenderable.builder()
                                .setSource(MainActivity.this, android.net.Uri.fromFile(outFile))
                                .build()
                                .thenAccept(renderable -> {
                                    if (dialog != null) dialog.dismiss();
                                    addModelToScene(anchor, renderable);
                                })
                                .exceptionally(err -> {
                                    if (dialog != null) dialog.dismiss();
                                    Log.e("ARModel", "SFB load error", err);
                                    Toast.makeText(MainActivity.this, "Failed to load .sfb: " + err.getMessage(), Toast.LENGTH_LONG).show();
                                    return null;
                                });
                    } catch (Exception ex) {
                        if (dialog != null) dialog.dismiss();
                        Log.e("ARModel", "SFB builder error", ex);
                        Toast.makeText(MainActivity.this, "Error creating .sfb renderable: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (dialog != null) dialog.dismiss();
                    Log.e("ARModel", "SFB download error", e);
                    Toast.makeText(MainActivity.this, "Error downloading .sfb: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void addModelLabel(TransformableNode node) {
        ViewRenderable.builder()
                .setView(this, R.layout.model_label_layout)
                .build()
                .thenAccept(viewRenderable -> {
                    Node labelNode = new Node();
                    labelNode.setParent(node);
                    labelNode.setRenderable(viewRenderable);
                    labelNode.setLocalPosition(new Vector3(0.0f, 0.3f, 0.0f));

                    TextView labelText = viewRenderable.getView().findViewById(R.id.labelText);
                    labelText.setText(getSelectedModelName());
                });
    }

    private void addDimensionDisplay(TransformableNode node) {
        ViewRenderable.builder()
                .setView(this, R.layout.dimension_display_layout)
                .build()
                .thenAccept(viewRenderable -> {
                    dimensionLabelNode = new Node();
                    dimensionLabelNode.setParent(node);
                    dimensionLabelNode.setRenderable(viewRenderable);
                    dimensionLabelNode.setLocalPosition(new Vector3(0.0f, 0.5f, 0.0f));

                    updateDimensionLabel(viewRenderable);
                });
    }

    private void updateDimensionLabel(ViewRenderable viewRenderable) {
        if (lastPlacedNode != null && viewRenderable != null) {
            Vector3 dimensions = calculateRealWorldDimensions();
            TextView widthText = viewRenderable.getView().findViewById(R.id.widthText);
            TextView heightText = viewRenderable.getView().findViewById(R.id.heightText);
            TextView depthText = viewRenderable.getView().findViewById(R.id.depthText);

            if (widthText != null && heightText != null && depthText != null) {
                widthText.setText(String.format("W: %.2f m", dimensions.x));
                heightText.setText(String.format("H: %.2f m", dimensions.y));
                depthText.setText(String.format("D: %.2f m", dimensions.z));
            }
        }
    }

    private Vector3 calculateRealWorldDimensions() {
        if (lastPlacedNode == null || lastPlacedNode.getRenderable() == null) {
            return new Vector3(0, 0, 0);
        }

        try {
            Box box = (Box) lastPlacedNode.getRenderable().getCollisionShape();
            Vector3 renderableSize = box.getSize();
            Vector3 nodeScale = lastPlacedNode.getWorldScale();

            return new Vector3(
                    renderableSize.x * nodeScale.x,
                    renderableSize.y * nodeScale.y,
                    renderableSize.z * nodeScale.z
            );
        } catch (Exception e) {
            Log.e("DimensionCalc", "Error calculating dimensions", e);
            return new Vector3(0, 0, 0);
        }
    }

    private void updateDimensionsDisplay() {
        if (dimensionsDisplay != null && showDimensions) {
            Vector3 dimensions = calculateRealWorldDimensions();
            String dimensionText = String.format("L: %.2fm  W: %.2fm  H: %.2fm",
                    dimensions.x, dimensions.z, dimensions.y);
            dimensionsDisplay.setText(dimensionText);
        }
    }

    private void updateModelInfo() {
        if (modelInfoText != null && showModelInfo) {
            String modelName = getSelectedModelName();
            int objectCount = placedAnchorNodes.size();
            String infoText = String.format("Model: %s\nObjects: %d", modelName, objectCount);
            modelInfoText.setText(infoText);
        }
    }

    private void removeAllAnchorNodes() {
        for (AnchorNode node : placedAnchorNodes) {
            if (node != null) {
                if (node.getAnchor() != null) {
                    node.getAnchor().detach();
                }
                node.setParent(null);
                arFragment.getArSceneView().getScene().removeChild(node);
            }
        }
        placedAnchorNodes.clear();
        lastPlacedNode = null;
        dimensionLabelNode = null;
        updateDimensionsDisplay();
        updateModelInfo();
        Toast.makeText(this, "All objects removed", Toast.LENGTH_SHORT).show();
    }

    private void resetLastModel() {
        if (lastPlacedNode != null) {
            lastPlacedNode.setLocalScale(new Vector3(1f, 1f, 1f));
            lastPlacedNode.setLocalPosition(Vector3.zero());
            updateDimensionsDisplay();
            Toast.makeText(this, "Model reset to original size and position", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No model to reset", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleInfoDisplay() {
        showModelInfo = !showModelInfo;
        showDimensions = !showDimensions;

        if (showModelInfo) {
            modelInfoText.setVisibility(android.view.View.VISIBLE);
            dimensionsDisplay.setVisibility(android.view.View.VISIBLE);
            btnToggleInfo.setText("Hide Info");
        } else {
            modelInfoText.setVisibility(android.view.View.GONE);
            dimensionsDisplay.setVisibility(android.view.View.GONE);
            btnToggleInfo.setText("Show Info");
        }

        updateModelInfo();
        updateDimensionsDisplay();
    }

    private void adjustLighting() {
        if (arFragment.getArSceneView().getScene().getCamera() != null) {
            Toast.makeText(this, "Lighting adjusted", Toast.LENGTH_SHORT).show();
        }
    }

    private void takeScreenshot() {
        final Bitmap bitmap = Bitmap.createBitmap(
                arFragment.getArSceneView().getWidth(),
                arFragment.getArSceneView().getHeight(),
                Bitmap.Config.ARGB_8888
        );

        PixelCopy.request(arFragment.getArSceneView(), bitmap, copyResult -> {
            if (copyResult == PixelCopy.SUCCESS) {
                String savedImageURL = MediaStore.Images.Media.insertImage(
                        getContentResolver(), bitmap, "AR_Furniture_" + System.currentTimeMillis(),
                        "AR Furniture Placement"
                );
                if (savedImageURL != null) {
                    Toast.makeText(this, "Screenshot saved to gallery!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Screenshot capture failed", Toast.LENGTH_SHORT).show();
            }
        }, new Handler(Looper.getMainLooper()));
    }

    private String getSelectedModelName() {
        for (ModelItem item : modelList) {
            if (item.getModelUrl().equals(Common.selectedModelUrl)) {
                return item.getName();
            }
        }
        return "Furniture Item";
    }
}