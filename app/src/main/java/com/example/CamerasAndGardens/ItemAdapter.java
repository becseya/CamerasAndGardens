package com.example.CamerasAndGardens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private final List<Item> myListOfItems;

    public ItemAdapter(List<Item> listOfItems) {
        super();
        myListOfItems = listOfItems;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.bindValues(myListOfItems.get(position));
    }

    @Override
    public int getItemCount() {
        return myListOfItems.size();
    }
}
