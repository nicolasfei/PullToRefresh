package com.nicolas.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

public class HeaderView extends LinearLayout {
    private static final String TAG = "HeaderView";
    private Context mContext;
    private int status;                 //状态
    private LinearLayout mContainer;
    private ProgressBar progressBar;

    private int pullTriggerHeight = 180;   //触发STATE_READY状态的高度

    public final static int STATE_NORMAL = 0;       //一般状态
    public final static int STATE_READY = 1;        //准备状态（下拉到了可以刷新的状态）
    public final static int STATE_REFRESHING = 2;   //刷新中


    public HeaderView(Context context) {
        super(context);
        initView(context);
    }

    public HeaderView(Context context, int pullTriggerHeight) {
        super(context);
        this.pullTriggerHeight = pullTriggerHeight;
        initView(context);
    }

    public HeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
        //初始化LinearLayout
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
        this.mContainer = (LinearLayout) LayoutInflater.from(this.mContext).inflate(R.layout.header_layout, this, false);
        addView(this.mContainer, lp);       //添加LinearLayout
        setGravity(Gravity.BOTTOM);
        //初始化progressBar
        this.progressBar = this.mContainer.findViewById(R.id.progressBar);
    }

    public void setStatus(int status) {
        if (this.status == status) {
            return;
        }

        switch (status) {
            case STATE_NORMAL:
                this.progressBar.setIndeterminate(false);
                break;
            case STATE_READY:
                this.progressBar.setProgress(100);
                break;
            case STATE_REFRESHING:
                this.progressBar.setIndeterminate(true);        //一直转圈
                break;
        }
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
}
