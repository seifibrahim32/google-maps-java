package com.example.places_googlemaps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.places_googlemaps.pojo.Response;
import com.example.places_googlemaps.pojo.Result;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission
                    .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                        0, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

        };

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);

            Location lastKnownLocation = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);

            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(),
                    lastKnownLocation.getLongitude());
            mMap = googleMap;


            mMap.clear();

            mMap.addMarker(new MarkerOptions().position(userLocation).title("Marker"));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

            Toast.makeText(MapsActivity.this,
                    userLocation.toString(),
                    Toast.LENGTH_SHORT).show();
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {

                    mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                    Toast.makeText(MapsActivity.this,
                            "Lat : " + latLng.latitude + " , "
                                    + "Long : " + latLng.longitude,
                            Toast.LENGTH_LONG).show();

                }

            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    getLocationDetails(marker);
                    return false;
                }

            });


        } else {

            ActivityCompat.requestPermissions(this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1
            );

        }


    }

    private void getLocationDetails(Marker marker) {
        String apiKey = "AIzaSyBDVZ5g_EAi5cOFATE8MaB_p9uPn4MfcRQ";
        LatLng latLng = marker.getPosition();
        Call<Response> call = RetrofitClient.getInstance()
                .getAPI()
                .getAPIResponse(
                        latLng.latitude
                                + "," + latLng.longitude,
                        apiKey
                );
        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Response res = response.body();

                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MapsActivity.this);
                bottomSheetDialog.setContentView(R.layout.activity_modal);
                // Check for null reference before accessing results
                if (res != null) {
                    LinearLayout bottomSheet = bottomSheetDialog.findViewById(R.id.bottom_sheet_layout);


                    String placeId = res.results.get(0).place_id;
                    if (bottomSheet != null) {
                        TextView nameView = bottomSheet.findViewById(R.id.place);

                        // Check for valid formatted_address before setting text
                        if (res.results.get(0).formatted_address != null) {
                            nameView.setText(res.results.get(0).formatted_address);
                        } else {
                            nameView.setText("N/A");
                        }
                        bottomSheet.addView(getLayoutInflater().inflate(R.layout.activity_modal, null));
                    }
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }


        });
    }

}