package com.nicolas.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class FooterView extends LinearLayout {

    private Context mContext;

    public FooterView(Context context) {
        super(context);
        initView(context);
    }

    public FooterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
    }
}
