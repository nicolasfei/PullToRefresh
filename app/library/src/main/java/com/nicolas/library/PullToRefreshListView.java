package com.nicolas.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;

public class PullToRefreshListView extends ListView implements AbsListView.OnScrollListener {
    private static final String TAG = "PullToRefreshListView";

    private HeaderView headerView;
    private FooterView footerView;

    private Context context;

    private int headerViewHeight;
    private float downY;        //按下时候Y的坐标
    private float moveY;        //移动时Y的坐标
    private final int MOV_REFRESH_DIS = 200;

    private OnRefreshListener onRefreshListener;
    private OnLoadingMoreListener onLoadingMoreListener;

    public PullToRefreshListView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        //加载头
        headerView = new HeaderView(this.context);
        headerView.measure(0, 0);        //测量尺寸
        headerViewHeight = headerView.getMeasuredHeight();
        Log.d(TAG, "initView: headerViewHeight is " + headerViewHeight);
        headerView.setPadding(0, -headerViewHeight, 0, 0);      //设置边距为-headerViewHeight，则会隐藏，通过设置headerView的padding来实现下拉过程
        headerView.setPullTriggerHeight(headerViewHeight);       //设置移动触发刷新距离
        headerView.setVisibilityHeight(0);
        addHeaderView(headerView);

        //加载尾
        footerView = new FooterView(this.context);
        addFooterView(footerView);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:       //用户按下
                downY = ev.getY();      //记录按下时候的Y坐标
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "onTouchEvent: ------------" + downY+"   "+moveY+"   "+headerViewHeight);
                moveY = ev.getY();
                int instance = (int) (moveY - downY);
                if (instance > 0) {
                    headerView.setStatus(HeaderView.STATE_READY);
                    if (instance > 3 * headerViewHeight) {      //最大下拉距离
                        headerView.setPadding(0, 3 * headerViewHeight, 0, 0);
                        return true;
                    } else {
                        headerView.setPadding(0, instance, 0, 0);
                    }
                    headerView.setVisibilityHeight(instance);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: downY "+downY+"---moveY "+moveY+"---headerViewHeight "+headerViewHeight);
                if (moveY - downY > headerViewHeight) {
                    headerView.setStatus(HeaderView.STATE_REFRESHING);
                    if (this.onRefreshListener != null) {
                        this.onRefreshListener.onRefresh();
                    }
                }else {
                    this.headerView.setPadding(0, -headerViewHeight, 0, 0);      //设置边距为-headerViewHeight，则会隐藏，通过设置headerView的padding来实现下拉过程
                }
                break;
            default:
                break;

        }
        return super.onTouchEvent(ev);      //交由父类来处理
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public void refreshFinish() {
        this.headerView.setStatus(HeaderView.STATE_NORMAL);
        this.headerView.setPadding(0, -headerViewHeight, 0, 0);      //设置边距为-headerViewHeight，则会隐藏，通过设置headerView的padding来实现下拉过程
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.onRefreshListener = listener;
    }

    public void setOnLoadingMoreListener(OnLoadingMoreListener listener) {
        this.onLoadingMoreListener = listener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface OnLoadingMoreListener {
        void onLoadingMore();
    }
}
