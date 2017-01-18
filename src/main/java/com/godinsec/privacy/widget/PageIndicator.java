package com.godinsec.privacy.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import com.godinsec.privacy.utils.ColorHelper;
import com.godinsec.privacy.utils.DisplayUtil;
import java.util.ArrayList;


public class PageIndicator extends View {

    private static final String TAG = "PageIndicator";

    private Paint circlePaint,textPaint;

    private Path path;

    private int circleCount;//指示球个数

    private static final int RADIUS_DEFAULT = 4;//小球半径

    private static final int DEVIDER = 6;//小球间距

    private int circleActivedIndex = -1;//当前选中的小球

    private static final float SCALE_DEFAULT = 1.4f;//放大倍数

    private static final float TEXTSIZEZ_DEFAULT = 8;//默认字体的大小

    private int radius;//小球实际半径

    private float scale;//选中小球放大倍数

    private int devider;//小球实际间距

    private float textSize;//字体大小

    private ArrayList<Circle> circlePaths = new ArrayList<Circle>();

    private static final int [] DEFAULT_COLORS = {0xFF555555,0xFFCCCCCC};

    public static class Style{
        public static final int NORMAL = 0x01;//正常效果
        public static final int SHADE = 0x02;//颜色渐变效果
        public static final int SCALE = 0x03;//大小缩放效果
        public static final int ADJOIN = 0x04;//滑动粘连效果
    }


    private class Circle {
        float centerX;
        float centerY;
        float radius;
        int color;
        int index;
        float textSizeScale;
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setColor(DEFAULT_COLORS[0]);

        path = new Path();

        radius = DisplayUtil.dip2px(getContext(), RADIUS_DEFAULT);
        scale = SCALE_DEFAULT;
        devider = DisplayUtil.dip2px(getContext(), DEVIDER);

        circleCount = 0;

        circleActivedIndex = 0;

        textSize = TEXTSIZEZ_DEFAULT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureSize();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(radius, 0);
//		canvas.drawPath(path,textPaint);
        for(int i = 0;i < circleCount;i++){
            drawCicle(canvas,circlePaths.get(i));
        }
    }

    /**
     * 计算大小
     */
    private void measureSize(){
        int height = (int) ((radius * scale * 2 * 1.5f)*1.5f);
        int width =  (circleCount -1) * (devider +radius * 2) + height;
        setMeasuredDimension(width, height);
    }

    /**
     * 开始绘制小球
     * @param canvas
     * @param circle
     */
    private void drawCicle(Canvas canvas,Circle circle){
        circlePaint.setColor(circle.color);
        canvas.drawCircle(circle.centerX, circle.centerY, circle.radius, circlePaint);
        drawDigit(canvas, circle);
    }

    /**
     * 绘制数字提示
     * @param canvas
     * @param circle
     */
    private void drawDigit(Canvas canvas,Circle circle){
        textPaint.setTextSize(DisplayUtil.sp2px(getContext(), textSize) *
                (circle.textSizeScale <= 0.1f ? 0f : circle.textSizeScale));
//		FontMetrics fm = textPaint.getFontMetrics();
//		double textHeight = Math.ceil(fm.descent - fm.ascent);
//		double textWidth = textPaint.measureText(""+circle.index);
//		canvas.drawText(circle.index+"", (float) (circle.centerX - textWidth / 2f),
//				(float) (circle.centerY+textHeight / 3.5f), textPaint);
    }

    /**
     * 设置小球显示的个数
     * @param count
     */
    public void setCount(int count) {
        this.circleCount = count;
        circlePaths.clear();
        for(int i = 0;i < this.circleCount;i++){
            Circle circle = new Circle();
            circle.index = i+1;
            circle.centerY = radius*scale;
            if(i == circleActivedIndex){
                circle.color = ColorHelper.evaluate(0.1f, DEFAULT_COLORS[1], DEFAULT_COLORS[0]);
                circle.radius = radius*scale;
                circle.textSizeScale = 1f;
            }else{
                circle.color = ColorHelper.evaluate(0.1f, DEFAULT_COLORS[0], DEFAULT_COLORS[1]);
                circle.radius = radius;
                circle.textSizeScale = 0;
            }
            if(i <= circleActivedIndex){
                circle.centerX = (radius * 2 + devider) * i +circle.radius;
            }else if(i > circleActivedIndex){
                circle.centerX = (radius * 2 + devider) * (i-1)//前面正常小球的宽度
                        + radius * scale * 2 + devider//前面放大小球
                        +circle.radius;
            }
            circlePaths.add(circle);
        }
        measureSize();
        invalidate();
    }

    /**
     * 设置活动的小球
     * @param index
     */
    public void setCircleActivedIndex(int index){
        if(index >= circlePaths.size()){
            index = circlePaths.size() -1;
        }
        Circle lastActivedCircele = circlePaths.get(circleActivedIndex);
        lastActivedCircele.color = ColorHelper.evaluate(0.1f, DEFAULT_COLORS[0], DEFAULT_COLORS[1]);
        lastActivedCircele.radius = radius;
        lastActivedCircele.textSizeScale = 0;
        this.circleActivedIndex = index;
        Circle currentActivedCircele = circlePaths.get(circleActivedIndex);
        currentActivedCircele.color = ColorHelper.evaluate(0.1f, DEFAULT_COLORS[1], DEFAULT_COLORS[0]);
        currentActivedCircele.radius = radius*scale;
        currentActivedCircele.textSizeScale = 1f;
        invalidate();
    }

    /**
     * 设置页面滑动的百分比，以此来实施计算相关小球的大小
     * @param leftPosition
     * @param percent
     */
    public void setScrollProgress(int leftPosition,float percent){
        if(leftPosition >= 0 && leftPosition < circleCount){
            Circle left = circlePaths.get(leftPosition);
            left.radius = radius*(1+ (scale -1f) *(1 - percent));
            left.textSizeScale = 1-percent;
            if(left.color == DEFAULT_COLORS[0]){
                left.color = ColorHelper.evaluate(percent, DEFAULT_COLORS[0], DEFAULT_COLORS[1]);
            }else{
                left.color = ColorHelper.evaluate(percent, DEFAULT_COLORS[1], DEFAULT_COLORS[0]);
            }
        }

        if(leftPosition +1 < circleCount){
            Circle right = circlePaths.get(leftPosition+1);
            right.radius = radius*(1+(scale - 1f)*percent);
            right.textSizeScale = percent;
            if(right.color == DEFAULT_COLORS[0]){
                right.color = ColorHelper.evaluate(1-percent, DEFAULT_COLORS[0], DEFAULT_COLORS[1]);
            }else{
                right.color = ColorHelper.evaluate(1-percent, DEFAULT_COLORS[1], DEFAULT_COLORS[0]);
            }
        }

        invalidate();
    }
}

