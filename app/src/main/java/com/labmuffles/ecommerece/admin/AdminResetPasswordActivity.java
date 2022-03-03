package com.labmuffles.ecommerece.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.Calculations;
import com.labmuffles.ecommerece.model.MD5Hash;
import com.labmuffles.ecommerece.prevelant.Prevalent;

import java.util.HashMap;

public class AdminResetPasswordActivity extends AppCompatActivity {

    private Button passwordResetButton;
    private EditText phoneInput, passwordInput;
    private TextView adminLink, userLink;
    private String parentDbName = "Users";
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reset_password);

        passwordInput = findViewById(R.id.password_reset_password_input);
        passwordResetButton = findViewById(R.id.password_reset_button);
        phoneInput = findViewById(R.id.password_reset_phoneNumber_input);
        adminLink = findViewById(R.id.password_reset_admin_link);
        userLink = findViewById(R.id.password_reset_user_link);
        loadingBar = new ProgressDialog(this);

        adminLink.setOnClickListener(view -> {
            adminLink.setVisibility(View.INVISIBLE);
            userLink.setVisibility(View.VISIBLE);
            parentDbName = "Admins";
        });

        userLink.setOnClickListener(view -> {
            adminLink.setVisibility(View.VISIBLE);
            userLink.setVisibility(View.INVISIBLE);
            parentDbName = "Users";
        });

        passwordResetButton.setOnClickListener(view -> {
            passwordReset();
        });
    }

    private void passwordReset() {
        String phone = phoneInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter new password", Toast.LENGTH_SHORT).show();
            } else {
                loadingBar.setTitle("Resetting Password");
                loadingBar.setMessage("Please wait, While we check credentials...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final DatabaseReference rootRef;
                rootRef = FirebaseDatabase.getInstance().getReference();

                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(parentDbName).child(phone).exists()){
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(Prevalent.userProfilePassword, MD5Hash.compute(password));
                            rootRef.child(parentDbName).child(phone).updateChildren(map);
                            Toast.makeText(AdminResetPasswordActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }else {
                            Toast.makeText(AdminResetPasswordActivity.this, "User Dose not exists...", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }
}