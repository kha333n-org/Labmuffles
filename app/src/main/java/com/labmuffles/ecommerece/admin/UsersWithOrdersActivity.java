package com.labmuffles.ecommerece.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.UsersWithOrders;
import com.labmuffles.ecommerece.prevelant.Prevalent;

public class UsersWithOrdersActivity extends AppCompatActivity {

    private RecyclerView usersList;
    private DatabaseReference userRef;
    private Switch userTypeSwitch;
    private String startAt = "true", endAt = "true";
    private FirebaseRecyclerAdapter<UsersWithOrders, UsersWithOrdersViewHolder> adapter;
    private FirebaseRecyclerOptions<UsersWithOrders> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_with_orders);

        usersList = findViewById(R.id.users_with_orders_list);
        userRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.usersWithOrders);

        usersList.setLayoutManager(new LinearLayoutManager(this));

        userTypeSwitch = findViewById(R.id.users_type_switch);

        userTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startAt = "false";
                endAt = "false";
                userTypeSwitch.setText("Show Users with pending orders: ");
            }
            else {
                startAt = "true";
                endAt = "true";
                userTypeSwitch.setText("Show Users with no pending orders: ");
            }
            onStart();
        } );

    }

    @Override
    protected void onStart() {
        super.onStart();

        options = new FirebaseRecyclerOptions.Builder<UsersWithOrders>()
                .setQuery(userRef
                        .orderByChild(Prevalent.orderState)
                        .startAt(startAt)
                                .endAt(endAt),
                        UsersWithOrders.class)
                .build();

         adapter =
                new FirebaseRecyclerAdapter<UsersWithOrders, UsersWithOrdersViewHolder>(options) {

                    @NonNull
                    @Override
                    public UsersWithOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = getLayoutInflater().inflate(R.layout.users_with_orders_layout, parent, false);
                        return new UsersWithOrdersViewHolder(view);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull UsersWithOrdersViewHolder usersWithOrdersViewHolder, int i, @NonNull UsersWithOrders usersWithOrders) {
                        usersWithOrdersViewHolder.userName.setText("Name : " + usersWithOrders.getName());
                        usersWithOrdersViewHolder.userPhoneNumber.setText("Phone : " + usersWithOrders.getPhone());
                        usersWithOrdersViewHolder.userShippingAddress.setText("Address: " + usersWithOrders.getAddress());
                        usersWithOrdersViewHolder.userDateTime.setText("Order Date/Time : " + usersWithOrders.getDateTime());

                        usersWithOrdersViewHolder.showOrdersBtn.setOnClickListener(view -> {
                            String uID = getRef(i).getKey();

                            startActivity(new Intent(UsersWithOrdersActivity.this, AdminsNewOrderActivity.class)
                                    .putExtra(Prevalent.userPhoneKey, uID));
                        });
                    }
                };

        usersList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class UsersWithOrdersViewHolder extends RecyclerView.ViewHolder {

        public TextView userName, userPhoneNumber, userDateTime, userShippingAddress, userState;
        public Button showOrdersBtn;

        public UsersWithOrdersViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name_users_with_orders_layout);
            userPhoneNumber = itemView.findViewById(R.id.phone_number_with_orders_layout);
            userDateTime = itemView.findViewById(R.id.date_time_users_with_orders_layout);
            userShippingAddress = itemView.findViewById(R.id.shipping_address_users_with_orders_layout);
            showOrdersBtn = itemView.findViewById(R.id.show_order_details_button);
        }
    }
    
}