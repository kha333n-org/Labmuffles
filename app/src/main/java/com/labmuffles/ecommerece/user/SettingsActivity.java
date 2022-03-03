package com.labmuffles.ecommerece.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.prevelant.Prevalent;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private EditText fullNameEditText, phoneNumberEditText, addressEditText;
    private TextView imageSelectButton, closeButton, updateButton;
    private final String CLICKED = "clicked";

    private Uri imageUri;
    private String myUrl = "", checker = "";
    private StorageReference storageReference;
    private StorageTask uploadTask;
    private static final int GALLERY_PICK = 1;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileImage = (CircleImageView) findViewById(R.id.profile_image_settings);
        fullNameEditText = (EditText) findViewById(R.id.settings_full_name);
        phoneNumberEditText = (EditText) findViewById(R.id.settings_phone_number);
        addressEditText = (EditText) findViewById(R.id.settings_address);
        imageSelectButton = (TextView) findViewById(R.id.profile_image_change_button);
        closeButton = (TextView) findViewById(R.id.close_settings);
        updateButton = (TextView) findViewById(R.id.update_account_settings);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Update Profile");
        progressDialog.setMessage("Updating profile");
        progressDialog.setCanceledOnTouchOutside(false);
        
        userInfoDisplay(profileImage, fullNameEditText, phoneNumberEditText, addressEditText);

        storageReference = FirebaseStorage.getInstance().getReference(Prevalent.userImagesFolder);

        imageSelectButton.setOnClickListener(view -> {
            checker = CLICKED;

            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_PICK);
        });


        closeButton.setOnClickListener(view -> {
            finish();
        });

        updateButton.setOnClickListener(view -> {
            if (checker.equals(CLICKED)){
                progressDialog.show();
                userInfoSaved();
            }else {
                progressDialog.show();
                updateOnlyUserInfo();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
        else {
            Toast.makeText(SettingsActivity.this, "Error: Please try again", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
            finish();
        }
    }

    private void updateOnlyUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");

        HashMap<String,Object> userMap = new HashMap<>();

        userMap.put(Prevalent.userProfileName, fullNameEditText.getText().toString());
        userMap.put(Prevalent.userProfileAddress, addressEditText.getText().toString());
        userMap.put(Prevalent.userOrderPhone, phoneNumberEditText.getText().toString());

        reference.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

        progressDialog.dismiss();
        startActivity(new Intent(SettingsActivity.this, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        Toast.makeText(SettingsActivity.this, "Account updated successfully...", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void userInfoSaved() {

        if (TextUtils.isEmpty(fullNameEditText.getText().toString())){
            Toast.makeText(SettingsActivity.this, "Name is required...", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }else if (TextUtils.isEmpty(phoneNumberEditText.getText().toString())){
            Toast.makeText(SettingsActivity.this, "Phone number is required...", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }else if (TextUtils.isEmpty(addressEditText.getText().toString())){
            Toast.makeText(SettingsActivity.this, "Address is required...", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }else
        if (checker.equals(CLICKED)){
            uploadImage();
        }

    }

    private void uploadImage() {

        if (imageUri != null){
            final StorageReference fileRef = storageReference.child(Prevalent.currentOnlineUser.getPhone() + ".jpg");

            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) throw task.getException();
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String,Object> userMap = new HashMap<>();

                        userMap.put(Prevalent.userProfileName, fullNameEditText.getText().toString());
                        userMap.put(Prevalent.userProfileAddress, addressEditText.getText().toString());
                        userMap.put(Prevalent.userOrderPhone, phoneNumberEditText.getText().toString());
                        userMap.put(Prevalent.userProfileImage, myUrl);

                        reference.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

                        Prevalent.currentOnlineUser.setImage(myUrl);

                        progressDialog.dismiss();

                        startActivity(new Intent(SettingsActivity.this, HomeActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        Toast.makeText(SettingsActivity.this, "Account updated successfully...", Toast.LENGTH_SHORT).show();
                        finish();
                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Error...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(SettingsActivity.this, "Image is not selected...", Toast.LENGTH_SHORT).show();
        }
    }

    private void userInfoDisplay(CircleImageView image, EditText fullName, EditText phoneNumber, EditText address) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.child(Prevalent.userProfileImage).exists()){
                        String userImage = snapshot.child(Prevalent.userProfileImage).getValue().toString();
                        String userName = snapshot.child(Prevalent.userProfileName).getValue().toString();
                        String userPhone = snapshot.child(Prevalent.userProfilePhoneNumber).getValue().toString();
                        String userAddress = snapshot.child(Prevalent.userProfileAddress).getValue().toString();

                        Picasso.get().load(userImage).into(profileImage);
                        fullName.setText(userName);
                        phoneNumber.setText(userPhone);
                        address.setText(userAddress);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}