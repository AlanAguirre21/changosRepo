package com.example.parkgo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Maps extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private EditText searchEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        ImageView profileImageView = findViewById(R.id.profileImageView);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {
            Uri photoUrl = account.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .into(profileImageView);
            }
        }

        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Maps.this, UserProfileActivity.class);
            intent.putExtra("userName", account.getDisplayName());
            intent.putExtra("userEmail", account.getEmail());
            intent.putExtra("userPhoto", account.getPhotoUrl().toString());
            startActivity(intent);
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                hideKeyboard();
                new GeocodeTask().execute(query);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class GeocodeTask extends AsyncTask<String, Void, LatLng> {
        @Override
        protected LatLng doInBackground(String... strings) {
            try {
                String query = URLEncoder.encode(strings[0], "UTF-8");
                URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&limit=1");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "parkgo-app");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonResult = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResult.append(line);
                }

                JSONArray jsonArray = new JSONArray(jsonResult.toString());
                if (jsonArray.length() > 0) {
                    JSONObject obj = jsonArray.getJSONObject(0);
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    return new LatLng(lat, lon);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            if (latLng != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                showNearbyParkings(latLng);
            } else {
                Toast.makeText(Maps.this, "No se encontró ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        loadParkingMarkers();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            showNearbyParkings(currentLocation); // 👈 Mostrar solo cercanos al abrir el mapa
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    private void loadParkingMarkers() {
        db.collection("parkings").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Double lat = document.getDouble("latitude");
                        Double lng = document.getDouble("longitude");
                        String name = document.getString("name");
                        String address = document.getString("address");

                        if (lat != null && lng != null && name != null) {
                            LatLng location = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(name)
                                    .snippet(address));
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar estacionamientos", Toast.LENGTH_SHORT).show());
    }

    private void showNearbyParkings(LatLng center) {
        final double RADIUS_KM = 1.5;

        db.collection("parkings").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mMap.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        String name = doc.getString("name");
                        String address = doc.getString("address");

                        if (lat != null && lng != null) {
                            LatLng parkingLatLng = new LatLng(lat, lng);
                            float[] results = new float[1];
                            Location.distanceBetween(center.latitude, center.longitude,
                                    parkingLatLng.latitude, parkingLatLng.longitude, results);

                            if (results[0] / 1000 <= RADIUS_KM) {
                                mMap.addMarker(new MarkerOptions()
                                        .position(parkingLatLng)
                                        .title(name)
                                        .snippet(address));
                            }
                        }
                    }
                });
    }
}
