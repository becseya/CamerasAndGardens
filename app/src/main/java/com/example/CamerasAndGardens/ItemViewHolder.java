package com.example.CamerasAndGardens;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView myDisplay;
    ItemClickListener listener;

    public ItemViewHolder(View itemView, ItemClickListener listener) {
        super(itemView);
        this.myDisplay = itemView.findViewById(R.id.title);
        this.listener = listener;

        myDisplay.setOnClickListener(this);
    }

    void bindValues(Item item) {
        myDisplay.setText(item.getDisplayText());
    }

    @Override
    public void onClick(View view) {
        listener.onItemClick(getAdapterPosition(), view);
    }

    public interface ItemClickListener {
        void onItemClick(int position, View v);
    }
}
