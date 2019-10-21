package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;

/**
 * AdasTutorialPagerAdapter
 */

public class AdasTutorialPagerAdapter extends PagerAdapter {

    protected Context mContext;
    protected LayoutInflater mInflater;

    /**
     * コンストラクタ
     *
     * @param context Context
     */
    public AdasTutorialPagerAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view;
        view = mInflater.inflate(R.layout.element_page_adas_tutorial, container, false);
        ViewHolder vh = new ViewHolder(view);
        if(position == 0){
            vh.image.setImageResource(R.drawable.p1530_fcw);
            vh.descriptionText.setText(R.string.set_284);
            vh.descriptionText2.setText(R.string.set_368);
        }else if(position == 1){
            vh.image.setImageResource(R.drawable.p1532_ldwl);
            vh.descriptionText.setText(R.string.set_285);
            vh.descriptionText2.setText(R.string.set_369);
        }else if(position == 2){
            vh.image.setImageResource(R.drawable.p1531_pcw);
            vh.descriptionText.setText(R.string.set_286);
            //vh.descriptionText2.setText(R.string.set_369);
        }
        view.invalidate();
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

    static class ViewHolder {
        @BindView(R.id.image) ImageView image;
        @BindView(R.id.description_text) TextView descriptionText;
        @BindView(R.id.description_text2) TextView descriptionText2;
        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
