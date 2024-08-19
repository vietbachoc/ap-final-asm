package com.example.final_asm.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.final_asm.Apdater.CategoryApdater;
import com.example.final_asm.DAO.DBManager;
import com.example.final_asm.Model.Category;
import com.example.final_asm.Model.User;
import com.example.final_asm.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class BudgetFragment extends Fragment {

    private FloatingActionButton fabAddCategory;
    private ListView listViewCategories;
    private DBManager dbManager;
    private int userId;
    private int selectedCategoryId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        initializeViews(view);
        initializeDBManager();

        setUserIdFromSession();
        loadCategories();
        setupListeners(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshBudgetData();
    }

    private void initializeViews(View view) {
        fabAddCategory = view.findViewById(R.id.fabAddCategory);
        listViewCategories = view.findViewById(R.id.expensesList);
    }

    private void initializeDBManager() {
        dbManager = new DBManager(getActivity());
    }

    private void setUserIdFromSession() {
        User sessionUser = dbManager.retrieveSessionUser();
        if (sessionUser != null) {
            this.userId = sessionUser.getId();
        } else {
            Toast.makeText(getActivity(), "No user session found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners(View view) {
        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        listViewCategories.setOnItemClickListener((parent, itemView, position, id) -> {
            Category selectedCategory = (Category) parent.getItemAtPosition(position);
            selectedCategoryId = selectedCategory.getId();
            showUpdateCategoryDialog(selectedCategory);
        });

        listViewCategories.setOnItemLongClickListener((parent, itemView, position, id) -> {
            Category selectedCategory = (Category) parent.getItemAtPosition(position);
            selectedCategoryId = selectedCategory.getId();
            showDeleteCategoryDialog(selectedCategory);
            return true;
        });

        view.findViewById(R.id.BudgetSetup).setOnClickListener(v -> showSetInitialBudgetDialog());
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText categoryNameEditText = dialogView.findViewById(R.id.categoryNameEditText);
        EditText categoryBudgetEditText = dialogView.findViewById(R.id.categoryBudgetEditText);
        Button btnSaveCategory = dialogView.findViewById(R.id.btnSaveCategory);

        btnSaveCategory.setOnClickListener(v -> {
            String categoryName = categoryNameEditText.getText().toString().trim();
            String categoryBudget = categoryBudgetEditText.getText().toString().trim();

            if (validateInput(categoryName, categoryBudget)) {
                double budget = Double.parseDouble(categoryBudget);
                dbManager.createCategory(categoryName, budget, userId);
                refreshBudgetData();
                dialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateCategoryDialog(final Category category) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText categoryNameEditText = dialogView.findViewById(R.id.categoryNameEditText);
        EditText categoryBudgetEditText = dialogView.findViewById(R.id.categoryBudgetEditText);
        Button btnSaveCategory = dialogView.findViewById(R.id.btnSaveCategory);

        categoryNameEditText.setText(category.getName());
        categoryBudgetEditText.setText(String.valueOf(category.getAmount()));

        btnSaveCategory.setOnClickListener(v -> {
            String categoryName = categoryNameEditText.getText().toString().trim();
            String categoryBudget = categoryBudgetEditText.getText().toString().trim();

            if (validateInput(categoryName, categoryBudget)) {
                double newBudget = Double.parseDouble(categoryBudget);
                double totalExpenses = dbManager.calculateTotalExpenses(category.getId());

                if (newBudget >= totalExpenses) {
                    Category updatedCategory = new Category(category.getId(), categoryName, newBudget);
                    dbManager.modifyCategory(updatedCategory);
                    refreshBudgetData();
                    dialog.dismiss();
                } else {
                    showBudgetWarning();
                }
            } else {
                Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteCategoryDialog(final Category category) {
        boolean hasExpenses = dbManager.hasExpenses(category.getId());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("Delete Category");

        if (hasExpenses) {
            dialogBuilder.setMessage("Deleting this category will also delete all associated expenses. Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dbManager.removeCategory(category.getId());
                        refreshBudgetData();
                    });
        } else {
            dialogBuilder.setMessage("Are you sure you want to delete this category: " + category.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dbManager.removeCategory(category.getId());
                        refreshBudgetData();
                    });
        }

        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.show();
    }

    private void showSetInitialBudgetDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_initial_budget, null);
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText initialBudgetEditText = dialogView.findViewById(R.id.initialBudgetEditText);
        Button btnSaveBudget = dialogView.findViewById(R.id.btnSaveBudget);

        double currentInitialBudget = dbManager.getInitialBudget(userId);
        initialBudgetEditText.setText(String.valueOf(currentInitialBudget));

        btnSaveBudget.setOnClickListener(v -> {
            String initialBudgetStr = initialBudgetEditText.getText().toString().trim();
            if (!initialBudgetStr.isEmpty()) {
                double initialBudget = Double.parseDouble(initialBudgetStr);
                dbManager.updateInitialBudget(userId, initialBudget);
                refreshBudgetData();
                dialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "Please enter a budget", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        List<Category> categories = dbManager.fetchCategories(userId);
        CategoryApdater adapter = new CategoryApdater(getActivity(), categories);
        listViewCategories.setAdapter(adapter);
    }

    private void loadInitialBudget() {
        double initialBudget = dbManager.getInitialBudget(userId);
        TextView initialBudgetValueTextView = getView().findViewById(R.id.initialBudgetValue);
        initialBudgetValueTextView.setText(String.format("$%.2f", initialBudget));
    }

    private void loadCurrentBudget() {
        double initialBudget = dbManager.getInitialBudget(userId);
        double totalExpenses = dbManager.computeTotalExpense(userId);
        double currentBudget = initialBudget - totalExpenses;

        TextView currentBudgetValue = getView().findViewById(R.id.currentBudgetValue);
        currentBudgetValue.setText(String.format("$%.2f", currentBudget));

        updateCurrentBudgetColor(currentBudget, initialBudget);
    }

    private void updateCurrentBudgetColor(double currentBudget, double initialBudget) {
        double percentage = (currentBudget / initialBudget) * 100;
        LinearLayout currentBudgetLayout = getView().findViewById(R.id.currentBudgetLayout);

        if (percentage >= 75) {
            currentBudgetLayout.setBackgroundColor(getResources().getColor(R.color.my_green));
        } else if (percentage >= 50) {
            currentBudgetLayout.setBackgroundColor(getResources().getColor(R.color.my_yellow));
        } else if (percentage > 0) {
            currentBudgetLayout.setBackgroundColor(getResources().getColor(R.color.my_orange));
        } else {
            currentBudgetLayout.setBackgroundColor(getResources().getColor(R.color.my_red));
        }
    }

    private boolean validateInput(String name, String budget) {
        return !name.isEmpty() && !budget.isEmpty();
    }

    private void showBudgetWarning() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("Warning")
                .setMessage("New budget is less than the total expenses already incurred. Please adjust the budget.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void refreshBudgetData() {
        loadInitialBudget();
        loadCurrentBudget();
        loadCategories();
    }
}
