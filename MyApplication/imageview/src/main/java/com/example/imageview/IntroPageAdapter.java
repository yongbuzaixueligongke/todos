package com.example.imageview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IntroPageAdapter extends RecyclerView.Adapter<IntroPageAdapter.ViewHolder> {

    private final int[] images = {
            R.drawable.start_01, // 替换为您的图片资源
            R.drawable.start_02,
            R.drawable.start_03,
            R.drawable.start_04
    };

    private final String[] titles = {
            "欢迎使用应用",
            "功能介绍",
            "使用指南",
            "开始体验"
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageView.setImageResource(images[position]);
        holder.titleTextView.setText(titles[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.intro_image);
            titleTextView = itemView.findViewById(R.id.intro_title);
        }
    }
}