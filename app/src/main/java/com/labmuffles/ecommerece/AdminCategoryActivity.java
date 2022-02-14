package com.labmuffles.ecommerece;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.labmuffles.ecommerece.prevelant.Prevalent;

public class AdminCategoryActivity extends AppCompatActivity {

    private ImageView category1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category);

        category1 = (ImageView) findViewById(R.id.category_1);

        category1.setOnClickListener(view -> startActivity(
                new Intent(AdminCategoryActivity.this, AdminAddNewProductActivity.class)
                .putExtra(Prevalent.categoryKey, "category1")
        ));
    }
}