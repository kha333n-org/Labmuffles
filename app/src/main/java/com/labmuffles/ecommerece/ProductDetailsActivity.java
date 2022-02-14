package com.labmuffles.ecommerece;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labmuffles.ecommerece.model.Products;
import com.labmuffles.ecommerece.prevelant.Prevalent;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity {

    private Button addToCartBtn;
    private ImageView productImage;
    private ElegantNumberButton numberButton;
    private TextView productPrice, productDescription, productName;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        addToCartBtn = findViewById(R.id.add_to_cart_button_details);
        productImage = findViewById(R.id.product_image_details);
        numberButton = findViewById(R.id.product_quantity_details);
        productPrice = findViewById(R.id.product_price_details);
        productDescription = findViewById(R.id.product_description_details);
        productName = findViewById(R.id.product_name_details);

        productId = getIntent().getStringExtra("pid");

        getProductDetails(productId);

        addToCartBtn.setOnClickListener(v -> {
            addToCart();
        });

    }

    private void addToCart() {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");

        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentDate.format(calendar.getTime());

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.cartDBRoot);

        HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put(Prevalent.cartDBPid, productId);
        cartMap.put(Prevalent.productDBName, productName.getText().toString());
        cartMap.put(Prevalent.productDBPrice, productPrice.getText().toString());
        cartMap.put(Prevalent.productDBDescription, productDescription.getText().toString());
        cartMap.put(Prevalent.productDBQuantity, numberButton.getNumber());
        cartMap.put(Prevalent.cartDBDate, saveCurrentDate);
        cartMap.put(Prevalent.cartDBTime, saveCurrentTime);
        cartMap.put(Prevalent.cartDBDiscount, "");

        cartListRef.child(Prevalent.cartDBUserView).child(Prevalent.currentOnlineUser.getPhone())
                .child(Prevalent.cartDBProductsChild).child(productId).updateChildren(cartMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cartListRef.child(Prevalent.cartDBAdminView).child(Prevalent.currentOnlineUser.getPhone())
                                .child(Prevalent.cartDBProductsChild).child(productId).updateChildren(cartMap).addOnCompleteListener(task1 -> {
                                    Toast.makeText(ProductDetailsActivity.this, "Added to cart list", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(ProductDetailsActivity.this, HomeActivity.class));
                                });
                    }
                });

    }

    private void getProductDetails(String productId) {

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.productDBRoot);
        productsRef.child(productId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Products products = snapshot.getValue(Products.class);

                    Picasso.get().load(products.getImage()).into(productImage);
                    productPrice.setText(products.getPrice());
                    productDescription.setText(products.getDescription());
                    productName.setText(products.getName());
                    Picasso.get().load(products.getImage()).into(productImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}