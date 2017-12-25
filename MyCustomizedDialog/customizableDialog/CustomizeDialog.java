import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/12/23.
 */

public class CustomizeDialog extends Dialog implements View.OnClickListener{


    private Context mContext;

    private Button mPositiveButton;
    private Button mNeutralButton;
    private Button mNegativeButton;
    private FrameLayout mFrameLayout;

    private View cumstomizeView;

    private TextView mTitleTv;

    private String title;
    private String mPosiBtnName, mNeutBtnName, mNegaBtnName; //这是三个按钮分别是从右到坐
    private static final int POSTITIVE_BUTTON = 0;
    private static final int NEUTRAL_BUTTON = 1;
    private static final int NEGATIVE_BUTTON = 2;
    private OnClickListener mPosiBtnListener,mNegaBtnListener,mNeutBtnListener;

    public CustomizeDialog(Context context, String title) {
        super(context, R.style.dialog_base_style);
        mContext = context;
        this.title = title;

    }

    public void setPositiveButton(String name, OnClickListener listener){
        mPosiBtnName = name;
        mPosiBtnListener = listener;
    }

    public void setNegativeButton(String name, OnClickListener listener){
        mNegaBtnName = name;
        mNegaBtnListener = listener;
    }

    public void setNeutralButton(String name, OnClickListener listener){
        mNeutBtnName = name;
        mNeutBtnListener = listener;
    }

    public void setView(View view){
        cumstomizeView = view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_customize);
        initView();
    }

    private void initView() {
        mPositiveButton = (Button) findViewById(R.id.btn_positive);
        mPositiveButton.setOnClickListener(this);
        mNegativeButton = (Button) findViewById(R.id.btn_negative);
        mNegativeButton.setOnClickListener(this);
        mNeutralButton = (Button) findViewById(R.id.btn_neutral);
        mNeutralButton.setOnClickListener(this);

        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mFrameLayout = (FrameLayout) findViewById(R.id.framelayout);

        if (title != null)
            mTitleTv.setText(title);

        if (cumstomizeView != null){
            mFrameLayout.addView(cumstomizeView);
        }

        if (mPosiBtnListener == null || mPosiBtnName == null){
            mPositiveButton.setVisibility(View.GONE);
        }else {
            mPositiveButton.setText(mPosiBtnName);
        }
        if (mNeutBtnListener == null || mNeutBtnName == null){
            mNeutralButton.setVisibility(View.GONE);
        }else {
            mNeutralButton.setText(mNeutBtnName);
        }
        if (mNegaBtnListener == null || mNegaBtnName == null){
            mNegativeButton.setVisibility(View.GONE);
        }else {
            mNegativeButton.setText(mNegaBtnName);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_positive:
                mPosiBtnListener.onClick(this, POSTITIVE_BUTTON);
                break;
            case R.id.btn_neutral:
                mNeutBtnListener.onClick(this, NEUTRAL_BUTTON);
                break;
            case R.id.btn_negative:
                mNegaBtnListener.onClick(this, NEGATIVE_BUTTON);
        }
    }


    public void setTitle(String title){
        this.title = title;
    }


}
