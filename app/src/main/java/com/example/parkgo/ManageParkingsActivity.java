package com.example.parkgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ManageParkingsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewParkings;
    private Button buttonAddParking;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ParkingAdapter adapter;
    private List<Parking> parkingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_parkings);

        recyclerViewParkings = findViewById(R.id.recyclerViewParkings);
        buttonAddParking = findViewById(R.id.buttonAddParking);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        parkingList = new ArrayList<>();
        adapter = new ParkingAdapter(parkingList);

        recyclerViewParkings.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewParkings.setAdapter(adapter);

        loadParkings();

        buttonAddParking.setOnClickListener(v -> {
            Intent intent = new Intent(ManageParkingsActivity.this, RegisterParkingActivity.class);
            startActivity(intent);
        });

        Button buttonEditParking = findViewById(R.id.buttonEditParking);

        buttonEditParking.setOnClickListener(v -> {
            if (parkingList.isEmpty()) {
                Toast.makeText(this, "No hay estacionamientos para editar", Toast.LENGTH_SHORT).show();
                return;
            }

            // Aquí puedes abrir un diálogo o lista para elegir cuál editar. Por simplicidad, seleccionamos el primero:
            String parkingId = parkingList.get(0).getId(); // o implementar un diálogo para elegir

            Intent intent = new Intent(ManageParkingsActivity.this, EditParkingActivity.class);
            intent.putExtra("parkingId", parkingId);
            startActivity(intent);
        });


    }

    private void loadParkings() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("parkings")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(ManageParkingsActivity.this, "Error al cargar estacionamientos", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        parkingList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Parking parking = doc.toObject(Parking.class);
                            parking.setId(doc.getId()); // Guarda el ID para posibles futuras acciones
                            parkingList.add(parking);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
