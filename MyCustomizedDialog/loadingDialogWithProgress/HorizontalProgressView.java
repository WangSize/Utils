import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2017/12/25.
 */

public class HorizontalProgressView extends View {

    private float strokeWidth;
    private int backgroundColor;
    private int progressColor;
    private int currentProgress;

    private Paint mPaint;

    public HorizontalProgressView(Context context) {
        this(context, null);
    }

    public HorizontalProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalProgressView, defStyleAttr, 0);
        strokeWidth = typedArray.getDimension(R.styleable.HorizontalProgressView_strokeWidth,5.0f);
        backgroundColor = typedArray.getColor(R.styleable.HorizontalProgressView_backgroundColor, Color.parseColor("#D3D3D3"));
        progressColor = typedArray.getColor(R.styleable.HorizontalProgressView_progressColor, Color.parseColor("#FF0000"));

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        if (heightMode == MeasureSpec.AT_MOST)
            setMeasuredDimension(width, (int)strokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(backgroundColor);
        canvas.drawRect(0, 0, (float)getWidth(),strokeWidth, mPaint);
        float currentProgressLength = (currentProgress*getWidth())/100;
        mPaint.setColor(progressColor);
        canvas.drawRect(0, 0, currentProgressLength,strokeWidth, mPaint);
    }

    /**
     * 设置进度
     */
    public void setCurrentProgress(int currentProgress){
        if (currentProgress < 0 || currentProgress >100)
            this.currentProgress = 0;
        else
            this.currentProgress = currentProgress;
        invalidate();
    }

    public int getCurrentProgress(){
        return currentProgress;
    }
}
