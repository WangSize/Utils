import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/12/22.
 */

public class CustomLoadingDialog extends ProgressDialog {
    private String title, info;
    private TextView mTitleTv, mInfoTv;

    public CustomLoadingDialog(Context context, String title) {
        super(context, R.style.dialog_base_style);
        this.title = title;
    }

    public void setInfo(String info){
        this.info = info;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom_loading_dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mInfoTv = (TextView) findViewById(R.id.tv_info);
        if (title == null)
            mTitleTv.setVisibility(View.GONE);
        else
            mTitleTv.setText(title);

        if (info == null)
            mInfoTv.setText("加载中···");
        else
            mInfoTv.setText(info);

    }


}