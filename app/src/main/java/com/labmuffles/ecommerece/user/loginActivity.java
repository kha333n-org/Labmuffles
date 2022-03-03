package com.labmuffles.ecommerece.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import com.labmuffles.ecommerece.MainActivity;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.admin.AdminDashboardActivity;
import com.labmuffles.ecommerece.model.MD5Hash;
import com.labmuffles.ecommerece.model.Users;
import com.labmuffles.ecommerece.prevelant.Prevalent;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class loginActivity extends AppCompatActivity {

    private EditText numberInput, passwordInput;
    private Button loginButton;
    private ProgressDialog loadingBar;
    private String parentDbName = "Users";
    private CheckBox rememberCheckBox;
    private TextView adminLink, userLink, passwordResetLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.login_button);
        numberInput = (EditText) findViewById(R.id.login_phoneNumber_input);
        passwordInput = (EditText) findViewById(R.id.login_password_input);
        adminLink = (TextView) findViewById(R.id.admin_panel_link);
        userLink = (TextView) findViewById(R.id.not_admin_panel_link);
        passwordResetLink = findViewById(R.id.login_forgot_password_link);
        loadingBar = new ProgressDialog(this);
        rememberCheckBox = (CheckBox) findViewById(R.id.remember_me_checkbox);
        Paper.init(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        passwordResetLink.setOnClickListener(view -> {
            passwordReset();
        });

        adminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.setText("Login as Admin");
                adminLink.setVisibility(View.INVISIBLE);
                userLink.setVisibility(View.VISIBLE);
                parentDbName = "Admins";
            }
        });

        userLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.setText("Login");
                adminLink.setVisibility(View.VISIBLE);
                userLink.setVisibility(View.INVISIBLE);
                parentDbName = "Users";

            }
        });
    }

    private void passwordReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgotten Password!");

        final View customLayout = getLayoutInflater().inflate(R.layout.password_reset_alert_layout, null);
        builder.setView(customLayout);

        builder.setPositiveButton("Contact Admin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(loginActivity.this, AboutActivity.class));
            }
        });

        builder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loginUser() {
        String phone = numberInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            } else {
                loadingBar.setTitle("Login Account");
                loadingBar.setMessage("Please wait, While we check credentials...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                allowAccessToAccount(phone, password);
            }
        }
    }

    private void allowAccessToAccount(String phone, String password) {

        if (rememberCheckBox.isChecked()){
            Paper.book().destroy();
            Paper.book().write(Prevalent.userPhoneKey, phone);
            Paper.book().write(Prevalent.userPasswordKey, MD5Hash.compute(password));
            Paper.book().write(Prevalent.userTypeKey, parentDbName);
        }

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(parentDbName).child(phone).exists()){
                    Users userData = snapshot.child(parentDbName).child(phone).getValue(Users.class);

                    if (userData.getPhone().equals(phone)){
                        if (userData.getPassword().equals(MD5Hash.compute(password))){

                            if (parentDbName.equals("Admins")){
                                Toast.makeText(loginActivity.this, "Logged in successfully!...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Prevalent.currentOnlineUser = userData;

                                startActivity(new Intent(loginActivity.this, AdminDashboardActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();

                            }else {
                                Toast.makeText(loginActivity.this, "Logged in successfully!...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Prevalent.currentOnlineUser = userData;

                                startActivity(new Intent(loginActivity.this, HomeActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();
                            }

                        }
                        else {
                            Toast.makeText(loginActivity.this, "Password is incorrect...", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                }
                else {
                    Toast.makeText(loginActivity.this, "Account with number (" + phone + ") dose not exists. Please create account", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}