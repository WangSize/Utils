package com.norca.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 可以设置两个按钮提示语、可以选择是否显示按钮
 * 要有两个stlye配置文件，v21资源文件夹下的，有阴影，但是背景不变灰
 * 普通资源文件夹下的，没有阴影，背景变灰
 * Created by Administrator on 2017/12/21.
 */

public class CommonTipDialog extends Dialog {
    private Context mContext;

    private Button mSure;
    private Button mCancel;

    private RelativeLayout mBtnsRel;

    private TextView mTitleTv;
    private TextView mMsgTv;

    private String message,title;
    private String mLeftBtnName,mRightBtnName;

    private boolean isHideButtons; //无按钮提示dialog

    private CommonTipDialog.OnClickListener listener;

    public CommonTipDialog(Context context, String message, CommonTipDialog.OnClickListener listener) {
        super(context, R.style.dialog_base_style);
        mContext = context;
        this.message = message;
        this.listener = listener;
    }

    /**
     * 无按钮Dialog
     * @param context
     * @param message
     * @param isHideButtons
     */
    public CommonTipDialog(Context context, String message, boolean isHideButtons) {
        super(context, R.style.dialog_base_style);
        mContext = context;
        this.message = message;
       this.isHideButtons = isHideButtons;
    }

    public void setClickListener(OnClickListener listener){
        this.listener = listener;
    }

    public void setButtonName(String leftBtnName,String rightBtnName){
        mLeftBtnName = leftBtnName;
        mRightBtnName = rightBtnName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_common_tip);
        initView();
    }

    private void initView() {
        mSure = (Button) findViewById(R.id.btn_sure);
        mCancel = (Button) findViewById(R.id.btn_cancel);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mMsgTv = (TextView) findViewById(R.id.tv_msg);
        mBtnsRel = (RelativeLayout) findViewById(R.id.rel_btns);

        if (isHideButtons == true)
            mBtnsRel.setVisibility(View.GONE);

        if (message != null)
            mMsgTv.setText(message);

        if (title != null)
            mTitleTv.setText(title);


        mSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.sure();
                dismiss();
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.cancel();
                dismiss();
            }
        });
        if (mRightBtnName == null && mLeftBtnName == null)
            return;
        mSure.setText(mRightBtnName);
        mCancel.setText(mLeftBtnName);
    }

    public interface OnClickListener{
        void sure();
        void cancel();
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setMessage(String message){
        this.message = message;
    }

}
