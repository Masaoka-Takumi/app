package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.view.PagerAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

import static android.view.View.VISIBLE;

/**
 * HOME画面ViewPager用アダプター
 */

public class ShortcutKeyAdapter extends PagerAdapter {

    protected Context mContext;
    protected LayoutInflater mInflater;

    //private static final int PORTRAIT_KEYS = 3;
    private static final int LANDSCAPE_KEYS = 5;
    private int mOrientation;
    private boolean mIsNotification = false;
    private ArrayList<ShortcutKeyItem> mShortcutKeyItems;
    private int[] mKeyId = new int[5];
    private int mColor;
    private ArrayList<BandType> mPresetBandList;
    private int selectedPresetNumber;
    private int mCurrentPagerPosition;
    /**
     * コンストラクタ
     *
     * @param context Context
     */
    public ShortcutKeyAdapter(Context context) {
        mContext = context;
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
        mInflater = LayoutInflater.from(context);
        for (int i = 0; i < LANDSCAPE_KEYS; i++) {
            mKeyId[i] = mContext.getResources().getIdentifier("shortcut_key" + i, "id", mContext.getPackageName());
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.uiColor, outValue, true);
        mColor = outValue.resourceId;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public void setNotification(boolean notification) {
        mIsNotification = notification;
        notifyDataSetChanged();
    }

    public ArrayList<BandType> getPresetBandList() {
        return mPresetBandList;
    }

    public void setPresetBandList(ArrayList<BandType> presetBandList) {
        mPresetBandList = presetBandList;
        notifyDataSetChanged();
    }

    public void setSelectedPresetNumber(int selectedPresetNumber) {
        if(this.selectedPresetNumber != selectedPresetNumber) {
            this.selectedPresetNumber = selectedPresetNumber;
            notifyDataSetChanged();
        }
    }

    public void setCurrentPagerPosition(int currentPagerPosition) {
        if(this.mCurrentPagerPosition != currentPagerPosition) {
            mCurrentPagerPosition = currentPagerPosition;
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setShortcutKeysItems(ArrayList<ShortcutKeyItem> keys) {
        mShortcutKeyItems = keys;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int page = 0;
        if(mShortcutKeyItems != null) {
            page = page + 1;
        }
        if(mPresetBandList != null){
            page = page + mPresetBandList.size()+1;
        }
        return page;
    }

    public int getRealCount(){
        int page = 0;
        if(mShortcutKeyItems != null) {
            page = page + 1;
        }
        if(mPresetBandList != null){
            page = page + mPresetBandList.size();
        }
         return page;
    }

    public int getRealPosition(int pagerPosition) {
        if(mShortcutKeyItems != null&&mPresetBandList != null) {
            if(pagerPosition==getRealCount()){
                return 1;
            }else{
                return pagerPosition;
            }
        }
        if(mPresetBandList != null){
            if(pagerPosition==getRealCount()){
                return 0;
            }else{
                return pagerPosition;
            }
        }
        return pagerPosition;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view;
        int dispPosition;
        dispPosition = getRealPosition(position);
        if (position < mCurrentPagerPosition) {
            dispPosition = 0;
        }
        //ショートカットキーの設定
        if(mShortcutKeyItems != null&&dispPosition==0) {
            int pageAllKey;
            pageAllKey = LANDSCAPE_KEYS;
            int pageKeyNum = pageAllKey;
            if (dispPosition == getCount() - 1) {
                pageKeyNum = mShortcutKeyItems.size() - (dispPosition * pageAllKey);
            }
            if (pageKeyNum % 2 == 0) {
                view = mInflater.inflate(R.layout.element_shortcut_keys_even, container, false);
            } else {
                view = mInflater.inflate(R.layout.element_shortcut_keys, container, false);
            }

            for (int i = 0; i < pageKeyNum; i++) {
                int keyId = mKeyId[i];
                switch (pageKeyNum) {
                    case 1:
                        keyId = mKeyId[2];
                        break;
                    case 2:
                        keyId = mKeyId[i + 1];
                        break;
                    case 3:
                        keyId = mKeyId[i + 1];
                        break;
                    default:
                        break;
                }
                RelativeLayout keyGroup = (RelativeLayout) view.findViewById(keyId);
                ImageView key = (ImageView) keyGroup.findViewById(R.id.key);
                key.setTag(dispPosition * pageAllKey + i);
                key.setImageResource(mShortcutKeyItems.get(dispPosition * pageAllKey + i).imageResource);
                ImageView iconBack = (ImageView) keyGroup.findViewById(R.id.icon_back);
                View icon = keyGroup.findViewById(R.id.icon);
                ImageView notification = (ImageView) keyGroup.findViewById(R.id.notification_circle);
                if (mShortcutKeyItems.get(dispPosition * pageAllKey + i).optionImageResource != 0) {
                    iconBack.setImageDrawable(ImageViewUtil.setTintColor(mContext, mShortcutKeyItems.get(dispPosition * pageAllKey + i).optionImageResource, mColor));
                    iconBack.setVisibility(VISIBLE);
                    icon.setVisibility(VISIBLE);
                } else {
                    iconBack.setVisibility(View.GONE);
                    icon.setVisibility(View.GONE);
                }

                key.setOnClickListener((View v) -> {
                    int keyIndex = (int) v.getTag();
                    ShortcutKey shortCutKey = mShortcutKeyItems.get(keyIndex).key;
                    onClickKey(shortCutKey);
                });

                if (mShortcutKeyItems.get(dispPosition * pageAllKey + i).key == ShortcutKey.PHONE ||
                        mShortcutKeyItems.get(dispPosition * pageAllKey + i).key == ShortcutKey.SOURCE ||
                        mShortcutKeyItems.get(dispPosition * pageAllKey + i).key == ShortcutKey.VOICE) {
                    key.setOnLongClickListener((View v) -> {
                        int keyIndex = (int) v.getTag();
                        ShortcutKey shortCutKey = mShortcutKeyItems.get(keyIndex).key;
                        onLongClickKey(shortCutKey);
                        return true;
                    });
                }
                key.setEnabled(mShortcutKeyItems.get(dispPosition * pageAllKey + i).enabled);

                if (mIsNotification && mShortcutKeyItems.get(dispPosition * pageAllKey + i).key == ShortcutKey.VOICE) {
                    notification.setVisibility(View.VISIBLE);
                } else {
                    notification.setVisibility(View.GONE);
                }

            }
            //プリセットリストの設定
        }else if(mPresetBandList!=null){
            int index;
            if(mShortcutKeyItems!=null){
                index = dispPosition-1;
            }else{
                index = dispPosition;
            }
            view = mInflater.inflate(R.layout.element_shortcut_keys_preset, container, false);
            for (int i = 1; i <= 6; i++) {
                int keyId = mContext.getResources().getIdentifier("preset" + i, "id", mContext.getPackageName());
                RelativeLayout keyGroup = (RelativeLayout) view.findViewById(keyId);
                ImageView iconBack = (ImageView) keyGroup.getChildAt(0);
                ImageView iconBack2 = (ImageView) keyGroup.getChildAt(1);
                keyGroup.setTag(i);

                if (dispPosition == mCurrentPagerPosition && i == selectedPresetNumber) {
                    keyGroup.setEnabled(false);
                    iconBack.setVisibility(View.VISIBLE);
                    iconBack2.setVisibility(View.VISIBLE);
                } else {
                    keyGroup.setEnabled(true);
                    iconBack.setVisibility(View.INVISIBLE);
                    iconBack2.setVisibility(View.INVISIBLE);
                    if (dispPosition == mCurrentPagerPosition) {
                        keyGroup.setOnClickListener((View v) -> {
                            int keyIndex = (int) v.getTag();
                            onClickPreset(mPresetBandList.get(index), keyIndex);
                        });
                        keyGroup.setOnLongClickListener((View v) -> {
                            int keyIndex = (int) v.getTag();
                            onLongClickPreset(mPresetBandList.get(index), keyIndex);
                            return true;
                        });
                    }
                }
            }
        }else{
            return null;
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
    protected void onClickKey(ShortcutKey shortCutKey) {
    }

    /**
     * Key長押し処理
     */
    protected void onLongClickKey(ShortcutKey shortCutKey) {
    }

    /**
     * Preset押下処理
     */
    protected void onClickPreset(BandType bandType, int preset) {
    }

    /**
     * Preset長押し処理
     */
    protected void onLongClickPreset(BandType bandType, int preset) {
    }
}
