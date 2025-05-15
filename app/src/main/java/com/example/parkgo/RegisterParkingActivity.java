package com.example.parkgo;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterParkingActivity extends AppCompatActivity {

    private EditText editTextName, editTextAddress, editTextSpaces, editTextPhone,
            editTextSchedule, editTextCostPerHour, editTextRestrictions;
    private Button buttonSaveParking;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_parking);

        // Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Views
        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextSpaces = findViewById(R.id.editTextSpaces);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextSchedule = findViewById(R.id.editTextSchedule);
        editTextCostPerHour = findViewById(R.id.editTextCostPerHour);
        editTextRestrictions = findViewById(R.id.editTextRestrictions);
        buttonSaveParking = findViewById(R.id.buttonSaveParking);

        buttonSaveParking.setOnClickListener(v -> saveParking());
    }

    private void saveParking() {
        String name = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String spacesStr = editTextSpaces.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String schedule = editTextSchedule.getText().toString().trim();
        String costStr = editTextCostPerHour.getText().toString().trim();
        String restrictions = editTextRestrictions.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(spacesStr) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(schedule) || TextUtils.isEmpty(costStr)) {
            Toast.makeText(this, "Por favor completa todos los campos requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        int spaces;
        double costPerHour;

        try {
            spaces = Integer.parseInt(spacesStr);
            costPerHour = Double.parseDouble(costStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor ingresa valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(this, "No se pudo geocodificar la dirección", Toast.LENGTH_SHORT).show();
                return;
            }

            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();

            String userId = mAuth.getCurrentUser().getUid();

            Map<String, Object> parking = new HashMap<>();
            parking.put("userId", userId);
            parking.put("name", name);
            parking.put("address", address);
            parking.put("spacesAvailable", spaces);
            parking.put("phone", phone);
            parking.put("schedule", schedule);
            parking.put("costPerHour", costPerHour);
            parking.put("restrictions", restrictions);
            parking.put("latitude", latitude);
            parking.put("longitude", longitude);

            db.collection("parkings")
                    .add(parking)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(RegisterParkingActivity.this, "Estacionamiento guardado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(RegisterParkingActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            Toast.makeText(this, "Error al convertir la dirección: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
