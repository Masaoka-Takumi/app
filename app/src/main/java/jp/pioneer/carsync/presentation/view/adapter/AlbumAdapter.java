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
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.AppMusicContract;

/**
 * アルバムリストAdapter
 */

public class AlbumAdapter extends AbstractCursorAdapter {
    private boolean mIsShowArtist = false;
    private boolean mIsSphCarDevice = false;
    private boolean mIsSearchResult = false;
    private int mOrientation;
    /**
     * コンストラクタ
     *
     * @param context Context
     * @param c       カーソル
     */
    public AlbumAdapter(Context context, Cursor c) {
        this(context, c, true);
    }

    public void setSphCarDevice(boolean sphCarDevice) {
        mIsSphCarDevice = sphCarDevice;
    }

    public void setSearchResult(boolean searchResult) {
        mIsSearchResult = searchResult;
    }

    /**
     * コンストラクタ
     *
     * @param context Context
     * @param c       カーソル
     * @param isShow  アーティスト名を表示するか
     */
    public AlbumAdapter(Context context, Cursor c, boolean isShow) {
        super(context, c, false);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mIsShowArtist = isShow;
        Configuration config = mContext.getResources().getConfiguration();
        mOrientation = config.orientation;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_music_play, parent, false);
        AlbumAdapter.ViewHolder holder = new AlbumAdapter.ViewHolder(view);
        if (!mIsShowArtist) {
            holder.artistName.setVisibility(View.GONE);
            RelativeLayout.LayoutParams param1 = (RelativeLayout.LayoutParams)holder.albumName.getLayoutParams();
            param1.addRule(RelativeLayout.CENTER_VERTICAL);
            holder.albumName.setLayoutParams(param1);
        }
        if(mIsSphCarDevice) {
            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.list_item_background_selector_music_no_focus));
        }
        if(mIsSearchResult) {
            ViewGroup.LayoutParams lp = holder.jacketGroup.getLayoutParams();
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mlp.setMargins((int) mContext.getResources().getDimension(R.dimen.player_search_list_item_left_margin), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
            }
            holder.jacketGroup.setLayoutParams(mlp);
        }
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        AlbumAdapter.ViewHolder holder = (AlbumAdapter.ViewHolder) view.getTag();
        if (holder.id != AppMusicContract.Album.getId(cursor)) {
            holder.id = AppMusicContract.Album.getId(cursor);
            holder.albumName.setText(AppMusicContract.Album.getAlbum(cursor));
            holder.artistName.setText(AppMusicContract.Album.getArtist(cursor));
            Glide.with(context)
                    .load(AppMusicContract.Album.getArtworkUri(cursor))
                    .error(R.drawable.p0070_noimage)
                    .into(holder.albumArt);
        }
    }

    /**
     * ジャケット押下処理
     *
     * @param id アルバムID
     */
    protected void onJacketClick(long id) {
    }

    class ViewHolder {
        @BindView(R.id.jacket_group) RelativeLayout jacketGroup;
        @BindView(R.id.jacket_view) ImageView albumArt;
        @BindView(R.id.title_text) TextView albumName;
        @BindView(R.id.subtitle_text) TextView artistName;
        long id;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.play_button)
        public void onClick(View v) {
            onJacketClick(id);
        }
    }
}
