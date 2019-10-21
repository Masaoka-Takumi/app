package jp.pioneer.carsync.presentation.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.TunerSeekStep;

/**
 * ラジオお気に入りリストadapter
 */

public class RadioFavoriteAdapter extends AbstractCursorAdapter {
    private MediaSourceType mSourceType;
    private TunerSeekStep mSeekStep;

    public void setSeekStep(TunerSeekStep seekStep) {
        mSeekStep = seekStep;
    }

    public TunerSeekStep getSeekStep() {
        return mSeekStep;
    }

    /**
     * コンストラクタ
     *
     * @param context Context
     * @param c       Cursor
     */
    public RadioFavoriteAdapter(Context context, Cursor c, MediaSourceType type) {
        super(context, c, false);
        mInflater = LayoutInflater.from(context);
        mSourceType = type;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.element_list_item_radio, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if(mSourceType == MediaSourceType.RADIO) {
            // バンド
            holder.band.setText(TunerContract.FavoriteContract.Radio.getBandType(cursor).name());
            // 周波数
            String description = TunerContract.FavoriteContract.Radio.getDescription(cursor);
            try {
                String[] data = description.split(" ");
                String freqFormat= data[1].substring(0, data[1].length() - 3) + " " + data[1].substring(data[1].length() - 3);
                holder.frequency.setText(freqFormat);
            } catch (ArrayIndexOutOfBoundsException e) {
                holder.frequency.setText(description);
            }
            // 番組名
            holder.title.setText(TunerContract.FavoriteContract.Radio.getName(cursor));
            //登録されたお気に入りリストがAM Bandかつ、現在のAM Step(9kHz/10kHz)設定と異なる周波数が登録されている場合グレーアウト
            if(TunerContract.FavoriteContract.Radio.getBandType(cursor).isAMVariant()){
                if(TunerContract.FavoriteContract.Radio.getTunerSeekStep(cursor)!=mSeekStep){
                    holder.band.setEnabled(false);
                    holder.title.setEnabled(false);
                }
            }
        } else if(mSourceType == MediaSourceType.SIRIUS_XM) {
            // バンド
            holder.band.setText(TunerContract.FavoriteContract.SiriusXm.getBandType(cursor).name());
            // 周波数
            int number = TunerContract.FavoriteContract.SiriusXm.getChannelNo(cursor);
            holder.frequency.setText(String.format(Locale.ENGLISH, "CH %03d", number));

            // 番組名
            holder.title.setText(TunerContract.FavoriteContract.SiriusXm.getName(cursor));
        } else if(mSourceType == MediaSourceType.DAB) {
            // バンド
            holder.band.setText(TunerContract.FavoriteContract.Dab.getBandType(cursor).getLabel());
            // 周波数
            String description = TunerContract.FavoriteContract.Dab.getDescription(cursor);
            try {
                String[] data = description.split(" ");
                String freqFormat= data[1].substring(0, data[1].length() - 3) + " " + data[1].substring(data[1].length() - 3);
                holder.frequency.setText(freqFormat);
            } catch (ArrayIndexOutOfBoundsException e) {
                holder.frequency.setText(description);
            }
            // 番組名
            holder.title.setText(TunerContract.FavoriteContract.Dab.getName(cursor));
        }else if(mSourceType == MediaSourceType.HD_RADIO){
            // バンド
            holder.band.setText(TunerContract.FavoriteContract.HdRadio.getBandType(cursor).name());
            // 周波数
            String description = TunerContract.FavoriteContract.HdRadio.getDescription(cursor);
            try {
                String[] data = description.split(" ");
                String freqFormat= data[1].substring(0, data[1].length() - 3) + " " + data[1].substring(data[1].length() - 3);
                holder.frequency.setText(freqFormat);
            } catch (ArrayIndexOutOfBoundsException e) {
                holder.frequency.setText(description);
            }
            // 番組名
            holder.title.setText(TunerContract.FavoriteContract.HdRadio.getName(cursor));
        }
    }

    static class ViewHolder {
        @BindView(R.id.band_text) TextView band;
        @BindView(R.id.frequency_text) TextView frequency;
        @BindView(R.id.title_text) TextView title;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
