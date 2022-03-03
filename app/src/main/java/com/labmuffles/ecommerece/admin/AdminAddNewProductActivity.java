package com.labmuffles.ecommerece.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.Calculations;
import com.labmuffles.ecommerece.model.Products;
import com.labmuffles.ecommerece.prevelant.Prevalent;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String productName, productDescription, productPrice;
    private Button addNewProduct, deleteProduct;
    private EditText productNameInput, productDescriptionInput, productPriceInput;
    private ImageView productImageInput;
    private static final int GALLERY_PICK = 1;
    private String oldImageUrl = "";
    private Uri imageUri;
    private String productRandomKey, downloadImageUrl;
    private StorageReference productImageRef;
    private DatabaseReference productDbReference;
    private ProgressDialog loadingBar;
    private String state = "new", productId, imageState = "new";
    private DatabaseReference productRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        deleteProduct = findViewById(R.id.delete_product_button);
        addNewProduct = (Button) findViewById(R.id.add_new_product);
        productNameInput = (EditText) findViewById(R.id.product_name);
        productDescriptionInput = (EditText) findViewById(R.id.product_description);
        productPriceInput = (EditText) findViewById(R.id.product_price);
        productImageInput  = (ImageView)    findViewById(R.id.select_product_image);
        productImageRef = FirebaseStorage.getInstance().getReference().child(Prevalent.productsImagesFolder);
        productDbReference = FirebaseDatabase.getInstance().getReference().child(Prevalent.productDBRoot);
        loadingBar = new ProgressDialog(this);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        if (bundle != null && bundle.containsKey("state")){
            state = getIntent().getStringExtra("state");
            productId = getIntent().getStringExtra("pid");
            productRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.productDBRoot).child(productId);
            deleteProduct.setVisibility(View.VISIBLE);
        }

        if (state.equals("update")) {
            displayProductInformation();
        }

        productImageInput.setOnClickListener(view -> {
            OpenGallery();
        });

        addNewProduct.setOnClickListener(view -> {
            validateProductData();

        });

        deleteProduct.setOnClickListener(view -> {
            deleteProduct();
        } );
    }

    private void deleteProduct() {
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
        imageRef.delete().addOnSuccessListener(aVoid -> {
            productRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Product Deleted Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AdminAddNewProductActivity.this, AdminDashboardActivity.class));
                    finish();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete product image", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayProductInformation() {

        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    Products products = snapshot.getValue(Products.class);

                    Picasso.get().load(products.getImage()).into(productImageInput);
                    oldImageUrl = products.getImage();
                    productPriceInput.setText(products.getPrice());
                    productDescriptionInput.setText(products.getDescription());
                    productNameInput.setText(products.getName());


                    addNewProduct.setText("Update Product");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void validateProductData() {
        productDescription = productDescriptionInput.getText().toString();
        productName = productNameInput.getText().toString();
        productPrice = productPriceInput.getText().toString();

//        if (state.equals("update")){
//            productRandomKey = productId;
//            saveProductInformationToDatabase();
//        }else

        if (imageUri == null && state.equals("new")){
            Toast.makeText(AdminAddNewProductActivity.this, "Product image is required", Toast.LENGTH_SHORT).show();
        }else
            if(imageUri == null && state.equals("update")){
                productRandomKey = productId;
            }
            if (TextUtils.isEmpty(productDescription)){
                Toast.makeText(AdminAddNewProductActivity.this, "Product description is required", Toast.LENGTH_SHORT).show();
            }else
            if (TextUtils.isEmpty(productName)){
                Toast.makeText(AdminAddNewProductActivity.this, "Product name is required", Toast.LENGTH_SHORT).show();
            }else
            if (TextUtils.isEmpty(productPrice)){
                Toast.makeText(AdminAddNewProductActivity.this, "Product price is required", Toast.LENGTH_SHORT).show();
            }
            else {
                if (state.equals("new")){
                    storeProductInformation();
                }else if (state.equals("update") && imageUri == null){
                    imageState = "old";
                    saveProductInformationToDatabase();

                }else if (state.equals("update") && imageUri != null){
                    storeProductInformation();
                }

            }
    }

    private void storeProductInformation() {

        loadingBar.setTitle("Updating Product Information");
        loadingBar.setMessage("Please wait, While product is being updated...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        if (state.equals("new")) {
            productRandomKey = Calculations.getCurrentDate() + Calculations.getCurrentTime();
        }else if (state.equals("update")){
            productRandomKey = productId;
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
            imageRef.delete().addOnSuccessListener(aVoid -> {

            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete product image", Toast.LENGTH_SHORT).show();
            });
        }

        StorageReference filePath = productImageRef.child(imageUri.getLastPathSegment() + productRandomKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(e -> {
            String message = e.toString();
            Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
        }).addOnSuccessListener(e -> {
            Toast.makeText(AdminAddNewProductActivity.this, "Image uploaded successfully.", Toast.LENGTH_SHORT).show();
            Task<Uri> uriTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()){
                    loadingBar.dismiss();
                    throw task.getException();
                }
                downloadImageUrl = filePath.getDownloadUrl().toString();
                return filePath.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    downloadImageUrl = task.getResult().toString();
                    Toast.makeText(AdminAddNewProductActivity.this, "Product Image URL got", Toast.LENGTH_SHORT).show();
                    saveProductInformationToDatabase();
                }
            });
        });
    }

    private void saveProductInformationToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put(Prevalent.productDBId, productRandomKey);
        productMap.put(Prevalent.productDBDate, Calculations.getCurrentDate());
        productMap.put(Prevalent.productDBTime, Calculations.getCurrentTime());
        productMap.put(Prevalent.productDBDescription, productDescription);
        if (imageState.equals("new")){
            productMap.put(Prevalent.productDBImage, downloadImageUrl);
        }else {
            productMap.put(Prevalent.productDBImage, oldImageUrl);
        }
        productMap.put(Prevalent.productDBPrice, productPrice);
        productMap.put(Prevalent.productDBName, productName);

        productDbReference.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        loadingBar.dismiss();
                        Toast.makeText(AdminAddNewProductActivity.this, "Product Added successfully", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(AdminAddNewProductActivity.this, AdminDashboardActivity.class));
                        finish();
                    }
                    else {
                        loadingBar.dismiss();
                        Toast.makeText(AdminAddNewProductActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            productImageInput.setImageURI(imageUri);
        }
    }
}