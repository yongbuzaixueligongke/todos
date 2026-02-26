package com.example.imageview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private Context context;
    private List<Message> messages;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 获取当前消息
        Message message = messages.get(position);

        // 检查是否需要创建新视图
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_message, parent, false);
        }

        // 获取视图中的控件
        TextView titleTextView = convertView.findViewById(R.id.message_title);
        TextView contentTextView = convertView.findViewById(R.id.message_content);
        TextView timeTextView = convertView.findViewById(R.id.message_time);

        // 设置消息内容
        titleTextView.setText(message.getTitle());
        contentTextView.setText(message.getContent());
        timeTextView.setText(message.getTime());


        return convertView;
    }
}