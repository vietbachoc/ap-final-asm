package com.example.final_asm.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.final_asm.Apdater.ExpenseAdapter;
import com.example.final_asm.DAO.DBManager;
import com.example.final_asm.Model.Expense;
import com.example.final_asm.Model.User;
import com.example.final_asm.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private FloatingActionButton fabAddExpense;
    private ListView listViewExpenses;
    private DBManager dbManager;
    private int userId;
    private String selectedDate;
    private TextView totalExpensesValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        fabAddExpense = view.findViewById(R.id.fabAddExpense);
        listViewExpenses = view.findViewById(R.id.expensesList);
        totalExpensesValue = view.findViewById(R.id.totalExpensesValue);

        // Initialize DBManager and get user ID from session
        dbManager = new DBManager(getActivity());
        setUserIdFromSession();

        // Load data
        loadExpenses();
        loadTotal();

        // Set click listener for adding new expense
        fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dbManager.close();
    }

    private void showAddExpenseDialog() {
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_expense, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText expenseNameEditText = dialogView.findViewById(R.id.expenseName);
        EditText amountEditText = dialogView.findViewById(R.id.expenseAmount);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);
        Button btnSaveExpense = dialogView.findViewById(R.id.btnAddExpense);

        selectedDate = getCurrentDate();
        btnSelectDate.setText(selectedDate);

        loadCategoriesIntoSpinner(categorySpinner);

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog(btnSelectDate));

        btnSaveExpense.setOnClickListener(v -> {
            String expenseName = expenseNameEditText.getText().toString();
            String amountStr = amountEditText.getText().toString();
            String categoryName = categorySpinner.getSelectedItem().toString();

            if (validateInputs(expenseName, amountStr)) {
                double amount = Double.parseDouble(amountStr);
                int categoryId = getCategoryIdByName(categoryName);
                double totalExpenses = dbManager.calculateTotalExpenses(categoryId);
                double budget = dbManager.fetchCategories(userId).stream()
                        .filter(category -> category.getId() == categoryId)
                        .findFirst()
                        .map(category -> category.getAmount())
                        .orElse(0.0);

                if (totalExpenses + amount > budget) {
                    Toast.makeText(getActivity(), "Exceeds budget limit!", Toast.LENGTH_SHORT).show();
                } else {
                    dbManager.logExpense(expenseName, amount, categoryId, userId, selectedDate);
                    refreshData();
                    dialog.dismiss();
                }
            }
        });
    }

    private void showDatePickerDialog(Button dateButton) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                    dateButton.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }
    private void loadCategoriesIntoSpinner(Spinner spinner) {
        List<String> categoryNames = dbManager.fetchCategories(userId).stream()
                .map(category -> category.getName())
                .collect(Collectors.toList());  // Changed from toList() to collect(Collectors.toList())
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void loadExpenses() {
        List<Expense> expenses = dbManager.fetchExpenses(userId);
        ExpenseAdapter adapter = new ExpenseAdapter(getActivity(), expenses,
                this::showUpdateExpenseDialog,
                this::showDeleteConfirmationDialog);
        listViewExpenses.setAdapter(adapter);
    }

    private void loadTotal() {
        double totalExpenses = dbManager.computeTotalExpense(userId);
        totalExpensesValue.setText(String.format("$%.2f", totalExpenses));
    }

    private void setUserIdFromSession() {
        User sessionUser = dbManager.retrieveSessionUser();
        if (sessionUser != null) {
            this.userId = sessionUser.getId();
        }
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private int getCategoryIdByName(String categoryName) {
        return dbManager.fetchCategories(userId).stream()
                .filter(category -> category.getName().equals(categoryName))
                .findFirst()
                .map(category -> category.getId())
                .orElse(-1);
    }

    private boolean validateInputs(String name, String amountStr) {
        if (name.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void refreshData() {
        loadExpenses();
        loadTotal();
    }

    private void showUpdateExpenseDialog(Expense expense) {
        // Implement the dialog to update an existing expense
    }

    private void showDeleteConfirmationDialog(Expense expense) {
        // Implement the dialog to confirm deletion of an expense
    }
}
