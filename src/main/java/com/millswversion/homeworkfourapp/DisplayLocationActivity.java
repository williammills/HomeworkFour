package com.millswversion.homeworkfourapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class DisplayLocationActivity extends FragmentActivity {

    private Geocoder geocoder;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLng myLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_location);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMyLocationEnabled(true);
        Location loc = mMap.getMyLocation();
        if (loc != null) {
            myLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
        }
    }

    public void onClick (View view) throws IOException {
        mMap.clear();
        EditText text = (EditText) findViewById(R.id.editText);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
        String location = text.getText().toString();
        JSONObject resp;

        try{
            System.out.println(location);
            String myRequest = formatMyRequest(location);
            //geocoder = new Geocoder(this);
            String resultString = "";

            try
            {
                URL url = new URL(myRequest);
                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;

                while ((str = in.readLine()) != null)
                {
                    // str is one line of text; readLine() strips the newline character(s)
                    // You can use the contain method here.
                    resultString+= str;
                    System.out.println(resultString);
                }
                in.close();
                resp = new JSONObject(resultString);
                JSONArray respArray = resp.getJSONArray("results");
                resp = respArray.getJSONObject(0);
                JSONArray addrComp = resp.getJSONArray("types");
                resp = resp.getJSONObject("geometry");
                resp = resp.getJSONObject("location");

                Double lat = resp.getDouble("lat");
                Double lng = resp.getDouble("lng");
                System.out.println(lat.toString());

                String locType = addrComp.getString(0);
                myLocation = new LatLng(lat,lng);
                int zoomAmount;
                String scaleDisplay = locType;
                if (locType.equals("administrative_area_level_1")){
                    zoomAmount = 6;
                    scaleDisplay = "state";
                }
                else if (locType.equals("country")){
                    zoomAmount = 4;
                }
                else if (locType.equals("natural_feature")){
                    zoomAmount = 4;
                    scaleDisplay = "natural feature";
                }
                else if (locType.equals("locality")){
                    zoomAmount = 13;
                    scaleDisplay = "city";
                }
                else if (locType.equals("neighborhood")){
                    zoomAmount = 18;
                }
                else if (locType.equals("continent")){
                    zoomAmount = 1;
                }
                else{
                    zoomAmount = 13;
                }
                Toast.makeText(getApplicationContext(), "Your search is on the " + scaleDisplay + " scale",
                        Toast.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoomAmount));
                mMap.addMarker(new MarkerOptions().title("here's where you want to see").position(myLocation));
                CameraUpdate center = CameraUpdateFactory.newLatLng(myLocation);
                mMap.moveCamera(center);
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            }catch (JSONException e){
                System.out.println("JSON EXCEPTION");
                e.printStackTrace();

            }
        }catch(Exception e){
            System.out.print("Unknown Exception");
            e.printStackTrace();
        }
    }

    public void zoomOut(View view){
        CameraUpdate newZoom = CameraUpdateFactory.zoomIn();
        mMap.moveCamera(newZoom);
    }

    public void zoomIn(View view){
        CameraUpdate newZoom = CameraUpdateFactory.zoomOut();
        mMap.moveCamera(newZoom);
    }

    public String formatMyRequest(String input) {
        String out = "https://maps.googleapis.com/maps/api/geocode/json?address=";
        String[] items = input.split(" ");
        for (int i = 0; i < items.length; i++) {
            if (i < items.length - 1) {
                out += items[i] + "+";
            } else {
                out += items[i];
            }
        }
        out += "&key=AIzaSyAF4yz2R5CNf7vvmF75CBJSpqo7YlKzA1s";
        return out;
    }
}
