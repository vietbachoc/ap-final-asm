package com.example.final_asm.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.final_asm.R;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.example.final_asm.DAO.DBManager;
import com.example.final_asm.Account.LoginActivity;
import com.example.final_asm.Model.User;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone;
    private Button btnEdit, btnLogout;
    private DBManager dbManager;
    private User sessionUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupListeners();
        loadUserInfo();

        return view;
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnLogout = view.findViewById(R.id.btn_logout);
        dbManager = new DBManager(getActivity());
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> showEditUserDialog());
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void loadUserInfo() {
        sessionUser = dbManager.retrieveSessionUser();
        if (sessionUser != null) {
            tvName.setText("Name: " + sessionUser.getName());
            tvEmail.setText("Email: " + sessionUser.getEmail());
            tvPhone.setText("Phone: " + sessionUser.getPhone());
        } else {
            showToast("Failed to load user info");
        }
    }

    private void showEditUserDialog() {
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_user, null);
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .create();
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etRepeatNewPassword = dialogView.findViewById(R.id.et_new_repeat_password);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.et_confirm_password);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);

        setInitialValues(etName, etEmail, etPhone);

        btnSubmit.setOnClickListener(v -> {
            String newName = etName.getText().toString();
            String newEmail = etEmail.getText().toString();
            String newPhone = etPhone.getText().toString();
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String repeatNewPassword = etRepeatNewPassword.getText().toString();
            String confirmNewPassword = etConfirmNewPassword.getText().toString();

            if (validateFields(newName, newEmail, newPhone, currentPassword, newPassword, repeatNewPassword, confirmNewPassword)) {
                if (validatePasswordChange(currentPassword, newPassword, repeatNewPassword, confirmNewPassword)) {
                    updateUser(newName, newEmail, newPhone, newPassword, repeatNewPassword);
                    updateUI(newName, newEmail, newPhone);
                    showToast("User updated successfully.");
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void updateUI(String newName, String newEmail, String newPhone) {
        tvName.setText("Name: " + newName);
        tvEmail.setText("Email: " + newEmail);
        tvPhone.setText("Phone: " + newPhone);
    }

    private void setInitialValues(EditText etName, EditText etEmail, EditText etPhone) {
        etName.setText(sessionUser.getName());
        etEmail.setText(sessionUser.getEmail());
        etPhone.setText(sessionUser.getPhone());
    }

    private boolean validateFields(String name, String email, String phone, String currentPassword, String newPassword, String repeatNewPassword, String confirmNewPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            showToast("Name must be filled");
            isValid = false;
        }
        if (TextUtils.isEmpty(email)) {
            showToast("Email must be filled");
            isValid = false;
        }
        if (TextUtils.isEmpty(phone)) {
            showToast("Phone number must be filled");
            isValid = false;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            showToast("Current Password must be filled");
            isValid = false;
        }

        if ((!TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmNewPassword)) &&
                (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword))) {
            showToast("New Password and Confirm New Password must both be filled if changing password");
            isValid = false;
        }

        if (!newPassword.equals(repeatNewPassword)) {
            showToast("New Password and Repeat New Password do not match");
            isValid = false;
        }

        return isValid;
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword, String repeatNewPassword, String confirmNewPassword) {
        if (!currentPassword.equals(sessionUser.getPassword())) {
            showToast("Current password is incorrect");
            return false;
        }
        if (newPassword.length() < 6) {
            showToast("New password must be at least 6 characters long");
            return false;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            showToast("New passwords do not match");
            return false;
        }
        return true;
    }

    private void updateUser(String newName, String newEmail, String newPhone, String newPassword, String repeatNewPassword) {
        User updatedUser = new User(
                sessionUser.getId(),
                TextUtils.isEmpty(newName) ? sessionUser.getName() : newName,
                TextUtils.isEmpty(newEmail) ? sessionUser.getEmail() : newEmail,
                TextUtils.isEmpty(newPhone) ? sessionUser.getPhone() : newPhone,
                TextUtils.isEmpty(newPassword) ? sessionUser.getPassword() : newPassword,
                TextUtils.isEmpty(repeatNewPassword) ? sessionUser.getRepeatPassword() : repeatNewPassword
        );
        dbManager.updateUser(updatedUser);
    }
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    dbManager.eraseSession(); // Remove session
                    showToast("Logged out successfully");
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
