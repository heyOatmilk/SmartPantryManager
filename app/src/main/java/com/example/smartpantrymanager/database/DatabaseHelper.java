package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_PANTRY = "pantry_items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_UNIT = "unit";
    private static final String COLUMN_EXPIRY = "expiry_date";

    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    private static final String RECIPE_ID = "id";
    private static final String RECIPE_NAME = "name";
    private static final String RECIPE_INSTRUCTIONS = "instructions";
    private static final String RI_ID = "id";
    private static final String RI_RECIPE_ID = "recipe_id";
    private static final String RI_INGREDIENT_NAME = "ingredient_name";
    private static final String RI_QUANTITY = "quantity";
    private static final String RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_QUANTITY + " REAL NOT NULL, " +
                COLUMN_UNIT + " TEXT NOT NULL, " +
                COLUMN_EXPIRY + " TEXT)");
        createRecipeTables(db);
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Existing users keep their pantry data — only add the new recipe tables.
        if (oldVersion < 2) {
            createRecipeTables(db);
            seedRecipes(db);
        }
    }

    private void createRecipeTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RECIPE_NAME + " TEXT NOT NULL, " +
                RECIPE_INSTRUCTIONS + " TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECIPE_INGREDIENTS + " (" +
                RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RI_RECIPE_ID + " INTEGER NOT NULL, " +
                RI_INGREDIENT_NAME + " TEXT NOT NULL, " +
                RI_QUANTITY + " REAL NOT NULL, " +
                RI_UNIT + " TEXT NOT NULL, FOREIGN KEY(" + RI_RECIPE_ID + ") REFERENCES " +
                TABLE_RECIPES + "(" + RECIPE_ID + "))");
    }

    // ----- Pantry CRUD -----

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_NAME, item.getName());
        v.put(COLUMN_QUANTITY, item.getQuantity());
        v.put(COLUMN_UNIT, item.getUnit());
        v.put(COLUMN_EXPIRY, item.getExpiryDate());
        long result = db.insert(TABLE_PANTRY, null, v);
        db.close();
        return result;
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, COLUMN_NAME + " ASC");
        if (c.moveToFirst()) {
            do {
                out.add(new PantryItem(
                        c.getInt(c.getColumnIndexOrThrow(COLUMN_ID)),
                        c.getString(c.getColumnIndexOrThrow(COLUMN_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                        c.getString(c.getColumnIndexOrThrow(COLUMN_UNIT)),
                        c.getString(c.getColumnIndexOrThrow(COLUMN_EXPIRY))));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return out;
    }

    public PantryItem getPantryItem(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = new PantryItem(id,
                    c.getString(c.getColumnIndexOrThrow(COLUMN_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_UNIT)),
                    c.getString(c.getColumnIndexOrThrow(COLUMN_EXPIRY)));
        }
        c.close();
        db.close();
        return item;
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_NAME, item.getName());
        v.put(COLUMN_QUANTITY, item.getQuantity());
        v.put(COLUMN_UNIT, item.getUnit());
        v.put(COLUMN_EXPIRY, item.getExpiryDate());
        int rows = db.update(TABLE_PANTRY, v, COLUMN_ID + "=?",
                new String[]{String.valueOf(item.getId())});
        db.close();
        return rows;
    }

    public int deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_PANTRY, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }


    public List<Recipe> getAllRecipes() {
        List<Recipe> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, RECIPE_NAME + " ASC");
        if (c.moveToFirst()) {
            do {
                out.add(new Recipe(
                        c.getInt(c.getColumnIndexOrThrow(RECIPE_ID)),
                        c.getString(c.getColumnIndexOrThrow(RECIPE_NAME)),
                        c.getString(c.getColumnIndexOrThrow(RECIPE_INSTRUCTIONS))));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return out;
    }

    public Recipe getRecipeById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, RECIPE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe r = null;
        if (c.moveToFirst()) {
            r = new Recipe(id,
                    c.getString(c.getColumnIndexOrThrow(RECIPE_NAME)),
                    c.getString(c.getColumnIndexOrThrow(RECIPE_INSTRUCTIONS)));
        }
        c.close();
        db.close();
        return r;
    }

    public List<RecipeIngredient> getRecipeIngredients(int recipeId) {
        List<RecipeIngredient> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        if (c.moveToFirst()) {
            do {
                out.add(new RecipeIngredient(
                        c.getInt(c.getColumnIndexOrThrow(RI_ID)), recipeId,
                        c.getString(c.getColumnIndexOrThrow(RI_INGREDIENT_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(RI_QUANTITY)),
                        c.getString(c.getColumnIndexOrThrow(RI_UNIT))));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return out;
    }

    // ----- Recipe seeding -----

    private long insertRecipe(SQLiteDatabase db, String name, String instructions) {
        ContentValues v = new ContentValues();
        v.put(RECIPE_NAME, name);
        v.put(RECIPE_INSTRUCTIONS, instructions);
        return db.insert(TABLE_RECIPES, null, v);
    }

    private void insertRecipeIngredient(SQLiteDatabase db, long recipeId, String name, double quantity, String unit) {
        ContentValues v = new ContentValues();
        v.put(RI_RECIPE_ID, recipeId);
        v.put(RI_INGREDIENT_NAME, name);
        v.put(RI_QUANTITY, quantity);
        v.put(RI_UNIT, unit);
        db.insert(TABLE_RECIPE_INGREDIENTS, null, v);
    }

    private void seedRecipes(SQLiteDatabase db) {
        Cursor check = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPES, null);
        boolean hasRecipes = check.moveToFirst() && check.getInt(0) > 0;
        check.close();
        if (hasRecipes) return;

        long id;
        id = insertRecipe(db, "Scrambled Eggs", "1. Crack the eggs into a bowl.\n2. Beat the eggs.\n3. Melt butter in a pan.\n4. Add the eggs and stir gently until cooked.");
        insertRecipeIngredient(db, id, "Eggs", 2, "pieces"); insertRecipeIngredient(db, id, "Butter", 1, "tbsp"); insertRecipeIngredient(db, id, "Salt", 1, "tsp");

        id = insertRecipe(db, "Boiled Eggs", "1. Place the eggs in a pot.\n2. Cover with water.\n3. Boil for 8 to 10 minutes.\n4. Cool and peel.");
        insertRecipeIngredient(db, id, "Eggs", 2, "pieces");

        id = insertRecipe(db, "Tomato Omelette", "1. Beat the eggs.\n2. Chop the tomatoes.\n3. Mix the tomatoes with the eggs.\n4. Cook in a lightly greased pan.");
        insertRecipeIngredient(db, id, "Eggs", 2, "pieces"); insertRecipeIngredient(db, id, "Tomatoes", 2, "pieces"); insertRecipeIngredient(db, id, "Salt", 1, "tsp");

        id = insertRecipe(db, "Cheese Omelette", "1. Beat the eggs.\n2. Heat butter in a pan.\n3. Add the eggs.\n4. Sprinkle cheese over the eggs.\n5. Fold and cook until ready.");
        insertRecipeIngredient(db, id, "Eggs", 2, "pieces"); insertRecipeIngredient(db, id, "Cheese", 50, "g"); insertRecipeIngredient(db, id, "Butter", 1, "tbsp");

        id = insertRecipe(db, "Egg Sandwich", "1. Boil or fry the eggs.\n2. Place the eggs between slices of bread.\n3. Add a little salt and serve.");
        insertRecipeIngredient(db, id, "Eggs", 2, "pieces"); insertRecipeIngredient(db, id, "Bread", 2, "pieces"); insertRecipeIngredient(db, id, "Salt", 1, "tsp");

        id = insertRecipe(db, "Cheese Sandwich", "1. Place cheese between two slices of bread.\n2. Serve immediately or toast lightly.");
        insertRecipeIngredient(db, id, "Bread", 2, "pieces"); insertRecipeIngredient(db, id, "Cheese", 50, "g");

        id = insertRecipe(db, "Tomato Sandwich", "1. Slice the tomatoes.\n2. Place them between slices of bread.\n3. Add a small amount of salt.");
        insertRecipeIngredient(db, id, "Bread", 2, "pieces"); insertRecipeIngredient(db, id, "Tomatoes", 1, "pieces"); insertRecipeIngredient(db, id, "Salt", 1, "tsp");

        id = insertRecipe(db, "Tuna Sandwich", "1. Drain the tuna.\n2. Place the tuna between slices of bread.\n3. Serve immediately.");
        insertRecipeIngredient(db, id, "Bread", 2, "pieces"); insertRecipeIngredient(db, id, "Tuna", 100, "g");

        id = insertRecipe(db, "Tomato Pasta", "1. Boil the pasta.\n2. Chop and cook the tomatoes.\n3. Mix the cooked pasta with the tomatoes.\n4. Add salt and serve.");
        insertRecipeIngredient(db, id, "Pasta", 200, "g"); insertRecipeIngredient(db, id, "Tomatoes", 3, "pieces"); insertRecipeIngredient(db, id, "Salt", 1, "tsp");

        id = insertRecipe(db, "Garlic Pasta", "1. Boil the pasta.\n2. Chop the garlic.\n3. Melt butter in a pan.\n4. Cook the garlic briefly.\n5. Mix with the pasta.");
        insertRecipeIngredient(db, id, "Pasta", 200, "g"); insertRecipeIngredient(db, id, "Garlic", 2, "pieces"); insertRecipeIngredient(db, id, "Butter", 1, "tbsp");

        id = insertRecipe(db, "Cheese Pasta", "1. Boil the pasta.\n2. Drain the pasta.\n3. Add cheese and mix until melted.");
        insertRecipeIngredient(db, id, "Pasta", 200, "g"); insertRecipeIngredient(db, id, "Cheese", 75, "g");

        id = insertRecipe(db, "Mashed Potatoes", "1. Peel and boil the potatoes.\n2. Drain them.\n3. Add milk and butter.\n4. Mash until smooth.");
        insertRecipeIngredient(db, id, "Potatoes", 4, "pieces"); insertRecipeIngredient(db, id, "Milk", 100, "ml"); insertRecipeIngredient(db, id, "Butter", 1, "tbsp");

        id = insertRecipe(db, "Potato Omelette", "1. Boil and slice the potatoes.\n2. Beat the eggs.\n3. Combine potatoes and eggs.\n4. Cook in a pan until firm.");
        insertRecipeIngredient(db, id, "Potatoes", 2, "pieces"); insertRecipeIngredient(db, id, "Eggs", 2, "pieces");

        id = insertRecipe(db, "Banana Oatmeal", "1. Heat the milk.\n2. Add oats and cook gently.\n3. Slice the banana.\n4. Add the banana and serve.");
        insertRecipeIngredient(db, id, "Oats", 1, "cup"); insertRecipeIngredient(db, id, "Milk", 250, "ml"); insertRecipeIngredient(db, id, "Banana", 1, "pieces");

        id = insertRecipe(db, "Plain Oatmeal", "1. Heat the milk.\n2. Add oats.\n3. Stir and cook until thickened.");
        insertRecipeIngredient(db, id, "Oats", 1, "cup"); insertRecipeIngredient(db, id, "Milk", 250, "ml");

        id = insertRecipe(db, "Egg Fried Rice", "1. Cook the rice if needed.\n2. Heat butter in a pan.\n3. Add the eggs and scramble.\n4. Add rice and mix well.");
        insertRecipeIngredient(db, id, "Rice", 2, "cup"); insertRecipeIngredient(db, id, "Eggs", 2, "pieces"); insertRecipeIngredient(db, id, "Butter", 1, "tbsp");

        id = insertRecipe(db, "Tomato Rice", "1. Cook the rice.\n2. Chop the tomatoes and onion.\n3. Cook the vegetables.\n4. Mix with the rice.");
        insertRecipeIngredient(db, id, "Rice", 2, "cup"); insertRecipeIngredient(db, id, "Tomatoes", 2, "pieces"); insertRecipeIngredient(db, id, "Onion", 1, "pieces");

        id = insertRecipe(db, "Chicken Rice", "1. Cook the rice.\n2. Cook the chicken thoroughly.\n3. Combine chicken and rice.\n4. Season lightly and serve.");
        insertRecipeIngredient(db, id, "Rice", 2, "cup"); insertRecipeIngredient(db, id, "Chicken", 250, "g"); insertRecipeIngredient(db, id, "Salt", 1, "tsp");

        id = insertRecipe(db, "Simple Pancakes", "1. Mix flour, milk and eggs.\n2. Heat butter in a pan.\n3. Pour in small amounts of batter.\n4. Cook both sides until golden.");
        insertRecipeIngredient(db, id, "Flour", 1, "cup"); insertRecipeIngredient(db, id, "Milk", 250, "ml"); insertRecipeIngredient(db, id, "Eggs", 1, "pieces"); insertRecipeIngredient(db, id, "Butter", 1, "tbsp");

        id = insertRecipe(db, "Garlic Potatoes", "1. Peel and cut the potatoes.\n2. Boil or roast until tender.\n3. Add chopped garlic and butter.\n4. Mix and serve.");
        insertRecipeIngredient(db, id, "Potatoes", 4, "pieces"); insertRecipeIngredient(db, id, "Garlic", 2, "pieces"); insertRecipeIngredient(db, id, "Butter", 1, "tbsp");
    }
}