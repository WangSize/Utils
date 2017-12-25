import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/12/23.
 * 可设置进度条宽度：strokeWidth，和进度条底层颜色及表层颜色
 */

public class LoadingWithProgressDialog extends Dialog {

    private String title;
    private int mCurrentProgress;
    private HorizontalProgressView mProgressBar;
    private TextView mTitleTv;
    private TextView mPercentLeftTv,mPercentRightTv;

    public LoadingWithProgressDialog(Context context, String title) {
        super(context, R.style.dialog_base_style);
        this.title = title;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading_with_progress);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mProgressBar = (HorizontalProgressView) findViewById(R.id.progress_bar);
        mProgressBar.setCurrentProgress(50);
        mPercentLeftTv = (TextView) findViewById(R.id.tv_percent_left);
        mPercentRightTv = (TextView) findViewById(R.id.tv_percent_right);
        setProgressInfo();
        if (title == null)
            mTitleTv.setVisibility(View.GONE);
        else
            mTitleTv.setText(title);

    }


    //以下获取和设置进度只能在show()后生效
    public void setCurrentProgress(int progress){
        if (mProgressBar != null){
            mProgressBar.setCurrentProgress(progress);
            mCurrentProgress = getCurrentProgress();
            setProgressInfo();
        }

    }

    public int getCurrentProgress(){
        return mProgressBar != null ? mCurrentProgress = mProgressBar.getCurrentProgress() : 0;
    }

    private void setProgressInfo(){
        String str = mCurrentProgress+"%";
        mPercentLeftTv.setText(str);
        String str1 = mCurrentProgress+"/100";
        mPercentRightTv.setText(str1);
    }

}
