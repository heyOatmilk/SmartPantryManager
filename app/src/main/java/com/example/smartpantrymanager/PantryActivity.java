package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.List;

public class PantryActivity extends AppCompatActivity {

    private ListView listPantry;
    private TextView tvEmptyPantry, tvIngredientCount;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        Button add = findViewById(R.id.btnAddIngredient);
        listPantry = findViewById(R.id.listPantry);
        tvEmptyPantry = findViewById(R.id.tvEmptyPantry);
        tvIngredientCount = findViewById(R.id.tvIngredientCount);

        databaseHelper = new DatabaseHelper(this);

        add.setOnClickListener(v -> startActivity(new Intent(this, IngredientFormActivity.class)));

        listPantry.setOnItemClickListener((parent, view, position, id) -> {
            PantryItem item = (PantryItem) parent.getItemAtPosition(position);
            Intent intent = new Intent(this, IngredientFormActivity.class);
            intent.putExtra("ITEM_ID", item.getId());
            startActivity(intent);
        });

        listPantry.setOnItemLongClickListener((parent, view, position, id) -> {
            PantryItem item = (PantryItem) parent.getItemAtPosition(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Ingredient")
                    .setMessage("Are you sure you want to delete " + item.getName() + "?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (d, w) -> {
                        if (databaseHelper.deletePantryItem(item.getId()) > 0) {
                            Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                            loadPantryItems();
                        }
                    }).show();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        List<PantryItem> items = databaseHelper.getAllPantryItems();
        tvIngredientCount.setText(items.size() == 1 ? "1 ingredient" : items.size() + " ingredients");

        if (items.isEmpty()) {
            tvEmptyPantry.setVisibility(View.VISIBLE);
            listPantry.setVisibility(View.GONE);
        } else {
            tvEmptyPantry.setVisibility(View.GONE);
            listPantry.setVisibility(View.VISIBLE);
            listPantry.setAdapter(new PantryAdapter(this, items));
        }
    }
}