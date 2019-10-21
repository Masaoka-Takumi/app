package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acbelter.directionalcarousel.page.PageFragment;
import com.acbelter.directionalcarousel.page.PageLayout;

import java.io.IOException;
import java.io.InputStream;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.ThemeType;
import jp.pioneer.carsync.presentation.model.ThemeSelectItem;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * Created by NSW00_007906 on 2018/01/23.
 */

public class ThemePageFragment extends PageFragment<ThemeSelectItem> {
    private RelativeLayout mItemSelect;
    private TextView mItemNumber;
    private ImageView mLiveMark;
    private ImageView mItemView;
    private TextView mCustomText;
    private Context mContext;
    @Override
    public View setupPage(PageLayout pageLayout, ThemeSelectItem pageItem) {
        mContext= getContext();
        ThemeType type = pageItem.themeType;

        View pageContent = pageLayout.findViewById(R.id.page_content);

        mItemView = (ImageView) pageContent.findViewById(R.id.item_view);
        mItemNumber = (TextView) pageContent.findViewById(R.id.item_number);
        mLiveMark = (ImageView) pageContent.findViewById(R.id.live_mark);
        mItemSelect = (RelativeLayout) pageContent.findViewById(R.id.item_select);
        mCustomText = (TextView) pageContent.findViewById(R.id.custom_text);
        mLiveMark.setVisibility(type.isVideo() ? View.VISIBLE : View.INVISIBLE);
        mItemNumber.setText(String.valueOf(pageItem.number));
        mItemSelect.setAlpha(0);
        mItemNumber.setVisibility(View.INVISIBLE);
        mCustomText.setVisibility(View.INVISIBLE);
        if(type == ThemeType.PICTURE_PATTERN13){
            InputStream in;
            try {
                in = mContext.openFileInput("myPhotoThumbnail.jpg");
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();
                mItemView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            mItemView.setImageResource(type.getThumbnail());
        }

        return pageContent;
    }

    public void setUiColor(int uiColor) {
        if(uiColor!=0&&mItemSelect!=null) {
            ImageView selectFlame1 = (ImageView) mItemSelect.getChildAt(0);
            ImageView selectFlame2 = (ImageView) mItemSelect.getChildAt(1);
            selectFlame1.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0068_chamferselect, uiColor));
            selectFlame2.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0067_chamferselect, uiColor));
        }
    }

    public void setItemSelectAlpha(float alpha) {
        if(mItemSelect!=null) mItemSelect.setAlpha(alpha);
    }

    public void setCustomText(boolean isCustom) {
        if(mCustomText!=null) {
            mCustomText.setVisibility(isCustom?View.VISIBLE:View.INVISIBLE);
        }
    }

}
