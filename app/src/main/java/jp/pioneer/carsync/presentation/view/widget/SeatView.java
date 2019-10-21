package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.pioneer.carsync.R;
import timber.log.Timber;

/**
 * Advanced Setting画面の座席ボタン
 * Created by tsuyosh on 16/04/27.
 */
public class SeatView extends RelativeLayout {
    public static final int AUDIO_OUTPUT_MODE_STANDARD = 0;
    public static final int AUDIO_OUTPUT_MODE_NETWORK = 1;

    public static final int LISTENING_POSITION_OFF = 0;
    public static final int LISTENING_POSITION_FRONT_LEFT = 1;
    public static final int LISTENING_POSITION_FRONT_RIGHT = 2;
    public static final int LISTENING_POSITION_FRONT = 3;
    public static final int LISTENING_POSITION_ALL = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({AUDIO_OUTPUT_MODE_STANDARD, AUDIO_OUTPUT_MODE_NETWORK})
    @interface AudioOutputMode {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LISTENING_POSITION_OFF, LISTENING_POSITION_FRONT_LEFT, LISTENING_POSITION_FRONT_RIGHT,
            LISTENING_POSITION_FRONT, LISTENING_POSITION_ALL})
    @interface ListeningPosition {}

    private int mAudioOutputMode;
    private int mListeningPosition;
    private ColorStateList mSeatBuckgroundTint;
    @BindView(R.id.flSeatIcon)
    RelativeLayout flSeatIcon;

    @BindView(R.id.frSeatIcon)
    RelativeLayout frSeatIcon;

    @BindView(R.id.rearSeatIcon)
    RelativeLayout rearSeatIcon;

    public SeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public SeatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeatView(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeatView, defStyleAttr, 0);
        mAudioOutputMode = a.getInt(R.styleable.SeatView_audioOutputMode, AUDIO_OUTPUT_MODE_STANDARD);
        mListeningPosition = a.getInt(R.styleable.SeatView_listeningPosition, LISTENING_POSITION_OFF);
        mSeatBuckgroundTint = a.getColorStateList(R.styleable.SeatView_seatBackgroundTint);
        a.recycle();

        int layoutId;
        if (mAudioOutputMode == AUDIO_OUTPUT_MODE_STANDARD) {
            layoutId = R.layout.widget_seat_std;
        } else {
            layoutId = R.layout.widget_seat_nw;
        }
        View view = inflate(context, layoutId, this);
        ButterKnife.bind(this, view);
        applyListeningPosition();
    }

    public void setListeningPosition(@ListeningPosition int position) {
        if (mListeningPosition == position) return;
        mListeningPosition = position;
        applyListeningPosition();
    }

    private void applyListeningPosition() {
        switch (mListeningPosition) {
            case LISTENING_POSITION_OFF:
                flSeatIcon.getChildAt(0).setVisibility(View.VISIBLE);
                flSeatIcon.getChildAt(1).setVisibility(View.GONE);
                flSeatIcon.getChildAt(2).setVisibility(View.GONE);
                frSeatIcon.getChildAt(0).setVisibility(View.VISIBLE);
                frSeatIcon.getChildAt(1).setVisibility(View.GONE);
                frSeatIcon.getChildAt(2).setVisibility(View.GONE);
                rearSeatIcon.getChildAt(0).setVisibility(View.VISIBLE);
                rearSeatIcon.getChildAt(1).setVisibility(View.GONE);
                rearSeatIcon.getChildAt(2).setVisibility(View.GONE);
                break;
            case LISTENING_POSITION_FRONT_LEFT:
                flSeatIcon.getChildAt(0).setVisibility(View.GONE);
                flSeatIcon.getChildAt(1).setVisibility(View.VISIBLE);
                flSeatIcon.getChildAt(2).setVisibility(View.VISIBLE);
                frSeatIcon.getChildAt(0).setVisibility(View.VISIBLE);
                frSeatIcon.getChildAt(1).setVisibility(View.GONE);
                frSeatIcon.getChildAt(2).setVisibility(View.GONE);
                rearSeatIcon.getChildAt(0).setVisibility(View.VISIBLE);
                rearSeatIcon.getChildAt(1).setVisibility(View.GONE);
                rearSeatIcon.getChildAt(2).setVisibility(View.GONE);
                break;
            case LISTENING_POSITION_FRONT_RIGHT:
                flSeatIcon.getChildAt(0).setVisibility(View.VISIBLE);
                flSeatIcon.getChildAt(1).setVisibility(View.GONE);
                flSeatIcon.getChildAt(2).setVisibility(View.GONE);
                frSeatIcon.getChildAt(0).setVisibility(View.GONE);
                frSeatIcon.getChildAt(1).setVisibility(View.VISIBLE);
                frSeatIcon.getChildAt(2).setVisibility(View.VISIBLE);
                rearSeatIcon.getChildAt(0).setVisibility(View.VISIBLE);
                rearSeatIcon.getChildAt(1).setVisibility(View.GONE);
                rearSeatIcon.getChildAt(2).setVisibility(View.GONE);
                break;
            case LISTENING_POSITION_FRONT:
                flSeatIcon.getChildAt(0).setVisibility(View.GONE);
                flSeatIcon.getChildAt(1).setVisibility(View.VISIBLE);
                flSeatIcon.getChildAt(2).setVisibility(View.VISIBLE);
                frSeatIcon.getChildAt(0).setVisibility(View.GONE);
                frSeatIcon.getChildAt(1).setVisibility(View.VISIBLE);
                frSeatIcon.getChildAt(2).setVisibility(View.VISIBLE);
                rearSeatIcon.getChildAt(0).setVisibility(View.VISIBLE);
                rearSeatIcon.getChildAt(1).setVisibility(View.GONE);
                rearSeatIcon.getChildAt(2).setVisibility(View.GONE);
                break;
            case LISTENING_POSITION_ALL:
                flSeatIcon.getChildAt(0).setVisibility(View.GONE);
                flSeatIcon.getChildAt(1).setVisibility(View.VISIBLE);
                flSeatIcon.getChildAt(2).setVisibility(View.VISIBLE);
                frSeatIcon.getChildAt(0).setVisibility(View.GONE);
                frSeatIcon.getChildAt(1).setVisibility(View.VISIBLE);
                frSeatIcon.getChildAt(2).setVisibility(View.VISIBLE);
                rearSeatIcon.getChildAt(0).setVisibility(View.GONE);
                rearSeatIcon.getChildAt(1).setVisibility(View.VISIBLE);
                rearSeatIcon.getChildAt(2).setVisibility(View.VISIBLE);
                break;
            default:
                Timber.w("This case is impossible.");
                break;
        }

/*        flSeatIcon.setOn(
                mListeningPosition == LISTENING_POSITION_FRONT_LEFT
                        || mListeningPosition == LISTENING_POSITION_FRONT
                        || mListeningPosition == LISTENING_POSITION_ALL
        );
        frSeatIcon.setOn(
                mListeningPosition == LISTENING_POSITION_FRONT_RIGHT
                        || mListeningPosition == LISTENING_POSITION_FRONT
                        || mListeningPosition == LISTENING_POSITION_ALL
        );
        rearSeatIcon.setOn(
                mListeningPosition == LISTENING_POSITION_ALL
        );*/
    }

}
