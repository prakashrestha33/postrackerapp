package com.android.postracker;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,View.OnClickListener{
    private double Latitude = 27.6868, Longitute = 85.3352;
    private GoogleMap mMap;
    private String cityName;
    private String stateName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapview);
        Button locationSelect = (Button) findViewById(R.id.activity_main_select_button);

        locationSelect.setOnClickListener(this);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker
        LatLng kathmandu = new LatLng(Latitude, Longitute);
        // Move the camera instantly to kathmandu with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kathmandu, 15));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000,null);

        mMap.addMarker(new MarkerOptions().position(kathmandu).title(cityName+","+stateName));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(kathmandu));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                //zoom
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(20), 2000,null);

                Latitude = latLng.latitude;
                Longitute = latLng.longitude;

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses;
                try {
                    addresses = geocoder.getFromLocation(Latitude, Longitute, 1);
                    cityName = addresses.get(0).getAddressLine(0);
                    stateName = addresses.get(0).getAddressLine(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(cityName + " , " + stateName);

                // Clears the previously touched position
                mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);
            }
        });
    }

    @Override
    public void onClick(View v) {
        createBatchId();
    }

    private void createBatchId() {
        final String url = "192.168.100:8080/api/tracking/create";
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_main, null);
        dialogBuilder.setView(dialogView);

        final EditText batchEditText =(EditText) findViewById(R.id.dialog_main_batch_editText);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    boolean error = jsonObject.getBoolean("error");
                                    if (!error){
                                        Toast.makeText(MainActivity.this, "Location Has been added", Toast.LENGTH_SHORT).show();
                                    }
                                    Toast.makeText(MainActivity.this, "Something Went wrong", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("longitude", String.valueOf(Longitute));
                        params.put("latitude", String.valueOf(Latitude));
                        params.put("address_name",cityName+","+stateName);
                        params.put("batch_id",batchEditText.getText().toString());
                        return params;
                    }
                };
                request.setRetryPolicy(new DefaultRetryPolicy());
                Volley.newRequestQueue(v.getContext()).add(request);
            }
        });
    }
}
