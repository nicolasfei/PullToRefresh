package com.nicolas.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class HeaderView extends LinearLayout {
    private static final String TAG = "HeaderView";
    private Context mContext;
    private int status;                 //状态
    private LinearLayout mContainer;
    private CircleProgressBarView progressBar;
    private TextView refresh;

    private int pullTriggerHeight = 300;   //触发STATE_READY状态的高度

    public final static int STATE_NORMAL = 0;       //一般状态
    public final static int STATE_READY = 1;        //下拉中。。。//准备状态（下拉到了可以刷新的状态）
    public final static int STATE_REFRESHING = 2;   //刷新中


    public HeaderView(Context context) {
        super(context);
        initView(context);
    }

    public HeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
        //初始化LinearLayout
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        this.mContainer = (LinearLayout) LayoutInflater.from(this.mContext).inflate(R.layout.header_layout, this, false);
        addView(this.mContainer, lp);       //添加LinearLayout
        setGravity(Gravity.BOTTOM);
        //初始化progressBar
        this.progressBar = this.mContainer.findViewById(R.id.progressBar);
        this.refresh = this.mContainer.findViewById(R.id.refresh);
    }

    public void setPullTriggerHeight(int pullTriggerHeight) {
        this.pullTriggerHeight = pullTriggerHeight;
    }

    public void setStatus(int status) {
        if (this.status == status) {
            return;
        }
        this.status = status;
        switch (status) {
            case STATE_NORMAL:
                this.progressBar.stopAnimation();
                if (this.refresh.getVisibility() != GONE) {
                    this.refresh.setVisibility(GONE);
                }
                this.progressBar.setProgress(0);
                break;
            case STATE_READY:
//                this.progressBar.setProgress(100);
                break;
            case STATE_REFRESHING:
                this.progressBar.startAutoPlayAnimation();
                if (this.refresh.getVisibility() != VISIBLE) {
                    this.refresh.setVisibility(VISIBLE);
                }
                break;
        }
    }

    public int getStatus() {
        return status;
    }

    public void setVisibilityHeight(int height) {
        int progress = height * 100 / pullTriggerHeight;
        this.progressBar.setProgress(Math.min(progress, 100));
    }

    public int getVisibilityHeight() {
        return mContainer.getHeight();
    }
}
