package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

public class AlexaTutorialPagerAdapter extends PagerAdapter {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private ArrayList<AlexaTutorialPage> mViewPagerList;

    /**
     * コンストラクタ
     * Viewを生成するためにLayoutInflaterが必要
     * Context経由で取得する
     * @param context Context
     */
    public AlexaTutorialPagerAdapter(Context context, ArrayList<AlexaTutorialPage> arrayList) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mViewPagerList = arrayList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view;
        switch (mViewPagerList.get(position)){
            case GUIDANCE_OF_PUTTING_SMARTPHONE:
                view = mLayoutInflater.inflate(R.layout.element_setting_alexa_guidance_of_putting_smartphone, container, false);
                break;
            case GUIDANCE_OF_USAGE:
                view = mLayoutInflater.inflate(R.layout.element_setting_alexa_guidance_of_usage, container, false);
                break;
            case GUIDANCE_OF_EXAMPLE_USAGE:
            default:
                view = mLayoutInflater.inflate(R.layout.element_setting_alexa_guidance_of_example_usage, container, false);
                TextView linkTextView = view.findViewById(R.id.linkTextView);
                setLinkTextView(mContext, linkTextView);
                break;
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // コンテナから View を削除
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return mViewPagerList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        // Object 内に View が存在するか判定する
        return view.equals(o);
    }

    private void setLinkTextView(Context context, TextView textView) {
        String str = context.getString(R.string.set_320);
        String linkStr = context.getString(R.string.set_322);
        int result = str.indexOf(linkStr);
        if(result != -1) {
            String htmlBlogStr = str.substring(0, result)
                    + "<font color =\"#00a5cf\" ><a href=\"https://play.google.com/store/apps/details?id=com.amazon.dee.app\">" + linkStr + "</a></font>"
                    + str.substring(result+linkStr.length());
            CharSequence blogChar = fromHtml(htmlBlogStr);
            textView.setText(blogChar);
            MovementMethod mMethod = LinkMovementMethod.getInstance();
            textView.setMovementMethod(mMethod);
        } else {
            textView.setText(str);
        }
    }

    @SuppressWarnings("deprecation")
    private static CharSequence fromHtml(String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    /**
     * Alexaチュートリアル画面の識別子
     */
    public enum AlexaTutorialPage {
        GUIDANCE_OF_PUTTING_SMARTPHONE, // 設置ガイダンス
        GUIDANCE_OF_USAGE, // Alexaの使い方
        GUIDANCE_OF_EXAMPLE_USAGE, // 使い方の例
        ;
    }
}
