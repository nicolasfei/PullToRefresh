package com.nicolas.library;

import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class CircleProgressBarView extends View {

    private static final String TAG = "CircleProgressBarView";

    private Context context;

    //attr
    private int progressColor;          //进度条颜色
    private int backgroundColor;        //背景颜色
    private float progressWidth;        //进度条环形的宽度
    private float startAngle;           //进度条的起始角度
    private float sweepAngle;           //进度条的扫描角度
    private float startAngleBack;       //备份
    private float sweepAngleBack;       //备份

    private int defaultSize;    //自定义View默认的宽高
    private RectF mRectF;       //绘制圆弧的矩形区域
    private Paint progressPaint;        //圆弧画笔
    private Paint backgroundPaint;      //背景画笔

    private int progress;               //进度条进度
    private int max;                    //最大进度
    private float progressSweepAngle;   //通过progress折算的圆弧扫描角度
    private int rectSize;               //画圆弧的正方形的尺寸
    private float pivotX;               //圆弧的圆点X值
    private float pivotY;               //圆弧的圆点Y值
    private float radius;               //圆弧的半径

    public CircleProgressBarView(Context context) {
        super(context, null);
        this.context = context;
        initialize(null);
    }

    public CircleProgressBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initialize(attrs);
    }

    //初始化view
    private void initialize(AttributeSet attrs) {
        //初始化view的默认宽高
        this.defaultSize = dp2px(100);      //默认100dp
        //初始化画圆弧的矩形
        this.mRectF = new RectF();
        //初始化属性
        TypedArray typedArray;
        if (attrs == null) {
            typedArray = this.context.obtainStyledAttributes(R.styleable.CircleProgressBarView);
        } else {
            typedArray = this.context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBarView);
        }
        this.progressColor = typedArray.getColor(R.styleable.CircleProgressBarView_progressColor, Color.BLUE);
        this.backgroundColor = typedArray.getColor(R.styleable.CircleProgressBarView_backgroundColor, Color.GRAY);
        this.progressWidth = typedArray.getDimension(R.styleable.CircleProgressBarView_progressWidth, dp2px(10));   //默认10dp
        this.startAngle = typedArray.getFloat(R.styleable.CircleProgressBarView_startAngle, 90);
        this.sweepAngle = typedArray.getFloat(R.styleable.CircleProgressBarView_sweepAngle, 360);
        this.progress = typedArray.getInteger(R.styleable.CircleProgressBarView_progress, 100);
        this.max = typedArray.getInteger(R.styleable.CircleProgressBarView_max, 100);        //最大进度默认为100
        typedArray.recycle();       //typedArray用完之后需要回收，防止内存泄漏
        this.startAngleBack = this.startAngle;
        this.sweepAngleBack = this.sweepAngle;
        //计算进度折算后的扫描角度
        progressConversionSweepAngle();
        //初始化画笔
        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE); //只描边，不填充
        progressPaint.setColor(this.progressColor);
        progressPaint.setAntiAlias(true);   //设置抗锯齿
        progressPaint.setStrokeWidth(this.progressWidth);   //画笔宽度

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(this.backgroundColor);
        backgroundPaint.setAntiAlias(true); //设置抗锯齿
        backgroundPaint.setStrokeWidth(15);
    }

    /**
     * 精确测量View的宽高
     *
     * @param widthMeasureSpec  widthMeasureSpec
     * @param heightMeasureSpec heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureSize(defaultSize, widthMeasureSpec);
        int height = measureSize(defaultSize, heightMeasureSpec);
        rectSize = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(rectSize, rectSize);// 强制改View为以最短边为长度的正方形

        //这里简单限制了圆弧的最大宽度
        if (rectSize >= progressWidth * 2) {
            mRectF.set(progressWidth * 2, progressWidth * 2, rectSize - progressWidth * 2, rectSize - progressWidth * 2);
            radius = ((float) rectSize - progressWidth * 4) / 2;
        } else {
            mRectF.set(progressWidth, progressWidth, rectSize - progressWidth, rectSize - progressWidth);
            radius = ((float) rectSize - progressWidth * 2) / 2;
        }
        pivotX = ((float) rectSize) / 2;
        pivotY = ((float) rectSize) / 2;
        arrowSizeMax = progressWidth / 2;
    }

    private float arrowSizeMax;     //箭头的最大宽度

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画view
        //画背景圆
        canvas.drawCircle(pivotX, pivotY, rectSize / 2, backgroundPaint);
        //画圆弧
        canvas.drawArc(mRectF, startAngle, progressSweepAngle, false, progressPaint);
        //画箭头
        canvas.rotate(progressSweepAngle + startAngle, pivotX, pivotY);     //将画布旋转，让圆弧的末端对准X轴正方向
        //计算箭头size
        float arrowSize = progressSweepAngle / startAngle * arrowSizeMax;      //实现箭头由小变大
        //计算箭头Path
        Path arrow = new Path();
        arrow.moveTo(pivotX + radius, pivotY);
        arrow.lineTo(pivotX + radius - arrowSize, pivotY);
        arrow.lineTo(pivotX + radius, pivotY + arrowSize);
        arrow.lineTo(pivotX + radius + arrowSize, pivotY);
        arrow.close();
        canvas.drawPath(arrow, progressPaint);
    }

    /**
     * 设置进度条进度
     *
     * @param progress 进度
     */
    public void setProgress(int progress) {
        this.progress = progress;
        progressConversionSweepAngle();
        this.invalidate();
    }

    /**
     * 设置最大进度
     *
     * @param max 最大进度
     */
    public void setMax(int max) {
        this.max = max;
        this.invalidate();
    }

    /**
     * 将进度折算为扫描角度
     */
    private void progressConversionSweepAngle() {
        this.progressSweepAngle = sweepAngle * progress / max;    //这里计算进度条的比例
    }

    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    public int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    //-------------------------------------------------------------------------------
    private AnimatorSet animSet;
    private TimeInterpolator interpolatorType = new AccelerateDecelerateInterpolator();

    /**
     * 动画--以固定的扫描角度，且通过切换其实角度来实现动画
     */
    public void startAutoPlayAnimation() {
        //控制开始角度
        float startAngle = 90;
        float endAngle = 180;
        this.progressSweepAngle = 270;      //设置扫描角度为240度
        ValueAnimator startAngleAnimator = ValueAnimator.ofFloat(startAngle, endAngle);
        startAngleAnimator.setRepeatCount(-1);       //无限重复播放
        startAngleAnimator.setRepeatMode(ValueAnimator.RESTART);
        startAngleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CircleProgressBarView.this.startAngle = (float) animation.getAnimatedValue();
                CircleProgressBarView.this.invalidate();
            }
        });
        //控制扫描角度
        float startSwipe = 0;
        float endSwipe = 270;
        ValueAnimator swipeAngleAnimator = ValueAnimator.ofFloat(startSwipe, endSwipe);
        swipeAngleAnimator.setRepeatCount(-1);       //无限重复播放
        swipeAngleAnimator.setRepeatMode(ValueAnimator.RESTART);
        swipeAngleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                CircleProgressBarView.this.progressSweepAngle = (float) animation.getAnimatedValue();
                CircleProgressBarView.this.invalidate();
            }
        });

        animSet = new AnimatorSet();
        animSet.play(startAngleAnimator).with(swipeAngleAnimator);
        animSet.setDuration(2000);
        animSet.setInterpolator(interpolatorType);
        animSet.start();
    }

    public void setInterpolatorType(int type) {
        switch (type) {
            case 1:
                interpolatorType = new BounceInterpolator();
                break;
            case 2:
                interpolatorType = new AccelerateDecelerateInterpolator();
                break;
            case 3:
                interpolatorType = new DecelerateInterpolator();
                break;
            case 4:
                interpolatorType = new AnticipateInterpolator();
                break;
            case 5:
                interpolatorType = new LinearInterpolator();
                break;
            case 6:
                interpolatorType = new LinearOutSlowInInterpolator();
                break;
            case 7:
                interpolatorType = new OvershootInterpolator();
            default:
                interpolatorType = new LinearInterpolator();
                break;
        }
    }

    /**
     * 暂停动画
     */
    public void pauseAnimation() {
        if (animSet != null) {
            animSet.pause();
        }
    }

    /**
     * 停止动画
     */
    public void stopAnimation() {
        if (animSet != null) {
            animSet.cancel();
            this.clearAnimation();
            this.startAngle = this.startAngleBack;
            this.sweepAngle = this.sweepAngleBack;
        }
    }
}