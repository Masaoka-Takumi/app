package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
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
 * プレイリストAdapter
 */

public class PlaylistAdapter extends AbstractCursorAdapter {
    private boolean mIsSphCarDevice = false;
    /**
     * コンストラクタ
     *
     * @param context Context
     * @param c       カーソル
     */
    public PlaylistAdapter(Context context, Cursor c) {
        super(context, c, false);
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setSphCarDevice(boolean sphCarDevice) {
        mIsSphCarDevice = sphCarDevice;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_music_play, parent, false);
        PlaylistAdapter.ViewHolder holder = new PlaylistAdapter.ViewHolder(view);
        holder.noText.setVisibility(View.GONE);
        RelativeLayout.LayoutParams param1 = (RelativeLayout.LayoutParams)holder.listName.getLayoutParams();
        param1.addRule(RelativeLayout.CENTER_VERTICAL);
        holder.listName.setLayoutParams(param1);
        if(mIsSphCarDevice) {
            view.setBackground(ContextCompat.getDrawable(mContext, R.drawable.list_item_background_selector_music_no_focus));
        }
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        PlaylistAdapter.ViewHolder holder = (PlaylistAdapter.ViewHolder) view.getTag();
        if (holder.id != AppMusicContract.Playlist.getId(cursor)) {
            holder.id = AppMusicContract.Playlist.getId(cursor);
            holder.listName.setText(AppMusicContract.Playlist.getName(cursor));
            Glide.with(context)
                    .load(AppMusicContract.Playlist.getArtworkUri(cursor))
                    .error(R.drawable.p0070_noimage)
                    .into(holder.albumArt);
        }
    }

    /**
     * ジャケット押下処理
     *
     * @param id プレイリストID
     */
    protected void onJacketClick(long id) {
    }

    class ViewHolder {
        @BindView(R.id.jacket_view) ImageView albumArt;
        @BindView(R.id.title_text) TextView listName;
        @BindView(R.id.subtitle_text) TextView noText;
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
