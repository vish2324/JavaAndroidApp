package com.example.user.wk10c32;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Thread.sleep;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener, GoogleMap.OnInfoWindowClickListener {

    SharedPreferences sharedPref;

    private GoogleMap mMap;
    private Marker marker[] = new Marker[7];
    private Marker newLoc;

    PlaceAutocompleteFragment placeAutoComplete;
    LatLng find;
    LatLngBounds bound;

    String address, nameLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        String nightMode = getString(R.string.nightKey);
        boolean isnight = sharedPref.getBoolean(nightMode, false);
        Log.d("Vishal", "calling from onCreate");
        changeMapStyle(isnight);
        changeMapType();
        mapStyleToast();

        boolean ischange = sharedPref.getBoolean("nightMarkerKey", false);
        changeMarkerColour(ischange);

        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d("Maps", "Place Selected: " + place.getName());
                address = place.getAddress().toString();
                nameLoc = place.getName().toString();
                find = place.getLatLng();
                bound = place.getViewport();
                findInput();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });
        placeAutoComplete.setHint("Enter Location");
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setTypeFilter(Place.TYPE_COUNTRY).setCountry("SG").build();
        placeAutoComplete.setFilter(typeFilter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ArrayList<LatLng> places = new ArrayList<>();
        ArrayList<String> placeName = new ArrayList<>();

        places.add(new LatLng(1.290602, 103.846474)); //Clarke Quay
        placeName.add("Clarke Quay");

        places.add(new LatLng(1.284544, 103.859590)); //MBS
        placeName.add("Marina Bay Sands");

        places.add(new LatLng(1.289299, 103.863137)); //Flyer
        placeName.add("Singapore Flyer");

        places.add(new LatLng(1.264282, 103.822158)); //Vivo City
        placeName.add("Vivo City");

        places.add(new LatLng(1.301800, 103.837797)); //Orchard
        placeName.add("Orchard Road");

        places.add(new LatLng(1.255179, 103.821811)); //RWS
        placeName.add("Resorts World Sentosa");

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        for (int i = 0; i < places.size(); i++) {
            try {
                System.out.println("Vishal1");
                System.out.println(placeName.get(i));
                System.out.println(places.get(i));
                addresses = geocoder.getFromLocation(places.get(i).latitude, places.get(i).longitude,1);
                String add = addresses.get(0).getAddressLine(0);
                marker[i] = mMap.addMarker(new MarkerOptions().position(places.get(i)).title(placeName.get(i)).snippet(add));
            } catch (IOException e) {
                System.out.println("Vishal1 Not found");
                e.printStackTrace();
            }
        }

        //update this to show central nameLoc
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(1.281399, 103.844268)));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(13));

        mMap.setOnInfoWindowClickListener(this);

        String nightMode = getString(R.string.nightKey);

        boolean isnight = sharedPref.getBoolean(nightMode, false);
        Log.d("Vishal", "calling from onMapReady");
        changeMapStyle(isnight);
        changeMapType();
        mapStyleToast();

        boolean ischange = sharedPref.getBoolean("nightMarkerKey", false);
        changeMarkerColour(ischange);
    }


    public String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("info.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("MARKERR");
            return null;
        }
        System.out.println("HEREEE"+json);
        return json;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        try {
            String inp = loadJSONFromAsset(this);
            JSONObject jsonArr = new JSONObject(inp);
            String toDis = jsonArr.getString( marker.getId() );
            Toast toast = Toast.makeText(this,toDis,Toast.LENGTH_LONG);
            toast.show();
            toast.show();
            toast.show();
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("MARKERRRR");
        }
    }

    public void findInput(){
        try{
            LatLng loc = find;
            if(newLoc!=null){
                newLoc.setVisible(false);
            }
            newLoc = mMap.addMarker(new MarkerOptions().position(loc));
            newLoc.setVisible(true);
            newLoc.setTitle(nameLoc);
            newLoc.setSnippet(address);
            newLoc.showInfoWindow();
            newLoc.getId();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bound,50));
        }catch(Exception ex){
            System.out.println("Cant find");
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.map_settings){
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.map_about){
            Intent intent = new Intent(this,ActivityAbout.class);
            startActivity(intent);
            return true;
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.nightKey))){
            boolean checked = sharedPreferences.getBoolean(key,false);
            Log.d("Vishal","calling from shared preference");
            changeMapStyle(checked);
            mapStyleToast();
        }
        if(key.equals("mapTypeKey")) {
            changeMapType();
            mapStyleToast();
        }
        if (key.equals("nightMarkerKey")){
            boolean ischange = sharedPref.getBoolean("nightMarkerKey",false);
            changeMarkerColour(ischange);
        }

    }

    boolean isMapNormal;
    boolean isMapNight;

    public void mapStyleToast(){
        if (isMapNight){
            if (!isMapNormal) {
                Toast tst = Toast.makeText(this,"Choose Normal map to see Night Mode",Toast.LENGTH_LONG);
                tst.show();
            }
        }
    }

    public void changeMapType(){
        String type = sharedPref.getString("mapTypeKey","");
        if(mMap != null) {
            switch (type) {
                case "Normal":
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    isMapNormal=true;
                    break;
                case "Hybrid":
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    isMapNormal=false;
                    break;
                case "Satellite":
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    isMapNormal=false;
                    break;
                default:
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
    }

    public void changeMapStyle(boolean change){
        if (change){
            Log.d("Vishal","change is true");
            if (mMap!=null) {
                Log.d("Vishal","mMap is not null");
                try {
                    boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
                    isMapNight=success;
                    if (!success) {
                        Log.e("Vishal", "Style parsing failed");
                    }
                    Log.d("Vishal","changed");
                } catch (Resources.NotFoundException e) {
                    Log.e("Vishal", "Can't find style", e);
                }
            }
            else
                Log.d("Vishal","mMap is null");
        }
        else {
            Log.d("Vishal","change is false");
            if (mMap!=null) {
                Log.d("Vishal","mMap is not null");
                try {
                    boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.normal_json));
                    isMapNight=!success;
                    if(!success){
                        Log.e("Vishal", "Default style parsing failed");
                        isMapNight=false;
                    }
                    Log.d("Vishal","changed to default");
                } catch (Resources.NotFoundException e) {
                    isMapNight=false;
                    Log.e("Vishal", "Can't find default style", e);
                }
            }
            else {
                Log.d("Vishal", "mMap is null");
                isMapNight = false;
            }
        }
    }

    public void changeMarkerColour(boolean change){
        for (Marker m : marker) {
            if (m != null) {
                if (change) {
                    float colour = 120;
                    m.setIcon(BitmapDescriptorFactory.defaultMarker(colour));
                } else {
                    m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
            }
        }
        if (newLoc!= null){
            if (change){
                float colour = 120;
                newLoc.setIcon(BitmapDescriptorFactory.defaultMarker(colour));
            } else {
                newLoc.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
        }
    }
}
