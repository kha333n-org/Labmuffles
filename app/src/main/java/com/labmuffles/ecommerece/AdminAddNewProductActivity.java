package com.labmuffles.ecommerece;

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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.labmuffles.ecommerece.prevelant.Prevelant;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String categoryName, productName, productDescription, productPrice, saveCurrentDate, saveCurrentTime;
    private Button addNewProduct;
    private EditText productNameInput, productDescriptionInput, productPriceInput;
    private ImageView productImageInput;
    private static final int GALLERY_PICK = 1;
    private Uri imageUri;
    private String productRandomKey, downloadImageUrl;
    private StorageReference productImageRef;
    private DatabaseReference productDbReference;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        categoryName = getIntent().getExtras().get(Prevelant.categoryKey).toString();
        addNewProduct = (Button) findViewById(R.id.add_new_product);
        productNameInput = (EditText) findViewById(R.id.product_name);
        productDescriptionInput = (EditText) findViewById(R.id.product_description);
        productPriceInput = (EditText) findViewById(R.id.product_price);
        productImageInput  = (ImageView)    findViewById(R.id.select_product_image);
        productImageRef = FirebaseStorage.getInstance().getReference().child(Prevelant.productsImagesFolder);
        productDbReference = FirebaseDatabase.getInstance().getReference().child(Prevelant.productDBRoot);
        loadingBar = new ProgressDialog(this);

        productImageInput.setOnClickListener(view -> {
            OpenGallery();
        });

        addNewProduct.setOnClickListener(view -> {
            validateProductData();

        });
    }

    private void validateProductData() {
        productDescription = productDescriptionInput.getText().toString();
        productName = productNameInput.getText().toString();
        productPrice = productPriceInput.getText().toString();

        if (imageUri == null){
            Toast.makeText(AdminAddNewProductActivity.this, "Product image is required", Toast.LENGTH_SHORT).show();
        }else
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
                storeProductInformation();
            }
    }

    private void storeProductInformation() {

        loadingBar.setTitle("Adding New Product");
        loadingBar.setMessage("Please wait, While product is being added...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime;

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
        productMap.put(Prevelant.productDBId, productRandomKey);
        productMap.put(Prevelant.productDBDate, saveCurrentDate);
        productMap.put(Prevelant.productDBTime, saveCurrentTime);
        productMap.put(Prevelant.productDBDescription, productDescription);
        productMap.put(Prevelant.productDBImage, downloadImageUrl);
        productMap.put(Prevelant.productDBCategory, categoryName);
        productMap.put(Prevelant.productDBPrice, productPrice);
        productMap.put(Prevelant.productDBName, productName);

        productDbReference.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        loadingBar.dismiss();
                        Toast.makeText(AdminAddNewProductActivity.this, "Product Added successfully", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(AdminAddNewProductActivity.this,AdminCategoryActivity.class));
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