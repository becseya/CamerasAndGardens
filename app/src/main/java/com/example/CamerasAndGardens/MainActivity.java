package com.example.CamerasAndGardens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

                    if (elementName.equals("Data") && isNextElementCameraName(parser)) {
                        listOfItems.add(new Item(parser.nextText()));
                    }
                }
                eventType = parser.next(); // Get next parsing event
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error:" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void parseJson() {
        try {
            InputStream is = getAssets().open("gardens.json");
            JSONObject root = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));

            JSONArray gardenArray = root.getJSONArray("@graph");
            for (int i = 0; i < gardenArray.length(); i++) {
                JSONObject gardenNode = gardenArray.getJSONObject(i);
                listOfItems.add(new Item(gardenNode.getString("title")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private String getCameraUrlFromDescription(String description){
        description = description.substring(description.indexOf("http:")); // from the beginning of “http:”...
        return description.substring(0, description.indexOf(".jpg") + 4); // ...to the end of “.jpg”
    }

    static private boolean isNextElementCameraName(XmlPullParser parser) throws IOException, XmlPullParserException {
        if ((parser.getAttributeCount() == 1) && (parser.getAttributeValue(0).equals("Nombre"))) {
            int eventType;

            do {
                eventType = parser.next();
            } while ((eventType != XmlPullParser.START_TAG) && (eventType != XmlPullParser.END_DOCUMENT));
            return parser.getName().equals("Value");
        }

        return false;
    }

    public void onBtnCameras(View view) {
        listOfItems.clear();
        parseXml();
        myAdapter.notifyDataSetChanged();
    }

    public void onBtnGardens(View view) {
        listOfItems.clear();
        parseJson();
        myAdapter.notifyDataSetChanged();
    }
}