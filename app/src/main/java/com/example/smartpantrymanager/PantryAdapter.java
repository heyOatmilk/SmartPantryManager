package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.smartpantrymanager.model.PantryItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends BaseAdapter {
    private final Context context;
    private final List<PantryItem> pantryItems;

    public PantryAdapter(Context context, List<PantryItem> pantryItems) {
        this.context = context;
        this.pantryItems = pantryItems;
    }

    @Override public int getCount() { return pantryItems.size(); }
    @Override public Object getItem(int position) { return pantryItems.get(position); }
    @Override public long getItemId(int position) { return pantryItems.get(position).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pantry, parent, false);

        TextView name = convertView.findViewById(R.id.tvItemName);
        TextView quantity = convertView.findViewById(R.id.tvItemQuantity);
        TextView expiry = convertView.findViewById(R.id.tvItemExpiry);
        TextView warning = convertView.findViewById(R.id.tvExpiryWarning);

        PantryItem item = pantryItems.get(position);
        name.setText(item.getName());

        String q = item.getQuantity() == Math.floor(item.getQuantity())
                ? (int) item.getQuantity() + " " + item.getUnit()
                : item.getQuantity() + " " + item.getUnit();
        quantity.setText(q);

        expiry.setText(item.getExpiryDate() == null || item.getExpiryDate().isEmpty()
                ? "No expiry date" : "Expires: " + item.getExpiryDate());

        SharedPreferences p = context.getSharedPreferences("SmartPantrySettings", Context.MODE_PRIVATE);
        boolean enabled = p.getBoolean("expiry_reminders", true);
        warning.setVisibility(View.GONE);
        if (enabled && item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()
                && isExpiringSoon(item.getExpiryDate())) warning.setVisibility(View.VISIBLE);

        return convertView;
    }

    private boolean isExpiringSoon(String expiryDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            format.setLenient(false);
            Date expiry = format.parse(expiryDate);
            if (expiry == null) return false;

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);

            Calendar end = Calendar.getInstance(); end.setTime(expiry);
            end.set(Calendar.HOUR_OF_DAY, 0); end.set(Calendar.MINUTE, 0);
            end.set(Calendar.SECOND, 0); end.set(Calendar.MILLISECOND, 0);

            long days = (end.getTimeInMillis() - today.getTimeInMillis()) / (1000L * 60 * 60 * 24);
            return days >= 0 && days <= 3;
        } catch (Exception e) { return false; }
    }
}