package com.example.CamerasAndGardens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ItemViewHolder.ItemClickListener {

    private static final List<Item> listOfItems = new ArrayList<>();
    private static final String UNKNOWN_NAME = "Unknown name";

    private RecyclerView myRecycleView;
    private ItemAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myRecycleView = findViewById(R.id.recyclerView);
        myAdapter = new ItemAdapter(listOfItems, this);
        myRecycleView.setAdapter(myAdapter);
        myRecycleView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void parseXml() {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            InputStream is = getAssets().open("CCTV.kml");

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            String lastKnownName = UNKNOWN_NAME;

            int eventType = parser.getEventType(); // current event state of the parser
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String elementName = parser.getName(); // name of the current element

                    if (elementName.equals("Data") && isNextElementCameraName(parser)) {
                        lastKnownName = parser.nextText();
                    }
                    else if (elementName.equals("coordinates")) {
                        listOfItems.add(new Item(lastKnownName, getLocationFromCordinates(parser.nextText())));
                    }
                }
                else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("Placemark")) {
                        lastKnownName = UNKNOWN_NAME;
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

                LatLng location = null;

                try{
                    JSONObject locationNode = gardenNode.getJSONObject("location");
                    location = new LatLng(
                            locationNode.getDouble("latitude"),
                            locationNode.getDouble("longitude"));
                }
                catch (JSONException ignored) {}

                listOfItems.add(new Item(gardenNode.getString("title"), location));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private String getCameraUrlFromDescription(String description) {
        description = description.substring(description.indexOf("http:")); // from the beginning of “http:”...
        return description.substring(0, description.indexOf(".jpg") + 4); // ...to the end of “.jpg”
    }

    static private LatLng getLocationFromCordinates(String coordinates) {
        String[] split = coordinates.split(",");
        LatLng location = null;

        if (split.length != 3) {
            return null;
        }

        return new LatLng(Double.parseDouble(split[1]), Double.parseDouble(split[0]));
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

    public void onBtnShowMap(View view) {
        showLatLonOnMap(new LatLng(-34, 151), "Marker in Sydney", false);
    }

    public void showLatLonOnMap(LatLng coordinates, String title, boolean zoom) {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        MapsActivity.appendExtraForMarker(mapIntent, coordinates, title, zoom);
        startActivity(mapIntent);
    }

    @Override
    public void onItemClick(int position, View v) {
        Item item = listOfItems.get(position);

        if (item.isLocationValid()) {
            showLatLonOnMap(item.getLocation(), item.getDisplayText(), true);
        }
        else {
            Toast.makeText(this, "Location is unknown", Toast.LENGTH_SHORT).show();
        }
    }
}