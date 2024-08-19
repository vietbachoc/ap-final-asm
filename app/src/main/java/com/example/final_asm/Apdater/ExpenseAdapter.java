package com.example.final_asm.Apdater;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.final_asm.DAO.DBManager;
import com.example.final_asm.Model.Expense;
import com.example.final_asm.R;

import java.util.List;

public class ExpenseAdapter extends ArrayAdapter<Expense> {

    private OnUpdateClickListener onUpdateClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private DBManager dbManager;

    public ExpenseAdapter(Context context, List<Expense> expenses, OnUpdateClickListener onUpdateClickListener, OnDeleteClickListener onDeleteClickListener) {
        super(context, 0, expenses);
        this.onUpdateClickListener = onUpdateClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
        this.dbManager = new DBManager(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_expense, parent, false);
        }

        Expense expense = getItem(position);

        TextView nameTextView = convertView.findViewById(R.id.expenseName);
        TextView amountTextView = convertView.findViewById(R.id.expenseAmount);
        TextView expenseCategoryTextView = convertView.findViewById(R.id.expenseCategory);
        TextView expenseDateTextView = convertView.findViewById(R.id.expenseDate);
        ImageView updateIcon = convertView.findViewById(R.id.ic_update);
        ImageView deleteIcon = convertView.findViewById(R.id.ic_delete);

        if (expense != null) {
            nameTextView.setText(expense.getName());
            amountTextView.setText("$" + String.valueOf(expense.getCost()));
            expenseDateTextView.setText(expense.getDate());

            String categoryName = dbManager.fetchCategoryNameById(expense.getCategoryId());
            expenseCategoryTextView.setText(categoryName);

            updateIcon.setOnClickListener(v -> {
                if (onUpdateClickListener != null) {
                    Log.d("ExpenseAdapter", "Expense ID for update: " + expense.getId());
                    onUpdateClickListener.onUpdateClick(expense);
                }
            });

            deleteIcon.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(expense);
                }
            });
        }

        return convertView;
    }

    public interface OnUpdateClickListener {
        void onUpdateClick(Expense expense);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Expense expense);
    }
}
