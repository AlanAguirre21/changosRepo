package com.example.parkgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText identifierEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        identifierEditText = findViewById(R.id.editTextIdentifier);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> loginAdmin());
    }

    private void loginAdmin() {
        String input = identifierEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si es NIP (solo dígitos)
        if (input.matches("\\d+")) {
            db.collection("admins")
                    .whereEqualTo("nip", input)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String email = queryDocumentSnapshots.getDocuments().get(0).getString("email");
                            signInWithEmail(email, password);
                        } else {
                            Toast.makeText(this, "NIP inválido", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al buscar NIP", Toast.LENGTH_SHORT).show());

        } else {
            // Si es email
            signInWithEmail(input, password);
        }
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                    // TODO: redirigir a pantalla principal de admin
                    Intent intent = new Intent(AdminLoginActivity.this, ManageParkingsActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Credenciales inválidas", Toast.LENGTH_SHORT).show());
    }
}

