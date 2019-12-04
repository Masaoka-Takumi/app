package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.pioneer.carsync.R;

public class AlexaTutorialPagerAdapter extends PagerAdapter {
    public static final int PAGER_COUNT = 3;
    private LayoutInflater mLayoutInflater;

    /**
     * コンストラクタ
     * Viewを生成するためにLayoutInflaterが必要
     * Context経由で取得する
     * @param context Context
     */
    public AlexaTutorialPagerAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view;
        switch (position){
            case 0:
                view = mLayoutInflater.inflate(R.layout.element_setting_alexa_guidance_of_putting_smartphone, container, false);
                break;
            case 1:
                view = mLayoutInflater.inflate(R.layout.element_setting_alexa_guidance_of_usage, container, false);
                break;
            case 2:
            default:
                view = mLayoutInflater.inflate(R.layout.element_setting_alexa_guidance_of_example_usage, container, false);
                // TODO #5200 リンクの設定
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
        return PAGER_COUNT;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        // Object 内に View が存在するか判定する
        return view.equals(o);
    }
}
