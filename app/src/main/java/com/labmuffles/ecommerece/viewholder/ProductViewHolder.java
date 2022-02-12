package com.labmuffles.ecommerece.viewholder;

import android.media.Image;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.labmuffles.ecommerece.R;
import com.labmuffles.ecommerece.interfaces.ItemClickListener;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtProductName, txtProductDescription, txtProductPrice;
    public ImageView imageView;
    public ItemClickListener itemClickListener;

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.product_item_image);
        txtProductName = (TextView) itemView.findViewById(R.id.product_items_name);
        txtProductDescription = (TextView) itemView.findViewById(R.id.product_items_description);
        txtProductPrice = (TextView) itemView.findViewById(R.id.product_items_price);
    }

    public void setItemClickListener(ItemClickListener listener){
        this.itemClickListener = listener;
    }

    @Override
    public void onClick(View view){
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
