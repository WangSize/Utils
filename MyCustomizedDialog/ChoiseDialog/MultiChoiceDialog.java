
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/22.
 */

public class MultiChoiceDialog extends Dialog {
    private Context mContext;

    private Button mCommitBtn;

    private ListView mListView;
    private List<String> mItems;
    private TextView mTitleTv;
    private MultiChoiceDialog.MultiChoiceDialogAdapter mAdapter;

    private String title;
    private String mBtnName;
    private List<Integer> selectedItems ;

    private OnClickListener listener;

    public MultiChoiceDialog(Context context, String message, List<String> items, OnClickListener listener) {
        super(context, R.style.dialog_base_style);
        mContext = context;
        this.title = message;
        this.listener = listener;
        mItems = items;
    }

    /**
     * 设置初始化时选中item
     * @param selectedItems
     */
    public void setSelectedItems(List<Integer> selectedItems){
        if (selectedItems == null || selectedItems.size() >= mItems.size())
            return;
        else
            this.selectedItems = selectedItems;
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
                    listener.onClick(selectedItems);
                dismiss();
            }
        });

        mAdapter = new MultiChoiceDialog.MultiChoiceDialogAdapter(mItems);
        mListView.setAdapter(mAdapter);

        if (mBtnName == null )
            return;
        mCommitBtn.setText(mBtnName);


    }

    public interface OnClickListener{
        void onClick(List<Integer> selectedItems);
    }

    public void setTitle(String title){
        this.title = title;
    }

    private class MultiChoiceDialogAdapter extends BaseAdapter {

        private List<String> items;


        public MultiChoiceDialogAdapter(List<String> items) {
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
                holder = (MultiChoiceDialog.MultiChoiceDialogAdapter.ViewHolder) convertView.getTag();
            }

            holder.mInfoTv.setText(items.get(position));
            if (selectedItems != null && selectedItems.contains(position))
                holder.mImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_dialog_single_choice_red_circle));
            else
                holder.mImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_dialog_single_choice_gray_circle));

            holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                 if (selectedItems == null){
                     selectedItems = new ArrayList<Integer>(items.size());
                     selectedItems.add(position);
                 }else {
                     if (selectedItems.contains(position))
                         selectedItems.remove(Integer.valueOf(position));
                     else
                         selectedItems.add(position);
                 }

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