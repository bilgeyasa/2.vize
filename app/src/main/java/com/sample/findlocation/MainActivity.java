package com.sample.findlocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ArrayList<FoursquareVenue> venuesList;
    private ArrayList<Double> coordinateList;

    private ProgressDialog progressDialog;
    private ListView listView;

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final String CLIENT_ID = "2GA4IYVZWJTPEH0T51Z2P2HMTBGPTMGUBRRVFY1YXAYGIRWV";
    private static final String CLIENT_SECRET = "CAAEHNG2K0BPE4MM0TDBNZIABIJHRFLZH3MJA5L21NQSVV14";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list_view);


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            new FoursquareAsync().execute();
        /*else
            Toast.makeText(this, "Konum için izin verilmedi.", Toast.LENGTH_SHORT).show(); */

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String urlAddress = "http://maps.google.com/maps?q=" + coordinateList.get(position * 2) + "," + coordinateList.get((position * 2) + 1) + "(" + venuesList.get(position).getName() + ")&iwloc=A&hl=es";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                startActivity(intent);
            }
        });
    }

    private class FoursquareAsync extends AsyncTask<View, Void, String> {

        String url;

        GpsInfo gps = new GpsInfo(MainActivity.this);

        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();

        @Override
        protected String doInBackground(View... urls) {
            url = makeCall("https://api.foursquare.com/v2/venues/search?client_id="
                    + CLIENT_ID + "&client_secret=" + CLIENT_SECRET +
                    "&v=20130815&ll=" + latitude + "," + longitude);
            return "";
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (url == null) {
                Toast.makeText(MainActivity.this, "Bağlantı Hatası", Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            } else {

                coordinateList = new ArrayList<>();
                ArrayList<String> titleList = new ArrayList<>();

                venuesList = (ArrayList<FoursquareVenue>) parseFoursquare(url);

                for (int i = 0; i < venuesList.size(); i++) {
                    titleList.add(venuesList.get(i).getName() + ", " + venuesList.get(i).getCategory() + "\n" + venuesList.get(i).getCity());

                    coordinateList.add(venuesList.get(i).getLat());
                    coordinateList.add(venuesList.get(i).getLng());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.row_layout, R.id.text_view_list, titleList);
                listView.setAdapter(adapter);
            }
            progressDialog.hide();
        }
    }

    public static String makeCall(String urlString) {

        StringBuffer response = new StringBuffer("");

        try{

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    private static ArrayList<FoursquareVenue> parseFoursquare(final String response) {

        ArrayList<FoursquareVenue> resultList = new ArrayList<>();
        try {

            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("response")) {
                if (jsonObject.getJSONObject("response").has("venues")) {
                    JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("venues");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        FoursquareVenue foursqrVenue = new FoursquareVenue();
                        if (jsonArray.getJSONObject(i).has("name")) {
                            foursqrVenue.setName(jsonArray.getJSONObject(i).getString("name"));

                            if (jsonArray.getJSONObject(i).has("location")) {
                                if (jsonArray.getJSONObject(i).getJSONObject("location").has("address")) {
                                    if (jsonArray.getJSONObject(i).getJSONObject("location").has("city")) {
                                        foursqrVenue.setCity(jsonArray.getJSONObject(i).getJSONObject("location").getString("city"));
                                    }
                                    if (jsonArray.getJSONObject(i).getJSONObject("location").has("lat")) {
                                        foursqrVenue.setLat(jsonArray.getJSONObject(i).getJSONObject("location").getDouble("lat"));
                                    }
                                    if (jsonArray.getJSONObject(i).getJSONObject("location").has("lng")) {
                                        foursqrVenue.setLng(jsonArray.getJSONObject(i).getJSONObject("location").getDouble("lng"));
                                    }
                                    if (jsonArray.getJSONObject(i).has("categories")) {
                                        if (jsonArray.getJSONObject(i).getJSONArray("categories").length() > 0) {
                                            if (jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).has("icon")) {
                                                foursqrVenue.setCategory(jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).getString("name"));
                                            }
                                        }
                                    }
                                    resultList.add(foursqrVenue);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
