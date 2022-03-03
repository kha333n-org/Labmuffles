package com.labmuffles.ecommerece.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.MD5Hash;
import com.labmuffles.ecommerece.prevelant.Prevalent;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button registerButton;
    private EditText nameInput, numberInput, passwordInput;
    private ProgressDialog loadingBar;
    private String userType = "Users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        if (bundle != null){
            userType = i.getStringExtra(Prevalent.userTypeKey);
        }

        registerButton = (Button) findViewById(R.id.register_button);
        nameInput = (EditText) findViewById(R.id.register_name_input);
        numberInput = (EditText) findViewById(R.id.register_phoneNumber_input);
        passwordInput = (EditText) findViewById(R.id.register_password_input);
        loadingBar = new ProgressDialog(this);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    private void createAccount() {
        String name = nameInput.getText().toString();
        String phone = numberInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this,"Please enter your name", Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            } else {
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Create Account");
                    loadingBar.setMessage("Please wait, Account creation in progress...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    validatePhoneNumber(name, phone, password);
                }
            }
        }
    }

    private void validatePhoneNumber(String name, String phone, String password) {

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!(snapshot.child(userType).child(phone).exists())){

                    //Encrypting password first...
                    String passwordHash = MD5Hash.compute(password);
                    HashMap<String, Object> usersDataMap = new HashMap<>();
                    usersDataMap.put(Prevalent.userProfilePhoneNumber, phone);
                    usersDataMap.put(Prevalent.userProfilePassword, passwordHash);
                    usersDataMap.put(Prevalent.userProfileName, name);
                    usersDataMap.put(Prevalent.orderId, "0");

                    rootRef.child(userType).child(phone).updateChildren(usersDataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "Congratulations! Your account has been created.", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                        startActivity(new Intent(RegisterActivity.this, loginActivity.class));
                                        finish();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Oh no! Please check your internet or contact admin.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else {
                    Toast.makeText(RegisterActivity.this, "This " + phone + " already exists." , Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this, "Try with another number or try to login.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}