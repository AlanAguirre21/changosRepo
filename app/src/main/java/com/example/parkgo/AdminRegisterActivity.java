package com.example.parkgo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AdminRegisterActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        registerButton = findViewById(R.id.buttonRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(v -> registerAdmin());
    }

    private void registerAdmin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
            if (isNewUser) {
                mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                    String nip = generateRandomNip();
                    String uid = authResult.getUser().getUid();

                    Map<String, Object> adminData = new HashMap<>();
                    adminData.put("email", email);
                    adminData.put("nip", nip);

                    db.collection("admins").document(uid).set(adminData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Administrador registrado. NIP: " + nip, Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar el NIP", Toast.LENGTH_SHORT).show());

                }).addOnFailureListener(e -> Toast.makeText(this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Este correo ya está registrado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generateRandomNip() {
        Random random = new Random();
        int nip = 100000 + random.nextInt(900000); // NIP de 6 dígitos
        return String.valueOf(nip);
    }

}
