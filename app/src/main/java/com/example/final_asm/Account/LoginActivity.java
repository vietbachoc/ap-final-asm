package com.example.final_asm.Account;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final_asm.DAO.DBManager;
import com.example.final_asm.Model.User;
import com.example.final_asm.Apdater.NavigationApdater;
import com.example.final_asm.R;

public class LoginActivity extends AppCompatActivity {
    private EditText etUserName, etPassword;
    private TextView ToRegister;
    private Button btnLogin;
    private DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUserName = findViewById(R.id.etUserLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        ToRegister = findViewById(R.id.tvregister);
        dbManager = new DBManager(this);
        // Kiểm tra nếu đã có session, chuyển tới ExpenseManagerActivity
        if (dbManager.checkActiveSession()) {
            User user = dbManager.retrieveSessionUser();
            if (user != null) {
                navigateToExpenseManager(user.getId());
            }
        }
        btnLogin.setOnClickListener(v -> loginUser());
        ToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity .class);
            startActivity(intent);
        });
    }
    private void loginUser() {
        String username = etUserName.getText().toString();
        String password = etPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor cursor = dbManager.authenticateUser(username, password);
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(DBManager.USER_ID));
            String name = cursor.getString(cursor.getColumnIndex(DBManager.USERNAME));
            String phone = cursor.getString(cursor.getColumnIndex(DBManager.USER_PHONE));
            cursor.close();

            // Lưu thông tin người dùng vào session
            dbManager.storeSession(userId, name, username, phone, password);

            // Chuyển đến ExpenseManagerActivity
            navigateToExpenseManager(userId);
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();  // Đảm bảo đóng cursor nếu nó không null
        }
    }
    private void navigateToExpenseManager(int userId) {
        Intent intent = new Intent(this, NavigationApdater.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}