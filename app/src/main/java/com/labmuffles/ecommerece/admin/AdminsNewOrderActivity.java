package com.labmuffles.ecommerece.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.AdminOrders;
import com.labmuffles.ecommerece.model.Calculations;
import com.labmuffles.ecommerece.prevelant.Prevalent;

import java.util.HashMap;

public class AdminsNewOrderActivity extends AppCompatActivity {

    private RecyclerView orderList;
    private DatabaseReference orderRef;
    private String userPhoneKey, userType = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admins_new_order);
        orderList = findViewById(R.id.orders_list);

        if (getIntent().getStringExtra(Prevalent.userTypeKey) != null) {
            userType = getIntent().getStringExtra(Prevalent.userTypeKey);
            userPhoneKey = Prevalent.currentOnlineUser.getPhone();
        }else {
            userPhoneKey = getIntent().getStringExtra(Prevalent.userPhoneKey);
        }


        orderRef = FirebaseDatabase.getInstance().getReference()
                .child(Prevalent.ordersDBRoot)
                .child(userPhoneKey);

        orderList.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<AdminOrders> options = new FirebaseRecyclerOptions.Builder<AdminOrders>()
                .setQuery(orderRef.orderByChild(Prevalent.orderState), AdminOrders.class)
                .build();

        FirebaseRecyclerAdapter<AdminOrders, AdminsOrdersViewHolder> adapter =
                new FirebaseRecyclerAdapter<AdminOrders, AdminsOrdersViewHolder>(options) {

                    @NonNull
                    @Override
                    public AdminsOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = getLayoutInflater().inflate(R.layout.orders_layout, parent, false);
                        return new AdminsOrdersViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull AdminsOrdersViewHolder adminsOrdersViewHolder, int i, @NonNull AdminOrders adminOrders) {
                        adminsOrdersViewHolder.userName.setText("Name : " + adminOrders.getName());
                        adminsOrdersViewHolder.userPhoneNumber.setText("Phone : " + adminOrders.getPhone());
                        adminsOrdersViewHolder.userShippingAddress.setText("Address: " + adminOrders.getAddress());
                        adminsOrdersViewHolder.userTotalPrice.setText("Total : " + adminOrders.getTotalAmount() + " Rs/- \n"
                        + "Total Including Discount: " + Calculations.calculateDiscount(adminOrders.getTotalAmount(), adminOrders.getDiscount())
                                + " Rs/-");
                        adminsOrdersViewHolder.userDateTime.setText("Order Date/Time : " + adminOrders.getDate() + " " + adminOrders.getTime());
                        adminsOrdersViewHolder.status.setText("Status: " + adminOrders.getState());
                        adminsOrdersViewHolder.discount.setText("Discount: " + adminOrders.getDiscount() + " Rs/-");

                        adminsOrdersViewHolder.showOrdersBtn.setOnClickListener(view -> {
                            String uID = getRef(i).getKey();

                            if (userType.equals("User")){
                                startActivity(new Intent(AdminsNewOrderActivity.this, AdminUserProductsActivity.class)
                                        .putExtra(Prevalent.userPhoneKey, userPhoneKey)
                                        .putExtra(Prevalent.orderId, uID)
                                        .putExtra(Prevalent.userTypeKey, "User"));
                            }else {
                                startActivity(new Intent(AdminsNewOrderActivity.this, AdminUserProductsActivity.class)
                                        .putExtra(Prevalent.userPhoneKey, userPhoneKey)
                                        .putExtra(Prevalent.orderId, uID)
                                        .putExtra(Prevalent.userTypeKey, "Admin"));
                            }
                        });

                        if (userType.equals("User"))
                        {
                            adminsOrdersViewHolder.itemView.setOnClickListener(view -> {
                                CharSequence options[] = new CharSequence[]{
                                        "Cancel"
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(AdminsNewOrderActivity.this);
                                builder.setTitle("Order Options");
                                builder.setItems(options, (dialog, which) -> {
                                    if (which == 0) {
                                        changeStatusUser(getRef(i).getKey(), "Cancel", adminOrders);
                                    }
                                });
                                builder.create();
                                builder.show();
                            });
                        }
                        else {
                            adminsOrdersViewHolder.itemView.setOnClickListener(view -> {
                                CharSequence options[] = new CharSequence[]{
                                        "Approved",
                                        "Shipped",
                                        "Completed",
                                        "Re-Open",
                                        "Cancel"
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(AdminsNewOrderActivity.this);
                                builder.setTitle("Order Options");
                                builder.setItems(options, (dialog, which) -> {
                                    if (which == 0) {
                                        changeStatus(getRef(i).getKey(), "(1) Approved");
                                    }
                                    if (which == 1) {
                                        changeStatus(getRef(i).getKey(), "(2) Shipped");
                                    }
                                    if (which == 2) {
                                        changeStatus(getRef(i).getKey(), "(3) Completed");
                                    }
                                    if (which == 3) {
                                        changeStatus(getRef(i).getKey(), "(0) Not Shipped");
                                    }
                                    if (which == 4){
                                        changeStatus(getRef(i).getKey(), "(4) Canceled");
                                    }
                                });
                                builder.create();
                                builder.show();
                            });
                        }
                    }
                };

        orderList.setAdapter(adapter);
        adapter.startListening();
    }

    private void changeStatusUser(String key, String s, AdminOrders adminOrders) {
        if (adminOrders.getState().equals("Not Shipped") && s.equals("Cancel")){
            changeStatus(key, s);
        } else {
            Toast.makeText(AdminsNewOrderActivity.this, "Order already under processing cannot change state. Contact Admin for further assistance.", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeStatus(String key, String status) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Prevalent.orderState, status);
        orderRef.child(key).updateChildren(map).addOnCompleteListener(task -> {
            Toast.makeText(AdminsNewOrderActivity.this, "Updated successfully...", Toast.LENGTH_SHORT).show();
        });
    }


    public static class AdminsOrdersViewHolder extends RecyclerView.ViewHolder {

        public TextView userName, userPhoneNumber, userTotalPrice, userDateTime, userShippingAddress, status, discount;
        public Button showOrdersBtn;

        public AdminsOrdersViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name_order);
            userPhoneNumber = itemView.findViewById(R.id.phone_number_order);
            userTotalPrice = itemView.findViewById(R.id.total_price_order);
            userDateTime = itemView.findViewById(R.id.date_time_order);
            userShippingAddress = itemView.findViewById(R.id.shipping_address_order);
            showOrdersBtn = itemView.findViewById(R.id.show_all_products_button);
            status = itemView.findViewById(R.id.status_order);
            discount = itemView.findViewById(R.id.discount_order);
        }
    }
}