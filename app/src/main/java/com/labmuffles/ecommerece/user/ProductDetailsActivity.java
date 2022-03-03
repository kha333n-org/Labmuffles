package com.labmuffles.ecommerece.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.Calculations;
import com.labmuffles.ecommerece.model.Products;
import com.labmuffles.ecommerece.prevelant.Prevalent;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity {

    private Button addToCartBtn;
    private ImageView productImage;
    private ElegantNumberButton numberButton;
    private TextView productPrice, productDescription, productName;
    private String productId;
    private EditText numberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

            productId = getIntent().getStringExtra(Prevalent.cartDBPid);

        addToCartBtn = findViewById(R.id.add_to_cart_button_details);
        productImage = findViewById(R.id.product_image_details);
        numberButton = findViewById(R.id.product_quantity_details);
        productPrice = findViewById(R.id.product_price_details);
        productDescription = findViewById(R.id.product_description_details);
        productName = findViewById(R.id.product_name_details);
        numberEditText = findViewById(R.id.product_quantity_edittext_details);
        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (numberEditText.getText().toString().equals("")) {

                }else
                if (Integer.parseInt(numberEditText.getText().toString()) == 0) {
                    numberEditText.setText("1");
                }else {
                    numberButton.setNumber(numberEditText.getText().toString());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        numberButton.setOnClickListener((ElegantNumberButton.OnClickListener) v -> {
                numberEditText.setText(numberButton.getNumber());
        });

        getProductDetails(productId);

        addToCartBtn.setOnClickListener(v -> {
                addToCart();
        });

    }


    private void addToCart() {

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.cartDBRoot);
        final DatabaseReference orderListRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.productsPerOrder);

        HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put(Prevalent.cartDBPid, productId);
        cartMap.put(Prevalent.productDBName, productName.getText().toString());
        cartMap.put(Prevalent.productDBPrice, productPrice.getText().toString());
        cartMap.put(Prevalent.productDBQuantity, numberButton.getNumber());
        cartMap.put(Prevalent.cartDBDate, Calculations.getCurrentDate());
        cartMap.put(Prevalent.cartDBTime, Calculations.getCurrentTime());
        cartMap.put(Prevalent.cartDBDiscount, "");

        cartListRef.child(Prevalent.currentOnlineUser.getPhone()).child(Prevalent.currentOnlineUser.getOrderId())
                .child(Prevalent.cartDBProductsChild).child(productId).updateChildren(cartMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderListRef.child(Prevalent.currentOnlineUser.getPhone()).child(Prevalent.currentOnlineUser.getOrderId())
                                .child(Prevalent.cartDBProductsChild).child(productId).updateChildren(cartMap).addOnCompleteListener(task1 -> {
                                    Toast.makeText(ProductDetailsActivity.this, "Added to cart list", Toast.LENGTH_SHORT).show();
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}