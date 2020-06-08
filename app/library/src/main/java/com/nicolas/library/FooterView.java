package com.nicolas.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class FooterView extends LinearLayout {
    private static final String TAG = "FooterView";
    private Context mContext;
    private CircleProgressBarView progressBar;
    private TextView loadMore;
    private TextView loading;
    private RelativeLayout mContainer;

    private int status;                            //状态
    private int pullUpTriggerHeight = 180;         //触发STATE_READY状态的高度

    public final static int LOAD_NORMAL = 0;       //一般状态
    public final static int LOAD_READY = 1;        //上拉中。。。
    public final static int LOAD_LOADING = 2;      //加载中

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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(this.mContainer, lp);       //添加LinearLayout
        setGravity(Gravity.BOTTOM);
        this.progressBar = this.mContainer.findViewById(R.id.progressBar);
        this.loadMore = this.mContainer.findViewById(R.id.loadMore);
        this.loading = this.mContainer.findViewById(R.id.loading);
    }

    public void setStatus(int status) {
        if (this.status == status) {
            return;
        }
        this.status = status;
        switch (status) {
            case LOAD_NORMAL:           //显示上拉加载更多
                if (this.progressBar.getVisibility() == VISIBLE) {
                    this.progressBar.setVisibility(INVISIBLE);
                    this.loading.setVisibility(INVISIBLE);
                    this.progressBar.stopAnimation();
                }
                if (this.loadMore.getVisibility() != VISIBLE) {
                    this.loadMore.setVisibility(VISIBLE);
                }
                break;
            case LOAD_READY:
                if (this.loadMore.getVisibility() == VISIBLE) {
                    this.loadMore.setVisibility(INVISIBLE);
                }
                if (this.progressBar.getVisibility() != VISIBLE) {
                    this.progressBar.setVisibility(VISIBLE);
                }
                if (this.loading.getVisibility() != GONE) {
                    this.loading.setVisibility(GONE);
                }
                break;
            case LOAD_LOADING:
                if (this.loading.getVisibility() != VISIBLE) {
                    this.loading.setVisibility(VISIBLE);
                }
                this.progressBar.startAutoPlayAnimation();        //一直转圈
                break;
        }
    }

    public int getStatus() {
        return status;
    }

    public void setPullUpTriggerHeight(int pullUpTriggerHeight) {
        this.pullUpTriggerHeight = pullUpTriggerHeight;
    }

    public void setVisibilityHeight(int height) {
        int progress = height * 100 / pullUpTriggerHeight;
        this.progressBar.setProgress(Math.min(progress, 100));
    }

    public int getVisibilityHeight() {
        return mContainer.getHeight();
    }

    public void setLoadFinishDescribe(String loadFinishDescribe) {
        this.loadMore.setText(loadFinishDescribe);
    }
}
