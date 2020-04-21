package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.model.TipsItem;
import jp.pioneer.carsync.presentation.model.TipsTag;

/**
 * TipsAdapter
 */

public class TipsAdapter extends ArrayAdapter<TipsItem> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<TipsItem> mItems = new ArrayList<>();
    private String mTagType = "all";
    private boolean mIsAlexaAvailableCountry = false;
    public TipsAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setItems(List<TipsItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public void setTagType(String tagType) {
        mTagType = tagType;
        notifyDataSetChanged();
    }

    public void setAlexaAvailableCountry(boolean isAvailable){
        mIsAlexaAvailableCountry = isAvailable;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getPosition(@Nullable TipsItem item) {
        return super.getPosition(item);
    }

    @Override
    public  @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(position == 0) {
            parent.setBackground(null);
        }
        View view = convertView;
        TipsAdapter.ViewHolder vh;
        if(view == null) {
            view = mLayoutInflater.inflate(R.layout.element_list_item_tips, parent, false);
            vh = new TipsAdapter.ViewHolder(view);
            view.setTag(vh);
        } else {
            vh = (TipsAdapter.ViewHolder) view.getTag();
        }
        vh.mContainer.setVisibility(View.GONE);
        if(mItems!=null&&mItems.size()>position) {
            TipsTag[] tags = mItems.get(position).tags;
            boolean isAlexaTips = false;
            if (tags.length > 0) {
                for (TipsTag tag : tags) {
                    if(tag.slug.equals("alexa")){
                        isAlexaTips = true;
                        break;
                    }
                }
                //Alexa記事で対応国でない場合は表示しない
                if(!isAlexaTips||mIsAlexaAvailableCountry) {
                    if (mTagType.equals("all")) {
                        vh.mIcon.setImageBitmap(tags[0].iconImage);
                        vh.mContainer.setVisibility(View.VISIBLE);
                        parent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.tips_background_color));
                    } else {
                        for (TipsTag tag : tags) {
                            if (tag.slug.equals(mTagType)) {
                                vh.mIcon.setImageBitmap(tag.iconImage);
                                vh.mContainer.setVisibility(View.VISIBLE);
                                parent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.tips_background_color));
                                break;
                            }
                        }
                    }
                }
            }
            vh.mBackground.setImageBitmap(mItems.get(position).thumbImage);
            vh.mTitle.setText(mItems.get(position).title);
            vh.mDescription.setText(mItems.get(position).description);
        }
        return view;
    }

    static class ViewHolder {
        @BindView(R.id.tips_container) RelativeLayout mContainer;
        @BindView(R.id.background) ImageView mBackground;
        @BindView(R.id.icon) ImageView mIcon;
        @BindView(R.id.title_text) TextView mTitle;
        @BindView(R.id.contents_text) TextView mDescription;
        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

}
