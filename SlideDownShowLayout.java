
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


/**
 * Created by wsz on 2018/1/11.
 */

public class SlideDownShowLayout extends LinearLayout {

    private boolean isShow;
    private int contentHeight;
    private float originY; //手指按下的位置
    private boolean isInit = true;
    private float lastDeltaY; //记录上一个滑动距离，用于处理上下滑动超过contentView大小的情况

    public SlideDownShowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public SlideDownShowLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //因为展示隐藏contentView时，整个Layout的大小都会变化，会调用onMeasure，所以用isInit标志
        //判断是否是未开始滑动，以获取contentView的正确原始高度
        if (isInit) {
            View contentView = getChildAt(0);
            MarginLayoutParams params =(MarginLayoutParams) contentView.getLayoutParams();
            //保存contentView原始高度
            contentHeight = contentView.getMeasuredHeight() + params.bottomMargin + params.topMargin;
            showContentView(0);
            isInit = false;
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isShow) {
            float y = event.getY();
            float deltaY = y - originY;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastDeltaY = 0;
                    originY = y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (deltaY > 0)
                        break;
                    if (deltaY < -contentHeight) {
                        //当本次滑动距离大于contentview高度，而上一次滑小于时，通过动画将contentview完全隐藏
                        //下拉时也使用这种处理方式
                        if (lastDeltaY >= -contentHeight){
                            createAnimator(contentHeight + (int) lastDeltaY, 0).start();
                            lastDeltaY = deltaY;
                            break;
                        }
                        break;
                    }
                    showContentView(contentHeight + (int) deltaY);
                    lastDeltaY = deltaY;
                    break;
                case MotionEvent.ACTION_UP:
                    if (deltaY > 0 || deltaY < -contentHeight)
                        break;
                    if (deltaY < -contentHeight / 3)
                        createAnimator(contentHeight + (int) deltaY, 0).start();
                    else
                        createAnimator(contentHeight + (int) deltaY, contentHeight).start();
                    originY = 0;
                    break;
            }
            return super.onTouchEvent(event);

        } else {
            float y = event.getY();
            float deltaY = y - originY;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    originY = y;
                    lastDeltaY = 0;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (deltaY < 0)
                        break;
                    if (deltaY > contentHeight) {
                        if (lastDeltaY <= contentHeight){
                            createAnimator((int) lastDeltaY, contentHeight).start();
                            lastDeltaY = deltaY;
                            break;
                        }
                        break;
                    }
                    showContentView((int) deltaY);
                    lastDeltaY = deltaY;
                    break;
                case MotionEvent.ACTION_UP:
                    if (deltaY < 0 || deltaY > contentHeight)
                        break;
                    if (deltaY > contentHeight / 3)
                        createAnimator((int) deltaY, contentHeight).start();
                    else
                        createAnimator((int) deltaY, 0).start();
                    originY = 0;
                    break;
            }
            return super.onTouchEvent(event);
        }

    }

    private void showContentView(int height) {
        if (height >= contentHeight) {
            isShow = true;
        }
        if (height <= 0) {
            isShow = false;
        }

        View contentView = getChildAt(0);
        ViewGroup.LayoutParams params = contentView.getLayoutParams();
        params.height = height;
        contentView.setLayoutParams(params);
        invalidate();
    }

    private ValueAnimator createAnimator(int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                showContentView(value);
            }
        });
        animator.setDuration(300);
        return animator;
    }
}
