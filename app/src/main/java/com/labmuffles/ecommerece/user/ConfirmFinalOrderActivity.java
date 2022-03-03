package com.labmuffles.ecommerece.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.Calculations;
import com.labmuffles.ecommerece.prevelant.Prevalent;

import java.util.HashMap;
import java.util.Map;

public class ConfirmFinalOrderActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, addressEditText;
    private Button confirmOrderButton;
    private String totalAmount = "";
    private DatabaseReference orderRef, userRef, usersWithOrdersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_final_order);

        confirmOrderButton = (Button) findViewById(R.id.order_confirm_button);
        nameEditText = (EditText) findViewById(R.id.shipment_name);
        phoneEditText = (EditText) findViewById(R.id.shipment_phone);
        addressEditText = (EditText) findViewById(R.id.shipment_address);



        totalAmount = getIntent().getStringExtra("total_price");

        confirmOrderButton.setOnClickListener(v -> {
             check();
             
        });

        loadData();

    }

    private void loadData() {
        DatabaseReference userDetail = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(Prevalent.currentOnlineUser.getPhone());
        userDetail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    nameEditText.setText(snapshot.child(Prevalent.userProfileName).getValue().toString());
                    phoneEditText.setText(snapshot.child(Prevalent.userProfilePhoneNumber).getValue().toString());
                    addressEditText.setText(snapshot.child(Prevalent.userProfileAddress).getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void check() {
        if (TextUtils.isEmpty(nameEditText.getText().toString())) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
        }else
            if (TextUtils.isEmpty(phoneEditText.getText().toString())){
                Toast.makeText(ConfirmFinalOrderActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            }
            else
                if (TextUtils.isEmpty(addressEditText.getText().toString())){
                    Toast.makeText(ConfirmFinalOrderActivity.this, "Please enter your address", Toast.LENGTH_SHORT).show();
                }
                    else {
                        confirmOrder();
                    }
    }

    private void confirmOrder() {
        orderRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.ordersDBRoot)
                .child(Prevalent.currentOnlineUser.getPhone())
                .child(Prevalent.currentOnlineUser.getOrderId());

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());

        usersWithOrdersRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.usersWithOrders)
                .child(Prevalent.currentOnlineUser.getPhone());

        HashMap<String, Object> orderMap = new HashMap<>();
        orderMap.put(Prevalent.totalOrderAmount, totalAmount);
        orderMap.put(Prevalent.userProfileName, nameEditText.getText().toString());
        orderMap.put(Prevalent.userProfilePhoneNumber, phoneEditText.getText().toString());
        orderMap.put(Prevalent.userProfileAddress, addressEditText.getText().toString());
        orderMap.put(Prevalent.orderState, "Not Shipped");
        orderMap.put(Prevalent.cartDBDiscount, "0");
        orderMap.put(Prevalent.date, Calculations.getCurrentDate());
        orderMap.put(Prevalent.time, Calculations.getCurrentTime());

        orderRef.updateChildren(orderMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                FirebaseDatabase.getInstance().getReference()
                        .child(Prevalent.cartDBRoot)
                        .child(Prevalent.currentOnlineUser.getPhone())
                        .child(Prevalent.currentOnlineUser.getOrderId())
                        .removeValue()
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                Prevalent.currentOnlineUser.setOrderId(String.valueOf(Integer.valueOf(Prevalent.currentOnlineUser.getOrderId()) + 1));
                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put(Prevalent.orderId, Prevalent.currentOnlineUser.getOrderId());
                                userRef.updateChildren(userMap).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()){

                                        HashMap<String, Object> usersWithOrdersMap = new HashMap<>();
                                        usersWithOrdersMap.put(Prevalent.orderState, "true");
                                        usersWithOrdersMap.put(Prevalent.userProfileName, Prevalent.currentOnlineUser.getName());
                                        usersWithOrdersMap.put(Prevalent.userProfileAddress, Prevalent.currentOnlineUser.getAddress());
                                        usersWithOrdersMap.put(Prevalent.userProfilePhoneNumber, Prevalent.currentOnlineUser.getPhone());
                                        usersWithOrdersMap.put("dateTime", Calculations.getCurrentDate() + " " + Calculations.getCurrentTime());

                                        usersWithOrdersRef.updateChildren(usersWithOrdersMap).addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful()){
                                                Toast.makeText(ConfirmFinalOrderActivity.this, "Your order has been placed successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(ConfirmFinalOrderActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                                finish();
                                            }
                                                });

                                    }
                                });

                            }
                        });
            }
        });
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}