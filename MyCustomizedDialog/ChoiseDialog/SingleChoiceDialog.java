
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2017/12/21.
 */

public class SingleChoiceDialog extends Dialog {
    private Context mContext;

    private Button mCommitBtn;

    private ListView mListView;
    private List<String> mItems;
    private TextView mTitleTv;
    private SingleChoiceDialogAdapter mAdapter;

    private String title;
    private String mBtnName;
    private int selectedPosition;

    private OnClickListener listener;

    public SingleChoiceDialog(Context context, String message, List<String> items, OnClickListener listener) {
        super(context, R.style.dialog_base_style);
        mContext = context;
        this.title = message;
        this.listener = listener;
        mItems = items;
    }

    /**
     * 设置初始化时选中item
     * @param position
     */
    public void setSelectedItem(int position){
        if (position < 0 || position >= mItems.size())
            selectedPosition = 0;
        else
            selectedPosition = position;
    }

    public void setClickListener(OnClickListener listener){
        this.listener = listener;
    }

    public void setButtonName(String BtnName){

        mBtnName = BtnName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choice_dialog);
        initView();
    }

    private void initView() {
        mCommitBtn = (Button) findViewById(R.id.btn_commit);
        mListView = (ListView) findViewById(R.id.dialog_listview);
        mTitleTv = (TextView) findViewById(R.id.tv_title);

        if (title != null)
            mTitleTv.setText(title);

        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onClick(selectedPosition);
                dismiss();
            }
        });

        mAdapter = new SingleChoiceDialogAdapter(mItems);
        mListView.setAdapter(mAdapter);

        if (mBtnName == null )
            return;
        mCommitBtn.setText(mBtnName);


    }

    public interface OnClickListener{
        void onClick(int position);
    }

    public void setTitle(String title){
        this.title = title;
    }

    private class SingleChoiceDialogAdapter extends BaseAdapter{

        private List<String> items;


        public SingleChoiceDialogAdapter(List<String> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null){
                holder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.item_single_choice_dialog, null);
                holder.mImageView = (ImageView) convertView.findViewById(R.id.iv_item);
                holder.mInfoTv = (TextView) convertView.findViewById(R.id.tv_info);
                holder.mLinearLayout = (LinearLayout) convertView.findViewById(R.id.lin_item);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mInfoTv.setText(items.get(position));
            if (selectedPosition == position)
                holder.mImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_dialog_single_choice_red_circle));
            else
                holder.mImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_dialog_single_choice_gray_circle));

            holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

        private class ViewHolder {
            TextView mInfoTv;
            ImageView mImageView;
            LinearLayout mLinearLayout;
        }
    }
}
