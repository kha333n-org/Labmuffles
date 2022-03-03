package com.labmuffles.ecommerece.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.Calculations;
import com.labmuffles.ecommerece.model.Cart;
import com.labmuffles.ecommerece.prevelant.Prevalent;
import com.labmuffles.ecommerece.viewholder.CartViewHolder;

import java.util.HashMap;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button proceedToCheckout;
    private TextView totalAmount;
    private DatabaseReference cartListRef, ordersListRef;
    private int overAllTotalPrice = 0;
    private String userType = "User", userPhone, orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        proceedToCheckout = (Button) findViewById(R.id.proceed_to_checkout);

        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Intent i = getIntent();
        if (i.hasExtra(Prevalent.userTypeKey)){
            userType = i.getStringExtra(Prevalent.userTypeKey);
            userPhone = i.getStringExtra(Prevalent.userPhoneKey);
            orderId = i.getStringExtra(Prevalent.orderId);
            proceedToCheckout.setVisibility(View.GONE);
        }

        totalAmount = (TextView) findViewById(R.id.total_price);

        proceedToCheckout.setOnClickListener(view -> {
            if (overAllTotalPrice == 0) {
                Toast.makeText(this, "Your Cart is Empty", Toast.LENGTH_SHORT).show();
            }else {
                startActivity(new Intent(CartActivity.this, ConfirmFinalOrderActivity.class)
                        .putExtra("total_price", String.valueOf(overAllTotalPrice)));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Cart> options;
            if (userType.equals("Admin")){
                ordersListRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.ordersDBRoot).child(userPhone).child(orderId);
                cartListRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.productsPerOrder);
                options = new FirebaseRecyclerOptions.Builder<Cart>()
                        .setQuery(cartListRef
                                .child(userPhone)
                                .child(orderId)
                                .child(Prevalent.productDBRoot), Cart.class)
                        .build();
            }
            else {
                ordersListRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.ordersDBRoot);
                cartListRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.cartDBRoot);
                options = new FirebaseRecyclerOptions.Builder<Cart>()
                        .setQuery(cartListRef
                                .child(Prevalent.currentOnlineUser.getPhone())
                                .child(Prevalent.currentOnlineUser.getOrderId())
                                .child(Prevalent.productDBRoot), Cart.class)
                        .build();
            }

            FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {

                @Override
                protected void onBindViewHolder(@NonNull CartViewHolder holder, int i, @NonNull Cart cart) {
                    int oneProductTotalPrice = Calculations.calculateTotal(Integer.valueOf(cart.getPrice()), Integer.valueOf(cart.getQuantity()));
                    overAllTotalPrice = overAllTotalPrice + oneProductTotalPrice;

                    totalAmount.setText("Total Price: " + overAllTotalPrice + " Rs/-");

                    holder.txtProductName.setText(cart.getName());
                    holder.txtProductPrice.setText("Unit price: " + cart.getPrice() + " Rs/- \n" + "Total Price: " + oneProductTotalPrice + " Rs/-");
                    holder.txtProductQuantity.setText("Quantity: " + cart.getQuantity());

                    holder.itemView.setOnClickListener(view -> {
                        CharSequence options[] = new CharSequence[]{
                                "Edit",
                                "Remove"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options");
                        builder.setItems(options, (dialog, which) -> {
                            if (userType.equals("Admin")){
                                if (which == 0) {
                                    Toast.makeText(CartActivity.this, "Admin can only remove...", Toast.LENGTH_SHORT).show();
                                }
                                if (which == 1) {
                                    ordersListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put(Prevalent.totalOrderAmount,
                                                        String.valueOf(
                                                                Integer.valueOf(
                                                                        snapshot.child(Prevalent.totalOrderAmount).getValue().toString()
                                                                )
                                                                -
                                                                        (
                                                                                Integer.valueOf(cart.getPrice())
                                                                                        *
                                                                                        Integer.valueOf(cart.getQuantity())
                                                                        )
                                                        )
                                                );
                                                ordersListRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        cartListRef.child(userPhone)
                                                                .child(orderId)
                                                                .child(Prevalent.productDBRoot).child(cart.getPid())
                                                                .removeValue()
                                                                .addOnCompleteListener(task1 -> {
                                                                    if (task1.isSuccessful()) {
                                                                        Toast.makeText(CartActivity.this, "Item removed successfully", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            }
                            else {
                                if (which == 0) {
                                    startActivity(new Intent(CartActivity.this, ProductDetailsActivity.class)
                                            .putExtra(Prevalent.cartDBPid, cart.getPid()));
                                    finish();
                                }
                                if (which == 1) {
                                    cartListRef.child(Prevalent.currentOnlineUser.getPhone())
                                            .child(Prevalent.currentOnlineUser.getOrderId())
                                            .child(Prevalent.productDBRoot).child(cart.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    ordersListRef.child(Prevalent.currentOnlineUser.getPhone())
                                                            .child(Prevalent.currentOnlineUser.getOrderId())
                                                            .child(Prevalent.productDBRoot)
                                                            .child(cart.getPid())
                                                            .removeValue()
                                                            .addOnCompleteListener(task2 -> {
                                                                if (task.isSuccessful()) {

                                                                    Toast.makeText(CartActivity.this, "Item removed successfully", Toast.LENGTH_SHORT).show();

                                                                }
                                                            });
                                                }
                                            });
                                }
                            }
                        });
                        builder.show();
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

            recyclerView.setAdapter(adapter);
            adapter.startListening();
    }

    private void updateOrderDetails() {

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}