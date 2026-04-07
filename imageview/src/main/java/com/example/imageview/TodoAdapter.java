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

    public void notifyItemInserted(int position) {
        notifyDataSetChanged();
    }

    public void notifyItemChanged(int position) {
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

    private final CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ViewHolder holder = (ViewHolder) buttonView.getTag();
            TodoItem item = holder.item;
            item.setCompleted(isChecked);
            if (checkedListener != null) {
                checkedListener.onCheckedChanged(item, isChecked);
            }
        }
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_message, parent, false);
            holder = new ViewHolder();
            holder.titleTextView = convertView.findViewById(R.id.message_title);
            holder.contentTextView = convertView.findViewById(R.id.message_content);
            holder.timeTextView = convertView.findViewById(R.id.message_time);
            holder.checkBox = convertView.findViewById(R.id.message_checkbox);
            holder.checkBox.setTag(holder);
            holder.checkBox.setOnCheckedChangeListener(checkBoxListener);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TodoItem item = items.get(position);
        holder.item = item;
        holder.titleTextView.setText(item.getTitle());
        holder.contentTextView.setText(item.getContent());
        holder.timeTextView.setText(item.getTime());

        // 直接设置状态，因为监听器已经通过holder关联到了正确的item
        holder.checkBox.setChecked(item.isCompleted());

        return convertView;
    }

    private static class ViewHolder {
        TodoItem item;
        TextView titleTextView;
        TextView contentTextView;
        TextView timeTextView;
        CheckBox checkBox;
    }
}
