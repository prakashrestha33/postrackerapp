package com.android.postracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    final String url = "192.168.0.112:8080/api/tracking/create";
    private double Latitude = 27.6868, Longitute = 85.3352;
    private GoogleMap mMap;
    private String cityName="shankhamul";
    private String stateName="kathmandu";
    private EditText batchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapview);
        Button locationSelect = (Button) findViewById(R.id.activity_main_select_button);

        locationSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBatchId();
            }
        });

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

    private void createBatchId() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_main, null);
        dialogBuilder.setView(dialogView);

        batchEditText =(EditText) findViewById(R.id.dialog_main_batch_editText);

        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOrder();
            }
        });
    }

    private void setOrder() {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");
                    if (!error){
                        Toast.makeText(MainActivity.this, "Tracking has been created", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("latitude", String.valueOf(Latitude));
                params.put("longitude", String.valueOf(Longitute));
                params.put("batch_id",batchEditText.getText().toString().trim());
                params.put("tracking_address",cityName + "," + stateName);
                return params;
            }
        };
        Appcontroller.getInstance().addToRequestQueue(request);
    }
}
