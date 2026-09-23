package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.Calendar;

public class IngredientFormActivity extends AppCompatActivity {

    private int editItemId = -1;
    private EditText etIngredientName, etQuantity, etExpiryDate;
    private AutoCompleteTextView unitDropdown;
    private Button btnSaveIngredient;
    private TextView tvFormTitle;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_form);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        etIngredientName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        unitDropdown = findViewById(R.id.unitDropdown);
        btnSaveIngredient = findViewById(R.id.btnSaveIngredient);

        databaseHelper = new DatabaseHelper(this);

        setupUnitDropdown();
        setupDatePicker();

        editItemId = getIntent().getIntExtra("ITEM_ID", -1);
        if (editItemId != -1) loadIngredientForEditing();
        else loadDefaultUnit();

        btnSaveIngredient.setOnClickListener(v -> saveIngredient());
    }

    private void setupUnitDropdown() {
        String[] units = {"pieces", "g", "kg", "ml", "L", "cup", "tbsp", "tsp"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_dropdown_item_1line, units) {
            @Override
            public android.view.View getView(int position, android.view.View convertView,
                                             android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                android.widget.TextView t = (android.widget.TextView) view;
                t.setTextColor(android.graphics.Color.parseColor("#222222"));
                t.setTextSize(16);
                return view;
            }

            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView,
                                                     android.view.ViewGroup parent) {
                android.view.View view = super.getDropDownView(position, convertView, parent);
                android.widget.TextView t = (android.widget.TextView) view;
                t.setTextColor(android.graphics.Color.parseColor("#222222"));
                t.setTextSize(16);
                t.setBackgroundColor(android.graphics.Color.WHITE);
                t.setPadding(24, 18, 24, 18);
                return view;
            }
        };

        unitDropdown.setAdapter(adapter);
        unitDropdown.setTextColor(android.graphics.Color.parseColor("#222222"));
        unitDropdown.setHintTextColor(android.graphics.Color.parseColor("#777777"));
        unitDropdown.setDropDownBackgroundResource(android.R.color.white);
        unitDropdown.setOnClickListener(v -> unitDropdown.showDropDown());
        unitDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) unitDropdown.showDropDown();
        });
    }

    private void loadDefaultUnit() {
        SharedPreferences p = getSharedPreferences("SmartPantrySettings", MODE_PRIVATE);
        unitDropdown.setText(p.getString("default_unit", "pieces"), false);
    }

    private void setupDatePicker() {
        etExpiryDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) ->
                    etExpiryDate.setText(d + "/" + (m + 1) + "/" + y),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void loadIngredientForEditing() {
        PantryItem item = databaseHelper.getPantryItem(editItemId);
        if (item == null) {
            Toast.makeText(this, "Ingredient could not be loaded", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        etIngredientName.setText(item.getName());
        if (item.getQuantity() == Math.floor(item.getQuantity()))
            etQuantity.setText(String.valueOf((int) item.getQuantity()));
        else etQuantity.setText(String.valueOf(item.getQuantity()));
        unitDropdown.setText(item.getUnit(), false);
        etExpiryDate.setText(item.getExpiryDate());
        tvFormTitle.setText("Edit Ingredient");
        btnSaveIngredient.setText("Update Ingredient");
    }

    private void saveIngredient() {
        String name = etIngredientName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String unit = unitDropdown.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        if (name.isEmpty()) { etIngredientName.setError("Ingredient name is required"); return; }
        if (quantityText.isEmpty()) { etQuantity.setError("Quantity is required"); return; }
        if (unit.isEmpty()) { unitDropdown.setError("Please select a unit"); return; }

        double quantity;
        try { quantity = Double.parseDouble(quantityText); }
        catch (NumberFormatException e) { etQuantity.setError("Enter a valid quantity"); return; }

        if (quantity <= 0) { etQuantity.setError("Quantity must be greater than zero"); return; }

        if (editItemId == -1) {
            PantryItem item = new PantryItem(name, quantity, unit, expiryDate);
            long result = databaseHelper.addPantryItem(item);
            if (result != -1) {
                Toast.makeText(this, "Ingredient added successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else Toast.makeText(this, "Unable to add ingredient", Toast.LENGTH_SHORT).show();
        } else {
            PantryItem item = new PantryItem(editItemId, name, quantity, unit, expiryDate);
            int rows = databaseHelper.updatePantryItem(item);
            if (rows > 0) {
                Toast.makeText(this, "Ingredient updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else Toast.makeText(this, "Unable to update ingredient", Toast.LENGTH_SHORT).show();
        }
    }
}