package com.example.imageview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class PriorityTagSpinnerAdapter extends ArrayAdapter<String> {

    public PriorityTagSpinnerAdapter(@NonNull Context context, @NonNull List<String> tags) {
        super(context, android.R.layout.simple_spinner_item, tags);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        bind(view, getItem(position));
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        bind(view, getItem(position));
        return view;
    }

    private void bind(TextView view, String tag) {
        view.setCompoundDrawables(null, null, null, null);
        view.setCompoundDrawablePadding(PriorityTagUtils.dp(view, 8));
        if (PriorityTagUtils.isPriorityTag(tag)) {
            int iconSize = PriorityTagUtils.dp(view, 10);
            view.setCompoundDrawables(PriorityTagUtils.createDotDrawable(tag, iconSize), null, null, null);
        }
    }
}
