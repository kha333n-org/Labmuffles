package com.labmuffles.ecommerece.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.Calculations;
import com.labmuffles.ecommerece.model.Cart;
import com.labmuffles.ecommerece.model.Products;
import com.labmuffles.ecommerece.prevelant.Prevalent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.labmuffles.ecommerece.user.CartActivity;
import com.labmuffles.ecommerece.viewholder.CartViewHolder;

import java.util.HashMap;


public class AdminUserProductsActivity extends AppCompatActivity {

    private RecyclerView productsList;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference cartListRef, orderRef;
    private String userId = "", orderId = "", userType = "";
    private TextView name, phone, totalAmount, dateTime, status, discount;
    private EditText discountEditText;
    private Button discountUpdateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_products);
        name = findViewById(R.id.RL_admin_product_view_name);
        phone = findViewById(R.id.RL_admin_product_view_phone);
        totalAmount = findViewById(R.id.RL_admin_product_view_total_amount);
        dateTime = findViewById(R.id.RL_admin_product_view_date_time);
        status = findViewById(R.id.RL_admin_product_view_status);
        discount = findViewById(R.id.RL_admin_product_view_discount);
        discountEditText = findViewById(R.id.RL_admin_product_view_discount_editText);
        discountUpdateButton = findViewById(R.id.RL_admin_product_view_discount_update_button);

        userId = getIntent().getStringExtra(Prevalent.userPhoneKey);
        orderId = getIntent().getStringExtra(Prevalent.orderId);
        userType = getIntent().getStringExtra(Prevalent.userTypeKey);

        productsList = findViewById(R.id.admin_products_list);
        productsList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        productsList.setLayoutManager(layoutManager);

        cartListRef = FirebaseDatabase.getInstance().getReference()
                .child(Prevalent.productsPerOrder)
                .child(userId)
                .child(orderId)
                .child(Prevalent.productDBRoot);

        orderRef = FirebaseDatabase.getInstance().getReference()
                .child(Prevalent.ordersDBRoot)
                .child(userId)
                .child(orderId);
        
        discountUpdateButton.setOnClickListener(view -> {
            updateDiscount();
        });
    }

    private void updateDiscount() {
        String discountAmount = discountEditText.getText().toString();
        
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (Integer.valueOf(discountAmount) <= Integer.valueOf(snapshot.child(Prevalent.totalOrderAmount).getValue().toString()))
                    {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put(Prevalent.cartDBDiscount, discountAmount);
                        orderRef.updateChildren(map).addOnCompleteListener(task -> {
                            Toast.makeText(AdminUserProductsActivity.this, "Discount updated successfully...", Toast.LENGTH_SHORT).show();
                        });
                    }
                    else {
                        Toast.makeText(AdminUserProductsActivity.this, "Discount is more than total amount...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
    }

    @Override
    protected void onStart() {
        super.onStart();

        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    name.setText("Name: " + snapshot.child(Prevalent.userProfileName).getValue().toString());
                    phone.setText("Phone: " + snapshot.child(Prevalent.userProfilePhoneNumber).getValue().toString());
                    totalAmount.setText("Total: " + snapshot.child(Prevalent.totalOrderAmount).getValue().toString()
                                        + " Rs/-\n Total Including Discount: " + Calculations.calculateDiscount(
                                                snapshot.child(Prevalent.totalOrderAmount).getValue().toString(),
                                                snapshot.child(Prevalent.cartDBDiscount).getValue().toString()
                    ) + " Rs/-");
                    dateTime.setText("Order Date/Time: " + snapshot.child(Prevalent.date).getValue().toString() + snapshot.child(Prevalent.time).getValue().toString());
                    status.setText("Status: " + snapshot.child(Prevalent.orderState).getValue().toString());
                    discount.setText("Discount: " + snapshot.child(Prevalent.cartDBDiscount).getValue().toString() + " Rs/-");
                    if (userType.equals("Admin")){
                        discountEditText.setText(snapshot.child(Prevalent.cartDBDiscount).getValue().toString());
                        discountEditText.setVisibility(View.VISIBLE);
                        discountUpdateButton.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef, Cart.class)
                .build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int i, @NonNull Cart cart) {

                holder.txtProductName.setText(cart.getName());
                holder.txtProductPrice.setText("Unit price: " + cart.getPrice() + " Rs/- \n" + "Total Price: " +
                        Calculations.calculateTotal(Integer.valueOf(cart.getPrice()), Integer.valueOf(cart.getQuantity()))
                        + " Rs/-");
                holder.txtProductQuantity.setText("Quantity: " + cart.getQuantity());

                holder.itemView.setOnClickListener(view -> {
                    if (userType.equals("Admin")) {
                        startActivity(new Intent(AdminUserProductsActivity.this, CartActivity.class)
                                .putExtra(Prevalent.userTypeKey, "Admin")
                                .putExtra(Prevalent.userPhoneKey, userId)
                                .putExtra(Prevalent.orderId, orderId));
                    }
                });

            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };

        productsList.setAdapter(adapter);
        adapter.startListening();
    }
}