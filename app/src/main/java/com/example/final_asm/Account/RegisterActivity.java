package com.example.final_asm.Account;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_asm.DAO.DBManager;
import com.example.final_asm.Model.User;
import com.example.final_asm.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhoneNumber, etPassword,etReapeatPassword;
    private Button btnSubmitRegister;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etEmail = findViewById(R.id.etEmail);
        etName = findViewById(R.id.etName);
        etPhoneNumber = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etReapeatPassword = findViewById(R.id.etRepeatPassword);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);
        dbManager = new DBManager(this);
        btnSubmitRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }
    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String repeatPassword = etReapeatPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEmailUsed(email)) {
            Toast.makeText(this, "Email is already in use", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.PHONE.matcher(phone).matches()) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() <= 5) {
            Toast.makeText(this, "Password must be longer than 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(repeatPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user to the database using DBManager
        dbManager.addUser(name, email, phone, password, repeatPassword);

        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();

        // Redirect to login or another activity
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isEmailUsed(String email) {
        Cursor cursor = dbManager.getReadableDatabase().rawQuery(
                "SELECT 1 FROM " + DBManager.USERS_TABLE + " WHERE " + DBManager.USER_EMAIL + " = ?",
                new String[]{email}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}

