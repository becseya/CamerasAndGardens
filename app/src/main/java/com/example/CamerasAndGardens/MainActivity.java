package com.example.CamerasAndGardens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
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

    private void parseXml() {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            InputStream is = getAssets().open("CCTV.kml");

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            int eventType = parser.getEventType(); // current event state of the parser
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String elementName = parser.getName(); // name of the current element

                    if (elementName.equals("description")) {
                        listOfItems.add(new Item(getCameraUrlFromDescription(parser.nextText())));
                    }
                }
                eventType = parser.next(); // Get next parsing event
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error:" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    static private String getCameraUrlFromDescription(String description){
        description = description.substring(description.indexOf("http:")); // from the beginning of “http:”...
        return description.substring(0, description.indexOf(".jpg") + 4); // ...to the end of “.jpg”
    }

    public void onBtnCameras(View view) {
        listOfItems.clear();
        parseXml();
        myAdapter.notifyDataSetChanged();
    }
}