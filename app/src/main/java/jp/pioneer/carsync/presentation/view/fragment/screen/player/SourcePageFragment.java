package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.acbelter.directionalcarousel.page.PageFragment;
import com.acbelter.directionalcarousel.page.PageLayout;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.model.SourceSelectItem;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * ソース選択 各ソースのPageFragment
 */
public class SourcePageFragment extends PageFragment<SourceSelectItem> {
    private static final float INSET_WIDTH_FACTOR = (float) 25 / 190;
    private static int mColor;
    private ImageView mIconSelect;
    private ImageView mIconOval;
    private ImageView mIcon;

    @Override
    public View setupPage(PageLayout pageLayout, SourceSelectItem pageItem) {
        View pageContent = pageLayout.findViewById(R.id.page_content);
        mIcon = (ImageView) pageContent.findViewById(R.id.icon);
        mIconOval = (ImageView) pageContent.findViewById(R.id.icon_oval);
        mIconSelect = (ImageView) pageContent.findViewById(R.id.icon_select);
        TextView title = (TextView) pageContent.findViewById(R.id.title);
        if (pageItem.sourceType != null) {
            mIcon.setImageResource(pageItem.sourceTypeIcon);
            mIconSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0080_select, mColor));
            title.setText(pageItem.sourceTypeName);
        }

        if (pageItem.appPackageName != null) {
            mIcon.setImageDrawable(getAppIcon(getContext(), pageItem.appPackageName));
            mIconOval.setVisibility(View.INVISIBLE);
            mIconSelect.setVisibility(View.INVISIBLE);
            title.setVisibility(View.INVISIBLE);
        }
        return pageContent;
    }

    public static void setColor(int color) {
        mColor = color;
    }

    public void setIconAlpha(float alpha) {
        mIcon.setAlpha(alpha);
    }

    public void setIconOvalAlpha(float alpha) {
        mIconOval.setAlpha(alpha);
    }

    public void setIconSelectAlpha(float alpha) {
        mIconSelect.setAlpha(alpha);
    }

    @Nullable
    public static Drawable getAppIcon(@NonNull Context context, @NonNull String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return (info != null) ? getAppIcon(context, info) : null;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static Drawable getAppIcon(@NonNull Context context, @NonNull ApplicationInfo info) {
        PackageManager pm = context.getPackageManager();
        //int inset = (int) ((float) d.getIntrinsicWidth() * INSET_WIDTH_FACTOR);
        //return new InsetDrawable(d, inset);
        return info.loadIcon(pm);
    }
}
