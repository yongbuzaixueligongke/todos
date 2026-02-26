package com.example.imageview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 待办列表适配器，支持 CheckBox 勾选状态与数据库同步。
 */
public class TodoAdapter extends BaseAdapter {

    public interface OnTodoCheckedListener {
        void onCheckedChanged(TodoItem item, boolean checked);
    }

    private final Context context;
    private List<TodoItem> items;
    private final OnTodoCheckedListener checkedListener;

    public TodoAdapter(Context context, List<TodoItem> items, OnTodoCheckedListener checkedListener) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
        this.checkedListener = checkedListener;
    }

    public void setItems(List<TodoItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_message, parent, false);
        }

        TodoItem item = items.get(position);
        TextView titleTextView = convertView.findViewById(R.id.message_title);
        TextView contentTextView = convertView.findViewById(R.id.message_content);
        TextView timeTextView = convertView.findViewById(R.id.message_time);
        CheckBox checkBox = convertView.findViewById(R.id.message_checkbox);

        titleTextView.setText(item.getTitle());
        contentTextView.setText(item.getContent());
        timeTextView.setText(item.getTime());

        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(item.isCompleted());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setCompleted(isChecked);
                if (checkedListener != null) {
                    checkedListener.onCheckedChanged(item, isChecked);
                }
            }
        });

        return convertView;
    }
}
