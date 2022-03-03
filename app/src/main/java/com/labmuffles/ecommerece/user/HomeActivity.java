package com.labmuffles.ecommerece.user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.labmuffles.ecommerece.MainActivity;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.admin.AdminAddNewProductActivity;
import com.labmuffles.ecommerece.admin.AdminsNewOrderActivity;
import com.labmuffles.ecommerece.model.Products;
import com.labmuffles.ecommerece.prevelant.Prevalent;
import com.labmuffles.ecommerece.viewholder.ProductViewHolder;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    View headerView;
    TextView userNameTextView;
    CircleImageView profileImageView;

    private DatabaseReference productRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private String userType = "";

    private FirebaseRemoteConfig remoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initRemoteConfig();

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        if (bundle != null) {
            userType = getIntent().getStringExtra(Prevalent.userTypeKey);
        }


        Paper.init(this);

        productRef = FirebaseDatabase.getInstance().getReference().child(Prevalent.productDBRoot);
        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = navigationView.getHeaderView(0);
        userNameTextView = headerView.findViewById(R.id.user_profile_name);
        profileImageView = headerView.findViewById(R.id.user_profile_image);

        userNameTextView.setText(Prevalent.currentOnlineUser.getName());

        Picasso.get().load(Prevalent.currentOnlineUser.getImage()).placeholder(R.drawable.profile).into(profileImageView);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Products> options = new FirebaseRecyclerOptions.Builder<Products>()
                .setQuery(productRef, Products.class)
                .build();

        FirebaseRecyclerAdapter<Products, ProductViewHolder> adapter =
                new FirebaseRecyclerAdapter<Products, ProductViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int i, @NonNull Products model) {
                        holder.txtProductName.setText(model.getName());
                        holder.txtProductDescription.setText(model.getDescription());
                        holder.txtProductPrice.setText("Price: " + model.getPrice() + " Rs");

                        Picasso.get().load(model.getImage()).into(holder.imageView);


                            holder.itemView.setOnClickListener(view -> {
                                if (userType.equals("admin")) {
                                    startActivity(new Intent(HomeActivity.this, AdminAddNewProductActivity.class)
                                            .putExtra("pid", model.getProductId()).putExtra("state", "update")
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }else {
                                    startActivity(new Intent(HomeActivity.this, ProductDetailsActivity.class)
                                            .putExtra("pid", model.getProductId()));
                                }
                            });
                    }

                    @NonNull
                    @Override
                    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout, parent, false);
                       ProductViewHolder holder = new ProductViewHolder(view);
                       return holder;
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // When there are more than 1 fragments, so there is a way back
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
            } else {
                if (exit)
                    HomeActivity.this.finish();
                else {
                    Toast.makeText(this, "Press Back again to Exit.",
                            Toast.LENGTH_SHORT).show();
                    exit = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exit = false;
                        }
                    }, 3000);

                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_cart) {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
        }else
        if (id == R.id.nav_orders){

            startActivity(new Intent(HomeActivity.this, AdminsNewOrderActivity.class)
                    .putExtra(Prevalent.userTypeKey, "User"));

        }else if (id == R.id.nav_search) {
            startActivity(new Intent(HomeActivity.this, SearchProductsActivity.class));
        }else if (id == R.id.nav_settings){

            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));

        }else if (id == R.id.nav_logout){
            Paper.book().destroy();

            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cart) {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
        }else
            if (id == R.id.nav_orders){

                startActivity(new Intent(HomeActivity.this, AdminsNewOrderActivity.class)
                .putExtra(Prevalent.userTypeKey, "User"));

            }else if (id == R.id.nav_search) {
                startActivity(new Intent(HomeActivity.this, SearchProductsActivity.class));
            }else if (id == R.id.nav_settings){

                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));

            }else if (id == R.id.nav_logout){
                Paper.book().destroy();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean exit = false;

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