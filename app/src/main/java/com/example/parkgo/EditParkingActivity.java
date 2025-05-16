package com.example.parkgo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditParkingActivity extends AppCompatActivity {

    private EditText editTextName, editTextAddress, editTextPhone, editTextSchedule, editTextSpaces, editTextCost, editTextRestrictions;
    private Button buttonSave;

    private FirebaseFirestore db;
    private String parkingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_parking);

        db = FirebaseFirestore.getInstance();

        editTextName = findViewById(R.id.editTextName);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextSchedule = findViewById(R.id.editTextSchedule);
        editTextSpaces = findViewById(R.id.editTextSpaces);
        editTextCost = findViewById(R.id.editTextCost);
        editTextRestrictions = findViewById(R.id.editTextRestrictions);
        buttonSave = findViewById(R.id.buttonSave);

        parkingId = getIntent().getStringExtra("parkingId");

        if (parkingId == null) {
            Toast.makeText(this, "ID de estacionamiento no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("parkings").document(parkingId).get().addOnSuccessListener(doc -> {
            editTextName.setText(doc.getString("name"));
            editTextAddress.setText(doc.getString("address"));
            editTextPhone.setText(doc.getString("phone"));
            editTextSchedule.setText(doc.getString("schedule"));
            editTextSpaces.setText(String.valueOf(doc.getLong("spacesAvailable")));
            editTextCost.setText(String.valueOf(doc.getDouble("costPerHour")));
            editTextRestrictions.setText(doc.getString("restrictions"));
        });

        buttonSave.setOnClickListener(v -> {
            db.collection("parkings").document(parkingId).update(
                    "name", editTextName.getText().toString(),
                    "address", editTextAddress.getText().toString(),
                    "phone", editTextPhone.getText().toString(),
                    "schedule", editTextSchedule.getText().toString(),
                    "spacesAvailable", Integer.parseInt(editTextSpaces.getText().toString()),
                    "costPerHour", Double.parseDouble(editTextCost.getText().toString()),
                    "restrictions", editTextRestrictions.getText().toString()
            ).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Estacionamiento actualizado", Toast.LENGTH_SHORT).show();
                finish(); // volver a la pantalla anterior
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
