package com.example.final_asm.Apdater;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.final_asm.Model.Category;
import com.example.final_asm.R;

import java.util.List;
public class CategoryApdater extends ArrayAdapter<Category>  {

    public CategoryApdater(Context context, List<Category> categories) {
        super(context, 0, categories);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_category, parent, false);
        }

        // Get the data item for this position
        Category category = getItem(position);

        // Lookup views within item_category.xml
        TextView categoryName = convertView.findViewById(R.id.categoryName);
        TextView categoryAmount = convertView.findViewById(R.id.categoryAmount);

        // Populate the data into the template view using the data object
        categoryName.setText(category.getName());
        categoryAmount.setText(String.format("$%.2f", category.getAmount()));

        // Return the completed view to render on screen
        return convertView;
    }

}
