package com.nicolas.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class FooterView extends LinearLayout {
    private static final String TAG = "FooterView";
    private Context mContext;
    private ProgressBar progressBar;
    private TextView textView;
    private RelativeLayout mContainer;

    private int status;                     //状态
    private int pullTriggerHeight = 180;   //触发STATE_READY状态的高度

    public final static int STATE_NORMAL = 0;       //一般状态
    public final static int STATE_READY = 1;        //上拉中。。。
    public final static int STATE_REFRESHING = 2;   //刷新中

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
        this.mContainer = (RelativeLayout) LayoutInflater.from(this.mContext).inflate(R.layout.footer_layout, null, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
        addView(this.mContainer, lp);       //添加LinearLayout
        setGravity(Gravity.BOTTOM);
        this.progressBar = this.mContainer.findViewById(R.id.progressBar2);
        this.textView = this.mContainer.findViewById(R.id.textView);
    }

    public void setStatus(int status) {
        if (this.status == status) {
            return;
        }
        this.status = status;
        switch (status) {
            case STATE_NORMAL:
                if (this.progressBar.getVisibility() == VISIBLE) {
                    this.progressBar.setVisibility(GONE);
                }
                this.progressBar.setIndeterminate(false);
                break;
            case STATE_READY:
                if (this.textView.getVisibility() == VISIBLE) {
                    this.textView.setVisibility(GONE);
                }
                break;
            case STATE_REFRESHING:
                this.progressBar.setIndeterminate(true);        //一直转圈
                break;
        }
    }

    public int getStatus() {
        return status;
    }

    public void setVisibilityHeight(int height) {
        if (height < 0)
            height = 0;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
        lp.height = height;
        this.mContainer.setLayoutParams(lp);
        int progress = height * 100 / pullTriggerHeight;
        Log.d(TAG, "setVisibilityHeight: progress is " + progress + " height is " + height + " pullTriggerHeight is " + pullTriggerHeight);
        this.progressBar.setProgress(Math.min(progress, 100));
    }

    public int getVisibilityHeight() {
        return mContainer.getHeight();
    }

    public void setText(String text) {
        this.textView.setText(text);
    }
}
