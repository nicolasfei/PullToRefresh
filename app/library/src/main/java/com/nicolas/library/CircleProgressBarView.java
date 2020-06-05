package com.nicolas.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

public class CircleProgressBarView extends View {

    private Context context;

    //attr
    private int progressColor;          //进度条颜色
    private int backgroundColor;        //背景颜色
    private float progressWidth;        //进度条环形的宽度
    private float startAngle;           //进度条的起始角度
    private float sweepAngle;           //进度条的扫描角度

    private int defaultSize;    //自定义View默认的宽高
    private RectF mRectF;       //绘制圆弧的矩形区域
    private Paint progressPaint;        //圆弧画笔
    private Paint backgroundPaint;      //背景画笔

    private int progress;               //进度条进度
    private int max;                    //最大进度
    private float progressSweepAngle;   //通过progress折算的圆弧扫描角度

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
        this.backgroundColor = typedArray.getColor(R.styleable.CircleProgressBarView_backgroundColor, Color.TRANSPARENT);
        this.progressWidth = typedArray.getDimension(R.styleable.CircleProgressBarView_progressWidth, dp2px(10));   //默认10dp
        this.startAngle = typedArray.getFloat(R.styleable.CircleProgressBarView_startAngle, 90);
        this.sweepAngle = typedArray.getFloat(R.styleable.CircleProgressBarView_sweepAngle, 360);
        this.progress = typedArray.getInteger(R.styleable.CircleProgressBarView_progress, 100);
        this.max = typedArray.getInteger(R.styleable.CircleProgressBarView_max, 100);        //最大进度默认为100
        typedArray.recycle();       //typedArray用完之后需要回收，防止内存泄漏
        //计算进度折算后的扫描角度
        progressConversionSweepAngle();
        //初始化画笔
        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE); //只描边，不填充
        progressPaint.setColor(this.progressColor);
        progressPaint.setAntiAlias(true);   //设置抗锯齿
        progressPaint.setStrokeWidth(this.progressWidth);   //画笔宽度

        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.STROKE);   //只描边，不填充
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
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形

        //这里简单限制了圆弧的最大宽度
        if (min >= progressWidth * 2) {
            mRectF.set(progressWidth / 2, progressWidth / 2, min - progressWidth / 2, min - progressWidth / 2);
        } else {
            mRectF.set(0, 0, min, min);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画view
        //画圆弧框
        canvas.drawArc(mRectF, startAngle, sweepAngle, false, backgroundPaint);
        //画圆弧框
        canvas.drawArc(mRectF, startAngle, progressSweepAngle, false, progressPaint);
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
}