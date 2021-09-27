package com.example.CamerasAndGardens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final List<Item> listOfItems = new ArrayList<>();

    private RecyclerView myRecycleView;
    private ItemAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myRecycleView = findViewById(R.id.recyclerView);
        myAdapter = new ItemAdapter(listOfItems);
        myRecycleView.setAdapter(myAdapter);
        myRecycleView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void onBtnCameras(View view) {
        listOfItems.clear();
        listOfItems.add(new Item("A"));
        listOfItems.add(new Item("B"));
        listOfItems.add(new Item("C"));
        myAdapter.notifyDataSetChanged();
    }
}