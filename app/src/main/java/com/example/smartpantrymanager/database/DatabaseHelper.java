package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PANTRY = "pantry_items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_UNIT = "unit";
    private static final String COLUMN_EXPIRY = "expiry_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // This SQL creates the pantry_items table the first time the app runs.
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_QUANTITY + " REAL NOT NULL, " +
                COLUMN_UNIT + " TEXT NOT NULL, " +
                COLUMN_EXPIRY + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No upgrade logic needed yet at version 1.
    }

    // Insert a new pantry item and return its new row ID (-1 if it failed).
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

    // Returns every pantry item, sorted alphabetically by name.
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

    // Loads a single pantry item by its database ID, used when editing.
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

    // Updates an existing pantry item's values, matched by its ID.
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

    // Deletes a pantry item by its database ID. Returns the number of rows deleted.
    public int deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_PANTRY, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
}