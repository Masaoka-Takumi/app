package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import me.grantland.widget.AutofitTextView;

/**
 * Created by NSW00_007906 on 2018/10/11.
 */

public class AlexaSettingAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<String> mTypeArray = new ArrayList<>();
    private boolean mIsEnabled = true;
    private String mAlexaLanguageSetting;
    public AlexaSettingAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, 0, objects);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mTypeArray = objects;
    }

    public void setAlexaLanguageSetting(String language) {
        mAlexaLanguageSetting = language;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTypeArray.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getPosition(@Nullable String item) {
        return super.getPosition(item);
    }

    @Override
    public  @NonNull
    View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        ViewHolder vh;
        if(view == null) {
            view = mLayoutInflater.inflate(R.layout.element_list_item_summary, parent, false);
            vh = new ViewHolder(view);
            vh.mTitle.setText(mTypeArray.get(position));
            vh.mSeparator.setVisibility(View.INVISIBLE);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }
        if(position == 0) {
            if (mAlexaLanguageSetting != null) {
                vh.mSummary.setText(mAlexaLanguageSetting);
                vh.mSummary.setVisibility(View.VISIBLE);
            } else {
                vh.mSummary.setText("");
                vh.mSummary.setVisibility(View.GONE);
            }
        }else{
            vh.mSummary.setText("");
            vh.mSummary.setVisibility(View.GONE);
        }

        vh.separatorBottom.setVisibility(View.INVISIBLE);
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.titleText) AutofitTextView mTitle;
        @BindView(R.id.summaryText) TextView mSummary;
        @BindView(R.id.separator) View mSeparator;
        @BindView(R.id.separator_bottom) View separatorBottom;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

}
