package com.android.postracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.postracker.jsonparse.JsonParser;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    final String url = "192.168.10.8:8000/api/tracking/create";
    private final JsonParser jsonParser = new JsonParser();
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
        batchEditText = (EditText) dialogView.findViewById(R.id.dialog_main_batch_editText);
        dialogBuilder.setView(dialogView);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String data = batchEditText.getText().toString().trim();
                if (data.length() < 0) {
                    Toast.makeText(MainActivity.this, "Fields are Empty", Toast.LENGTH_SHORT).show();
                } else {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("address_name", cityName + "," + stateName);
                    hashMap.put("latitude", String.valueOf(Latitude));
                    hashMap.put("longitude", String.valueOf(Longitute));
                    hashMap.put("batch_id", data);
                    JSONObject jsonObject = jsonParser.performPostCI("tracking/create", hashMap);
                    try {
                        if (jsonObject == null) {
                            Toast.makeText(MainActivity.this, "Cannot Connect To server", Toast.LENGTH_SHORT).show();
                        } else if (!jsonObject.getBoolean("error")) {
                            Toast.makeText(MainActivity.this, "Tracking Created Successfull", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                        alertDialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
