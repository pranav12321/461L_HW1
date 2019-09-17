package com.example.googlemapsapi;

import androidx.fragment.app.FragmentActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
 //   OkHttpClient client = new OkHttpClient();
    private GoogleMap mMap;
    private TextView mTextViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onMapSearch(View view) {
        mTextViewResult = findViewById(R.id.textView);
        EditText locationSearch = (EditText) findViewById(R.id.editText);
        String location = locationSearch.getText().toString();                      // Parse the address
        List<Address> addressList = null;

        if ((location != null) && (!location.equals(""))) {
            Geocoder geocoder = new Geocoder(this);

            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (Exception e) {
                e.printStackTrace();                                // gprc exception when bckend service isn't running due to connection failure
            }


            if((addressList.size()==0) || (addressList==null)) {
                mTextViewResult.setText("Invalid");                   //Check for invalid inputs
                return;
            }
            final Address address = addressList.get(0);                                         //get the converted address, and the latitude and longitude
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());



            OkHttpClient client = new OkHttpClient();

            String url = "https://api.darksky.net/forecast/fc0fa19b3670e60c07abd6bd8a0f76a1/" + address.getLatitude()+","+address.getLongitude();
            final Request request = new Request.Builder().url(url).build();              //Create and send HTTP request for the url
            client.newCall(request).enqueue(new Callback() {

                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextViewResult.setText("Failure to connect");                      //Fetch failure
                        }
                    });
                }
                @Override
                public void onResponse(Call call, final Response response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //mTextViewResult.setText(response.body().string());
                                JSONObject obj = new JSONObject(response.body().string());       // Parse the necessary key values from the JSON file returned by darksky query
                                String weather_data = getString(R.string.weather_data,address.getLatitude() ,address.getLongitude(),
                                                                obj.getJSONObject("currently").getDouble("humidity"),
                                                                obj.getJSONObject("currently").getDouble("temperature"),
                                                                obj.getJSONObject("currently").getDouble("precipProbability"),
                                                                obj.getJSONObject("currently").getDouble("windSpeed"));

                                mTextViewResult.setText(weather_data);
                            } catch (IOException ioe) {
                                mTextViewResult.setText("Error during fetch");
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                                            });
            TextView textView = (TextView) findViewById(R.id.textView);

            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
           mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, (float)12.0));      //Zoom in to map serach point.
        }
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
