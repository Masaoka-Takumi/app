package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.AppMusicContract;

/**
 * 楽曲リストAdapter
 */

public class SongAdapter extends AbstractCursorAdapter {
    private Context mContext;
    private boolean mIsShowJacket = false;
    private boolean mIsSphCarDevice = false;
    private boolean mIsSearchResult = false;
    private int mOrientation;
    /**
     * コンストラクタ
     *
     * @param context Context
     * @param c       カーソル
     */
    public SongAdapter(Context context, Cursor c) {
        this(context, c, true);
    }

    /**
     * コンストラクタ
     *
     * @param context Context
     * @param c       カーソル
     * @param isShow  ジャケットを表示するか
     */
    public SongAdapter(Context context, Cursor c, boolean isShow) {
        super(context, c, false);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mIsShowJacket = isShow;
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    public void setSphCarDevice(boolean sphCarDevice) {
        mIsSphCarDevice = sphCarDevice;
    }

    public void setSearchResult(boolean searchResult) {
        mIsSearchResult = searchResult;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_music, parent, false);
        SongAdapter.ViewHolder holder = new SongAdapter.ViewHolder(view);
        if (!mIsShowJacket) {
            holder.albumArt.setVisibility(View.GONE);
            holder.artistName.setVisibility(View.GONE);
            RelativeLayout.LayoutParams param1 = (RelativeLayout.LayoutParams) holder.musicName.getLayoutParams();
            param1.addRule(RelativeLayout.CENTER_VERTICAL);
            holder.musicName.setLayoutParams(param1);
        }
        if(mIsSphCarDevice) {
            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.list_item_background_selector_music_no_focus));
        }
        if(mIsSearchResult) {
            ViewGroup.LayoutParams lp = holder.titleGroup.getLayoutParams();
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mlp.setMargins((int) mContext.getResources().getDimension(R.dimen.player_search_list_item_left_margin), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
            }
            holder.titleGroup.setLayoutParams(mlp);
        }
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SongAdapter.ViewHolder holder = (SongAdapter.ViewHolder) view.getTag();
        if (holder.id != AppMusicContract.Song.getId(cursor)) {
            holder.id = AppMusicContract.Song.getId(cursor);
            holder.musicName.setText(AppMusicContract.Song.getTitle(cursor));
            if (mIsShowJacket) {
                holder.artistName.setText(AppMusicContract.Song.getArtist(cursor));
                Glide.with(context)
                        .load(AppMusicContract.Song.getArtworkUri(cursor))
                        .error(R.drawable.p0070_noimage)
                        .into(holder.albumArt);
            }
        }
    }

    static class ViewHolder {
        @BindView(R.id.title_group) RelativeLayout titleGroup;
        @BindView(R.id.jacket_view) ImageView albumArt;
        @BindView(R.id.title_text) TextView musicName;
        @BindView(R.id.subtitle_text) TextView artistName;
        long id;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
