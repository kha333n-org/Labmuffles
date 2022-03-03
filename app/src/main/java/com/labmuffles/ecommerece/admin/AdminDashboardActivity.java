package com.labmuffles.ecommerece.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.labmuffles.ecommerece.user.HomeActivity;
import com.labmuffles.ecommerece.MainActivity;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.prevelant.Prevalent;
import com.labmuffles.ecommerece.user.RegisterActivity;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button adminAddNewProductButton, adminEditProductsButton, adminViewOrdersButton, adminLogoutButton
            , addAdminButton, resetPasswordButton;

    private FirebaseRemoteConfig remoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initRemoteConfig();

        adminAddNewProductButton = findViewById(R.id.admin_add_product_button);
        adminEditProductsButton = findViewById(R.id.admin_edit_product_button);
        adminViewOrdersButton = findViewById(R.id.admin_orders_list_button);
        adminLogoutButton = findViewById(R.id.admin_logout_button);
        addAdminButton = findViewById(R.id.admin_add_admin_button);
        resetPasswordButton = findViewById(R.id.admin_reset_password_button);

        resetPasswordButton.setOnClickListener(view -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminResetPasswordActivity.class));
        });

        addAdminButton.setOnClickListener(view -> {
            startActivity(new Intent(AdminDashboardActivity.this, RegisterActivity.class)
            .putExtra(Prevalent.userTypeKey, "Admins"));
        });

        adminAddNewProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAddNewProductActivity.class);
            startActivity(intent);
        });

        adminEditProductsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, HomeActivity.class)
                    .putExtra(Prevalent.userTypeKey,"admin");
            startActivity(intent);
        });

        adminViewOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, UsersWithOrdersActivity.class);
            startActivity(intent);
        });

        adminLogoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Prevalent.currentOnlineUser = null;
            Paper.book().destroy();
            startActivity(intent);
            finish();
        });
    }

    private void initRemoteConfig(){
        remoteConfig = FirebaseRemoteConfig.getInstance();

        HashMap<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(Prevalent.versionCodeKey, getCurrentVersionCode());
        remoteConfig.setDefaultsAsync(defaultConfigMap);

        remoteConfig.setConfigSettingsAsync(
                new FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(Prevalent.updateCheckDelay)
                        .build()
        );

        remoteConfig.fetch().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                remoteConfig.activate().addOnCompleteListener(task1 -> {
                    final int latestAppVersion = (int) remoteConfig.getDouble(Prevalent.versionCodeKey);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkForUpdate(latestAppVersion);
                        }
                    });
                });
            }
        });
    }

    private void checkForUpdate(int latestAppVersion) {
        if (latestAppVersion > getCurrentVersionCode()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Update Available");
            builder.setMessage("Please Update App");
            builder.setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String URL = "https://labmuffles.com/labmuffles.apk";
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(URL)));
                }
            });
            builder.setCancelable(false);
            builder.create();
            builder.show();
        }
    }

    private int getCurrentVersionCode(){
        int versionCode = 1;
        try {
            final PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = (int) pInfo.getLongVersionCode();
            } else {
                versionCode = pInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            //log exception
        }
        return versionCode;
    }
}
