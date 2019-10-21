package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.model.AbstractPresetItem;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * Created by NSW00_007906 on 2017/08/21.
 */

public class RadioPresetPageAdapter extends PagerAdapter {

    protected Context mContext;
    protected LayoutInflater mInflater;

    private static final int LANDSCAPE_ITEMS = 6;

    private ArrayList<AbstractPresetItem> mRadioPresetItems;
    private int mColor;
    private int mSelectedPosition;
    private boolean mIsSphCarDevice = false;

    private static final int[] PRESET_BUTTON_IMAGE = new int[]{
            R.drawable.p0251_chnum_1nrm,
            R.drawable.p0252_chnum_1nrm,
            R.drawable.p0253_chnum_1nrm,
            R.drawable.p0254_chnum_1nrm,
            R.drawable.p0255_chnum_1nrm,
            R.drawable.p0256_chnum_1nrm,
    };
    private static final int[] PRESET_BUTTON_SELECTED_IMAGE = new int[]{
            R.drawable.p0261_chnumselect_1nrm,
            R.drawable.p0262_chnumselect_1nrm,
            R.drawable.p0263_chnumselect_1nrm,
            R.drawable.p0264_chnumselect_1nrm,
            R.drawable.p0265_chnumselect_1nrm,
            R.drawable.p0266_chnumselect_1nrm,
    };

    /**
     * コンストラクタ
     *
     * @param context Context
     */
    public RadioPresetPageAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setSphCarDevice(boolean sphCarDevice) {
        mIsSphCarDevice = sphCarDevice;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setRadioPresetItems(ArrayList<AbstractPresetItem> items) {
        mRadioPresetItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mRadioPresetItems != null) {
            int keyNum = mRadioPresetItems.size();
            return (keyNum - 1) / LANDSCAPE_ITEMS + 1;
        }
        return 0;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view;

        if (mRadioPresetItems == null) {
            return null;
        }
        view = mInflater.inflate(R.layout.element_page_preset_channel, container, false);
        int pageAllKey;
        pageAllKey = LANDSCAPE_ITEMS;
        int pageKeyNum = pageAllKey;
        if (position == getCount() - 1) {
            pageKeyNum = mRadioPresetItems.size() - (position * pageAllKey);
        }
        for (int i = 0; i < pageKeyNum; i++) {
            int presetButtonGroupId = mContext.getResources().getIdentifier("preset_button" + (i + 1) + "_group", "id", mContext.getPackageName());
            RelativeLayout presetButtonGroup = (RelativeLayout) view.findViewById(presetButtonGroupId);
            presetButtonGroup.setTag(i + 1);
            int presetButtonId = mContext.getResources().getIdentifier("preset_button" + (i + 1), "id", mContext.getPackageName());
            RelativeLayout presetButton = (RelativeLayout) view.findViewById(presetButtonId);
            ImageView presetButtonImage = (ImageView) presetButton.getChildAt(1);
            presetButtonImage.setImageDrawable(ImageViewUtil.setTintColor(mContext, PRESET_BUTTON_IMAGE[i], mColor));
            int presetButtonSelectedId = mContext.getResources().getIdentifier("preset_button" + (i + 1) + "_selected", "id", mContext.getPackageName());
            RelativeLayout presetButtonSelected = (RelativeLayout) view.findViewById(presetButtonSelectedId);
            ImageView presetButtonSelectedImage = (ImageView) presetButtonSelected.getChildAt(1);
            presetButtonSelectedImage.setImageDrawable(ImageViewUtil.setTintColor(mContext, PRESET_BUTTON_SELECTED_IMAGE[i], mColor));
            int presetTitleId = mContext.getResources().getIdentifier("preset_title" + (i + 1), "id", mContext.getPackageName());
            TextView presetTitle = (TextView) view.findViewById(presetTitleId);
            int frequencyId = mContext.getResources().getIdentifier("frequency_text" + (i + 1), "id", mContext.getPackageName());
            TextView frequencyText = (TextView) view.findViewById(frequencyId);
            presetTitle.setText(mRadioPresetItems.get(position * pageAllKey + i).channelName);
            String freqText = mRadioPresetItems.get(position * pageAllKey + i).frequencyText;
            String freqFormat= freqText.substring(0, freqText.length() - 3) + " " + freqText.substring(freqText.length() - 3);
            frequencyText.setText(freqFormat);
            if (position * pageAllKey + i == mSelectedPosition&&!mIsSphCarDevice) {
                presetButton.setVisibility(View.INVISIBLE);
                presetButtonSelected.setVisibility(View.VISIBLE);
            } else {
                presetButton.setVisibility(View.VISIBLE);
                presetButtonSelected.setVisibility(View.INVISIBLE);
            }
            presetButtonGroup.setOnClickListener((View v) -> {
                int pch = position * pageAllKey + (int) v.getTag();
                onClickKey(pch);
            });
        }

        // コンテナに追加
        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // コンテナから View を削除
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // Object 内に View が存在するか判定する
        return view.equals(object);
    }

    /**
     * Key押下処理
     */
    protected void onClickKey(int pch) {
    }

}
