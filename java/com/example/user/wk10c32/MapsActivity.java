package com.example.user.wk10c32;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener{

    SharedPreferences sharedPref;

    private GoogleMap mMap;
    private Marker marker;
    EditText place;
    ListPreference lp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        String nightMode = getString(R.string.nightKey);
        boolean isnight = sharedPref.getBoolean(nightMode,false);
        Log.d("Vishal","calling from onCreate");
        changeMapStyle(isnight);
        changeMapType();
        mapStyleToast();
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
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.nightKey))){
            boolean checked = sharedPreferences.getBoolean(key,false);
            Log.d("Vishal","calling from shared preference");
            changeMapStyle(checked);
        }
        changeMapType();
        mapStyleToast();
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sg = new LatLng(1.352083,103.819836);
        marker = mMap.addMarker(new MarkerOptions().position(sg).title("Welcome to Singapore!").snippet("Hope you have a great stay!"));
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sg));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(10));

        String nightMode = getString(R.string.nightKey);

        boolean isnight = sharedPref.getBoolean(nightMode,false);
        Log.d("Vishal","calling from onMapReady");
        changeMapStyle(isnight);
        changeMapType();
        mapStyleToast();
    }

    public void whenClick(View view){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        place=findViewById(R.id.location_inp);
        String search = place.getText().toString();

        try{
            addresses = geocoder.getFromLocationName(search,1);

            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();
            LatLng loc = new LatLng(latitude,longitude);

            marker.setPosition(loc);
            marker.setTitle(addresses.get(0).getFeatureName());
            marker.setSnippet(addresses.get(0).getAddressLine(0));
            marker.showInfoWindow();

            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        }catch(Exception ex){
            System.out.println("Cant find");
            ex.printStackTrace();
        }
    }
}
