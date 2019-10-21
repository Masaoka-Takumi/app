package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.presentation.model.CustomKey;
import jp.pioneer.carsync.presentation.model.CustomKeyItem;
import me.grantland.widget.AutofitTextView;
import timber.log.Timber;

/**
 * カスタムキー割当画面のArrayAdapter
 */
public class CustomKeyAdapter extends ArrayAdapter<CustomKeyItem> {

    @Inject AppSharedPreference mPreference;
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<CustomKeyItem> mCustomKeyItemArrayList;
    private int mSelectedPosition = -1; // ユーザが選択しているカスタムキー

    public CustomKeyAdapter(Context context, ArrayList<CustomKeyItem> customKeyItemArrayList){
        super(context, 0);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mCustomKeyItemArrayList = customKeyItemArrayList; // 表示するArrayList
    }

    public void setSelectedIndex(int selectedIndex){
        mSelectedPosition = selectedIndex;
        notifyDataSetChanged(); // Adapterの内容を即時反映
    }

    @Override
    public int getCount(){
        return mCustomKeyItemArrayList.size();
    }

    @Override
    public CustomKeyItem getItem(int position){
        return mCustomKeyItemArrayList.get(position);
    }

    @Nonnull
    @Override
    public View getView(int position, View convertView, @Nonnull ViewGroup parent){
        Timber.i("getView position=" + position);
        CustomKeyAdapter.ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.element_item_select_singlechoice, parent, false);
            holder = new CustomKeyAdapter.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (CustomKeyAdapter.ViewHolder) convertView.getTag();
        }

        // ソース切替、ソースON/OFF、ソース一覧表示 のリスト表示設定
        CustomKeyItem item = mCustomKeyItemArrayList.get(position);
        if(item.getCustomKey() == CustomKey.SOURCE_CHANGE
            || item.getCustomKey() == CustomKey.SOURCE_ON_OFF
            || item.getCustomKey() == CustomKey.SOURCE_LIST) {
            holder.customKeyName.setVisibility(View.GONE); // TextViewを非表示
            holder.autofitCustomKeyName.setText(item.getNameId()); // 表示する文字列設定
            holder.autofitCustomKeyName.setVisibility(View.VISIBLE); // autoFitTextViewを表示
            holder.imageIcon.setVisibility(View.GONE); // imageIconを非表示
        }
        // ダイレクトソース切替 のリスト表示設定
        else if(item.getCustomKey() == CustomKey.SOURCE_DIRECT){
            holder.customKeyName.setText(item.getNameId()); // 表示する文字列設定
            holder.customKeyName.setVisibility(View.VISIBLE); // TextViewを表示
            holder.autofitCustomKeyName.setVisibility(View.GONE); // autoFitTextViewを非表示
            holder.imageIcon.setImageResource(item.getIcon());
            holder.imageIcon.setVisibility(View.VISIBLE); // imageIconを表示
        }
        // 3rd App のリスト表示設定
        else if(item.getCustomKey() == CustomKey.THIRD_PARTY_APP){
            //パッケージ名からアイコンの取得
            holder.customKeyName.setText(item.getApplication().label);
            holder.customKeyName.setVisibility(View.VISIBLE); // TextViewを表示
            holder.autofitCustomKeyName.setVisibility(View.GONE); // autoFitTextViewを非表示
            try {
                PackageManager pm = mContext.getPackageManager();
                ApplicationInfo info = pm.getApplicationInfo(item.getApplication().packageName, PackageManager.GET_META_DATA);
                Drawable icon;
                if(info != null) {
                    icon = info.loadIcon(pm);
                    holder.imageIcon.setImageDrawable(icon); // iconを設定
                    holder.imageIcon.setVisibility(View.VISIBLE); // imageIconを表示
                } else {
                    holder.imageIcon.setVisibility(View.GONE); // imageIconを非表示
                }
            } catch (PackageManager.NameNotFoundException e) {
                // 3rdAppが端末に見つからない場合
                e.printStackTrace();
                holder.imageIcon.setVisibility(View.INVISIBLE);
            }
        }
        else {
            Timber.w("CustomKeyItem not exist.");
            holder.customKeyName.setVisibility(View.VISIBLE);
            holder.autofitCustomKeyName.setVisibility(View.GONE);
            holder.imageIcon.setVisibility(View.GONE);
        }

        // ラジオボタンのチェック状態の設定
        if(position == mSelectedPosition){
            holder.radioButton.setChecked(true);
        } else{
            holder.radioButton.setChecked(false);
        }

        return convertView;
    }

    static class ViewHolder{
        @BindView(R.id.imageIcon) ImageView imageIcon;
        @BindView(android.R.id.text1) TextView customKeyName;
        @BindView(R.id.autofit_text1) AutofitTextView autofitCustomKeyName;
        @BindView(R.id.radioButton1) RadioButton radioButton;
        @BindView(R.id.separator_bottom) View separator_bottom;

        ViewHolder(View itemView){
            ButterKnife.bind(this, itemView);
        }
    }
}
