package com.labmuffles.ecommerece.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.model.Products;
import com.labmuffles.ecommerece.prevelant.Prevalent;
import com.labmuffles.ecommerece.viewholder.ProductViewHolder;
import com.squareup.picasso.Picasso;

public class SearchProductsActivity extends AppCompatActivity {

    private Button searchButton;
    private EditText searchQuery;
    private RecyclerView searchList;
    private String searchTerm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_products);

        searchQuery = (EditText) findViewById(R.id.search_product_name);
        searchButton = ( Button) findViewById(R.id.search_product_button);
        searchList = ( RecyclerView) findViewById(R.id.search_list);
        searchList.setLayoutManager(new LinearLayoutManager(SearchProductsActivity.this));


        searchButton.setOnClickListener(view -> {
            searchTerm = searchQuery.getText().toString();

            onStart();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Prevalent.productDBRoot);

        FirebaseRecyclerOptions<Products> options = new FirebaseRecyclerOptions.Builder<Products>()
                .setQuery(reference.orderByChild(Prevalent.productDBName).startAt(searchTerm), Products.class)
                .build();

        FirebaseRecyclerAdapter<Products, ProductViewHolder> adapter = new FirebaseRecyclerAdapter<Products, ProductViewHolder>(options) {

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_items_layout, parent, false);
                ProductViewHolder holder = new ProductViewHolder(view);
                return holder;
            }

            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder holder, int i, @NonNull Products model) {
                holder.txtProductName.setText(model.getName());
                holder.txtProductDescription.setText(model.getDescription());
                holder.txtProductPrice.setText("Price: " + model.getPrice() + " Rs");

                Picasso.get().load(model.getImage()).into(holder.imageView);

                holder.itemView.setOnClickListener(view -> {
                    startActivity(new Intent(SearchProductsActivity.this, ProductDetailsActivity.class)
                            .putExtra("pid", model.getProductId()));
                });
            }
        };

        searchList.setAdapter(adapter);
        adapter.startListening();
    }
}