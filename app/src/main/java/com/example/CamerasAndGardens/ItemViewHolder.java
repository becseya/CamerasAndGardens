package com.example.CamerasAndGardens;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ItemViewHolder extends RecyclerView.ViewHolder {

    TextView myDisplay;

    public ItemViewHolder(View itemView) {
        super(itemView);
        this.myDisplay = itemView.findViewById(R.id.title);
    }

    void bindValues(Item item) {
        myDisplay.setText(item.getDisplayText());
    }
}
