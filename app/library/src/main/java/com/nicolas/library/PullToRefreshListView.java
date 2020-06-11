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
    private int headerViewReadyHeight;
    private int headerViewTriggerHeight;
    private int footerViewHeight;
    private int footerViewReadyHeight;
    private int footerViewTriggerHeight;
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
        headerView.setPadding(0, -headerViewHeight, 0, 0);      //设置边距为-headerViewHeight，则会隐藏，通过设置headerView的padding来实现下拉过程
        headerViewReadyHeight = 1 * headerViewHeight;
        headerViewTriggerHeight = 5 * headerViewHeight;
        headerView.setPullTriggerHeight(headerViewTriggerHeight - headerViewReadyHeight);       //设置移动触发刷新距离
        headerView.setVisibilityHeight(0);
        addHeaderView(headerView);
        biggestPullDistance = 2 * headerViewHeight;

        //加载尾
        footerView = new FooterView(this.context);
        footerView.measure(0, 0);
        footerViewHeight = footerView.getMeasuredHeight();
        footerViewReadyHeight = 1 * footerViewHeight;
        footerViewTriggerHeight = 5 * footerViewHeight;
        footerView.setPullUpTriggerHeight(footerViewTriggerHeight - footerViewReadyHeight);       //设置移动触发刷新距离
        addFooterView(footerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:       //用户按下
                if (getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1) {
                    downY = ev.getY();      //记录按下时候的Y坐标
                    downX = ev.getX();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //判断list view是否滑倒顶端了
                if (getFirstVisiblePosition() == 0) {
                    moveY = ev.getY();
                    moveX = ev.getX();
                    instanceY = (int) (moveY - downY);
                    instanceX = (int) (moveX - downX);
                    //判断是否是下拉行为
                    if (instanceY > 0 && instanceY > Math.abs(instanceX)) {
                        //判断头和尾View的状态---判断footerView的状态，是为了防止用户按下后上下来回滑动造成的误判
                        if (headerView.getStatus() != HeaderView.STATE_REFRESHING && footerView.getStatus() == FooterView.LOAD_NORMAL) {
                            if (instanceY > headerViewReadyHeight) {
                                headerView.setStatus(HeaderView.STATE_READY);
                                headerView.setPadding(0, Math.min(instanceY - headerViewReadyHeight, biggestPullDistance), 0, 0);
                                headerView.setVisibilityHeight(instanceY - headerViewReadyHeight);
                            }
                        }
                    }
                }
                //判断listView是否滑倒了底部
                if (getLastVisiblePosition() == getCount() - 1) {
                    moveY = ev.getY();
                    moveX = ev.getX();
                    instanceY = (int) (moveY - downY);
                    instanceX = (int) (moveX - downX);
                    //判断是否是上拉行为
                    if (instanceY < 0 && -instanceY > Math.abs(instanceX)) {
                        //判断头和尾View的状态---判断headerView的状态，是为了防止用户按下后上下来回滑动造成的误判
                        if (footerView.getStatus() != FooterView.LOAD_LOADING && headerView.getStatus() == HeaderView.STATE_NORMAL) {
                            if (-instanceY > footerViewReadyHeight) {
                                footerView.setStatus(FooterView.LOAD_READY);
                                footerView.setVisibilityHeight(Math.abs(instanceY) - footerViewReadyHeight);
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //判断是否是垂直动作(下拉/上拉)
//                Log.d(TAG, "onTouchEvent: instanceY is " + instanceY + " instanceX is " + instanceX);
//                Log.d(TAG, "onTouchEvent: headerViewTriggerHeight is " + headerViewTriggerHeight + " footerViewTriggerHeight is " + footerViewTriggerHeight);
//                Log.d(TAG, "onTouchEvent: headerView Status is " + headerView.getStatus() + " footerView status is " + footerView.getStatus());
                //判断list view是否滑倒顶端了
                if (getFirstVisiblePosition() == 0) {
                    //判断头状态
                    if (headerView.getStatus() == HeaderView.STATE_READY) {
                        //判断下拉距离是否大于触发距离
                        if (instanceY >= headerViewTriggerHeight) {
                            //触发刷新
                            headerView.setStatus(HeaderView.STATE_REFRESHING);
                            if (this.onRefreshListener != null) {
                                this.onRefreshListener.onRefresh();
                            }
                        } else {
                            //还原
                            this.headerView.setPadding(0, -headerViewHeight, 0, 0);      //设置边距为-headerViewHeight，则会隐藏，通过设置headerView的padding来实现下拉过程
                            this.headerView.setStatus(HeaderView.STATE_NORMAL);
                        }
                    }
                }else {
                    if (headerView.getStatus()==HeaderView.STATE_READY){
                        Log.d(TAG, "onTouchEvent: HeaderView.STATE_READY");
                        headerView.setStatus(HeaderView.STATE_NORMAL);
                        headerView.setPadding(0,-headerViewHeight,0,0);
                    }
                }

                //判断listView是否滑倒了底部
                if (getLastVisiblePosition() == getCount() - 1) {
                    //判断footer状态
                    if (footerView.getStatus() == FooterView.LOAD_READY) {
                        //判断上拉距离是否大于触发距离
                        if (-instanceY >= footerViewTriggerHeight) {
                            //触发加载
                            footerView.setStatus(FooterView.LOAD_LOADING);
                            if (this.onLoadingMoreListener != null) {
                                this.onLoadingMoreListener.onLoadingMore();
                            }
                        } else {
                            //还原
                            this.footerView.setStatus(FooterView.LOAD_NORMAL);
                        }
                    }

                }else {
                    if (footerView.getStatus()==FooterView.LOAD_READY){
                        Log.d(TAG, "onTouchEvent: FooterView.LOAD_READY");
                        footerView.setStatus(FooterView.LOAD_NORMAL);
                    }
                }
                downY = 0;
                downX = 0;
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

    public void loadMoreFinish(String loadFinishDescribe) {
        this.footerView.setStatus(FooterView.LOAD_NORMAL);
        this.footerView.setLoadFinishDescribe(loadFinishDescribe);
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
