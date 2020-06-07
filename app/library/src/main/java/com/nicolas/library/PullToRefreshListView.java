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
    private float downY;        //按下时Y的坐标
    private float downX;        //按下时X的坐标
    private float moveY;        //移动时Y的坐标
    private float moveX;        //移动时X的坐标
    private int instanceY;
    private int instanceX;
    private int biggestPullDistance;    //最大下拉距离

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
        headerView.setPullTriggerHeight(2 * headerViewHeight);       //设置移动触发刷新距离
        headerView.setVisibilityHeight(0);
        addHeaderView(headerView);
        biggestPullDistance = 3 * headerViewHeight;

        //加载尾
        footerView = new FooterView(this.context);
        addFooterView(footerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:       //用户按下
                downY = ev.getY();      //记录按下时候的Y坐标
                downX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                moveY = ev.getY();
                moveX = ev.getX();
                instanceY = (int) (moveY - downY);
                instanceX = (int) (moveX - downX);
                //判断是否是下拉
                if (instanceY > 0 && Math.abs(instanceY) > Math.abs(instanceX) && headerView.getStatus() != HeaderView.STATE_REFRESHING) {
                    headerView.setStatus(HeaderView.STATE_READY);
                    if (instanceY > biggestPullDistance) {      //最大下拉距离
                        headerView.setPadding(0, 3 * headerViewHeight, 0, 0);
                        return true;
                    } else {
                        headerView.setPadding(0, instanceY, 0, 0);
                    }
                    headerView.setVisibilityHeight(instanceY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (instanceY > headerViewHeight) {
                    if (headerView.getStatus() != HeaderView.STATE_REFRESHING) {
                        headerView.setStatus(HeaderView.STATE_REFRESHING);
                        if (this.onRefreshListener != null) {
                            this.onRefreshListener.onRefresh();
                        }
                    }
                } else {
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
