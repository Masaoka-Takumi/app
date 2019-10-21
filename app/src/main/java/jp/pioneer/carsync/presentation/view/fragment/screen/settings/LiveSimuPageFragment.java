package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acbelter.directionalcarousel.page.PageFragment;
import com.acbelter.directionalcarousel.page.PageLayout;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.model.LiveSimulationItem;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * Created by NSW00_007906 on 2018/01/25.
 */

public class LiveSimuPageFragment extends PageFragment<LiveSimulationItem> {
    private RelativeLayout mItemSelect;
    private TextView mItemNumber;
    private ImageView mLiveMark;
    private ImageView mItemView;
    private TextView mCustomText;
    private Context mContext;

    @Override
    public View setupPage(PageLayout pageLayout, LiveSimulationItem pageItem) {
        mContext= getContext();
        View pageContent = pageLayout.findViewById(R.id.page_content);

        mItemView = (ImageView) pageContent.findViewById(R.id.item_view);
        mItemNumber = (TextView) pageContent.findViewById(R.id.item_number);
        mLiveMark = (ImageView) pageContent.findViewById(R.id.live_mark);
        mItemSelect = (RelativeLayout) pageContent.findViewById(R.id.item_select);
        mCustomText = (TextView) pageContent.findViewById(R.id.custom_text);
        mLiveMark.setVisibility(View.INVISIBLE);
        mItemNumber.setText(String.valueOf(pageItem.number));
        mItemSelect.setAlpha(0);
        mCustomText.setVisibility(View.INVISIBLE);
        mItemView.setImageResource(pageItem.resourceId);
        mItemNumber.setVisibility(View.INVISIBLE);

        TypedValue outValue = new TypedValue();
        int uiColor;
        getContext().getTheme().resolveAttribute(R.attr.uiColor, outValue, true);
        uiColor = outValue.resourceId;
        ImageView selectFlame1 = (ImageView) mItemSelect.getChildAt(0);
        ImageView selectFlame2 = (ImageView) mItemSelect.getChildAt(1);
        selectFlame1.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0068_chamferselect, uiColor));
        selectFlame2.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0067_chamferselect, uiColor));

        return pageContent;
    }

    public void setItemSelectAlpha(float alpha) {
        if(mItemSelect!=null) mItemSelect.setAlpha(alpha);
    }

}
