package com.example.imageview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TagManagementAdapter extends RecyclerView.Adapter<TagManagementAdapter.TagViewHolder> {

    public interface OnTagClickListener {
        void onTagClick(TagManagementActivity.TagSummary tag);
    }

    private final List<TagManagementActivity.TagSummary> tags;
    private final OnTagClickListener clickListener;
    private final OnTagClickListener deleteListener;

    public TagManagementAdapter(List<TagManagementActivity.TagSummary> tags,
                                OnTagClickListener clickListener,
                                OnTagClickListener deleteListener) {
        this.tags = tags;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_management, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        TagManagementActivity.TagSummary tag = tags.get(position);
        PriorityTagUtils.applyTagIcon(holder.tagName, tag.name);
        holder.tagCount.setText(tag.count + " tasks");
        holder.itemView.setBackground(PriorityTagUtils.createTagRowDrawable(
                tag.name,
                tag.fixed,
                PriorityTagUtils.dp(holder.itemView, 8)
        ));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTagClick(tag);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (!tag.fixed && deleteListener != null) {
                deleteListener.onTagClick(tag);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return tags == null ? 0 : tags.size();
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        final TextView tagName;
        final TextView tagCount;

        TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.text_tag_name);
            tagCount = itemView.findViewById(R.id.text_tag_count);
        }
    }
}
