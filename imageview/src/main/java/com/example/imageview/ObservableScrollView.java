package com.example.imageview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

public class ObservableScrollView extends ScrollView {

    public interface OnScrollChangedListener {
        void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }

    private OnScrollChangedListener listener;

    public ObservableScrollView(Context context) {
        super(context);
    }

    public ObservableScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
        if (listener != null) {
            listener.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
        }
    }
}
