package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.ParkingSensorErrorStatus;
import jp.pioneer.carsync.domain.model.ParkingSensorStatus;
import jp.pioneer.carsync.domain.model.SensorDistanceUnit;
import jp.pioneer.carsync.domain.model.SensorStatus;
import jp.pioneer.carsync.presentation.presenter.ParkingSensorDialogPresenter;
import jp.pioneer.carsync.presentation.view.ParkingSensorDialogView;
import timber.log.Timber;

/**
 * パーキングセンサー画面
 */

public class ParkingSensorDialogFragment extends AbstractDialogFragment<ParkingSensorDialogPresenter,
        ParkingSensorDialogView, ParkingSensorDialogFragment.Callback> implements ParkingSensorDialogView {
    private static final int SENSOR_DISTANCE_RANGE1 = 2; //20cm
    private static final int SENSOR_DISTANCE_RANGE2 = 4; //40cm
    private static final int SENSOR_DISTANCE_RANGE3 = 6; //60cm
    private static final int SENSOR_DISTANCE_RANGE4 = 9; //90cm
    private static final int SENSOR_DISTANCE_RANGE5 = 16; //160cm
    private static final int SENSOR_DISTANCE_RANGE6 = 25; //250cm
    private static final int SENSOR_DISTANCE_RANGE1_INCH = 10; //10in
    private static final int SENSOR_DISTANCE_RANGE2_INCH = 20; //20in
    private static final int SENSOR_DISTANCE_RANGE3_INCH = 30; //30in
    private static final int SENSOR_DISTANCE_RANGE4_INCH = 40; //40in
    private static final int SENSOR_DISTANCE_RANGE5_INCH = 60; //60in
    private static final int SENSOR_DISTANCE_RANGE6_INCH = 100; //100in
    @Inject ParkingSensorDialogPresenter mPresenter;
    @BindView(R.id.base_image) ImageView mBaseImage;
    @BindView(R.id.distance_text) TextView mTextDistance;
    @BindView(R.id.distance_unit_text) TextView mTextDistanceUnit;
    @BindView(R.id.sensor_a_1) ImageView mSensorA1;
    @BindView(R.id.sensor_a_2) ImageView mSensorA2;
    @BindView(R.id.sensor_a_3) ImageView mSensorA3;
    @BindView(R.id.sensor_a_4) ImageView mSensorA4;
    @BindView(R.id.sensor_a_5) ImageView mSensorA5;
    @BindView(R.id.sensor_a_6) ImageView mSensorA6;
    @BindView(R.id.sensor_b_1) ImageView mSensorB1;
    @BindView(R.id.sensor_b_2) ImageView mSensorB2;
    @BindView(R.id.sensor_b_3) ImageView mSensorB3;
    @BindView(R.id.sensor_b_4) ImageView mSensorB4;
    @BindView(R.id.sensor_b_5) ImageView mSensorB5;
    @BindView(R.id.sensor_b_6) ImageView mSensorB6;
    @BindView(R.id.sensor_c_1) ImageView mSensorC1;
    @BindView(R.id.sensor_c_2) ImageView mSensorC2;
    @BindView(R.id.sensor_c_3) ImageView mSensorC3;
    @BindView(R.id.sensor_c_4) ImageView mSensorC4;
    @BindView(R.id.sensor_c_5) ImageView mSensorC5;
    @BindView(R.id.sensor_c_6) ImageView mSensorC6;
    @BindView(R.id.sensor_d_1) ImageView mSensorD1;
    @BindView(R.id.sensor_d_2) ImageView mSensorD2;
    @BindView(R.id.sensor_d_3) ImageView mSensorD3;
    @BindView(R.id.sensor_d_4) ImageView mSensorD4;
    @BindView(R.id.sensor_d_5) ImageView mSensorD5;
    @BindView(R.id.sensor_d_6) ImageView mSensorD6;
    @BindView(R.id.error_layout) ConstraintLayout mErrorLayout;
    @BindView(R.id.error_title) TextView mErrorTitle;
    @BindView(R.id.sensor_a_icon) ImageView mSensorA;
    @BindView(R.id.sensor_b_icon) ImageView mSensorB;
    @BindView(R.id.sensor_c_icon) ImageView mSensorC;
    @BindView(R.id.sensor_d_icon) ImageView mSensorD;
    @BindView(R.id.item_ok) LinearLayout mItemOk;
    @BindView(R.id.item_error) LinearLayout mItemError;
    @BindView(R.id.item_no_data) LinearLayout mItemNoData;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public ParkingSensorDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return ParkingSensorDialogFragment
     */
    public static ParkingSensorDialogFragment newInstance(Fragment target, Bundle args) {
        ParkingSensorDialogFragment fragment = new ParkingSensorDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setCancelable(false);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.BehindScreenStyle);
        View view = inflater.inflate(R.layout.fragment_dialog_parking_sensor, null, false);
        mUnbinder = ButterKnife.bind(this, view);

        builder.setView(view);
        setCancelable(false);
        mErrorLayout.setVisibility(View.GONE);
        return builder.create();
    }


    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected ParkingSensorDialogPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        mUnbinder.unbind();
    }

    private void init() {
        mSensorA1.setAlpha(0.0f);
        mSensorA2.setAlpha(0.0f);
        mSensorA3.setAlpha(0.0f);
        mSensorA4.setAlpha(0.0f);
        mSensorA5.setAlpha(0.0f);
        mSensorA6.setAlpha(0.0f);
        mSensorB1.setAlpha(0.0f);
        mSensorB2.setAlpha(0.0f);
        mSensorB3.setAlpha(0.0f);
        mSensorB4.setAlpha(0.0f);
        mSensorB5.setAlpha(0.0f);
        mSensorB6.setAlpha(0.0f);
        mSensorC1.setAlpha(0.0f);
        mSensorC2.setAlpha(0.0f);
        mSensorC3.setAlpha(0.0f);
        mSensorC4.setAlpha(0.0f);
        mSensorC5.setAlpha(0.0f);
        mSensorC6.setAlpha(0.0f);
        mSensorD1.setAlpha(0.0f);
        mSensorD2.setAlpha(0.0f);
        mSensorD3.setAlpha(0.0f);
        mSensorD4.setAlpha(0.0f);
        mSensorD5.setAlpha(0.0f);
        mSensorD6.setAlpha(0.0f);

    }

    @Override
    public void setSensor(ParkingSensorStatus status) {
        init();
        int sensorDistanceA = status.sensorDistanceA;
        int sensorDistanceB = status.sensorDistanceB;
        int sensorDistanceC = status.sensorDistanceC;
        int sensorDistanceD = status.sensorDistanceD;
/*        sensorDistanceA = 5;
        sensorDistanceB = 6;
        sensorDistanceC = 10;
        sensorDistanceD = 12;
        status.sensorStatusA = SensorStatus.NORMAL;
        status.sensorStatusB = SensorStatus.NORMAL;
        status.sensorStatusC = SensorStatus.NORMAL;
        status.sensorStatusD = SensorStatus.NORMAL; */
        //status.sensorStatusA = SensorStatus.ERROR;
        //status.errorStatus = ParkingSensorErrorStatus.NO_DATA_ERROR;

        if(status.errorStatus == ParkingSensorErrorStatus.NO_DATA_ERROR){
            mErrorLayout.setVisibility(View.VISIBLE);
            mErrorTitle.setText(R.string.pas_011);
            mSensorA.setImageResource(R.drawable.p1215_nodata_pserror);
            mSensorB.setImageResource(R.drawable.p1215_nodata_pserror);
            mSensorC.setImageResource(R.drawable.p1215_nodata_pserror);
            mSensorD.setImageResource(R.drawable.p1215_nodata_pserror);
            mItemOk.setVisibility(View.GONE);
            mItemError.setVisibility(View.GONE);
            mItemNoData.setVisibility(View.VISIBLE);
            return;
        }

        if(status.sensorStatusA == SensorStatus.ERROR||status.sensorStatusB == SensorStatus.ERROR
                ||status.sensorStatusC == SensorStatus.ERROR||status.sensorStatusD == SensorStatus.ERROR){
            mErrorLayout.setVisibility(View.VISIBLE);
            mErrorTitle.setText(R.string.pas_004);
            if(status.sensorStatusA==SensorStatus.ERROR) {
                mSensorA.setImageResource(R.drawable.p1213_error_pserror);
            }else{
                mSensorA.setImageResource(R.drawable.p1212_ok_pserror);
            }
            if(status.sensorStatusB==SensorStatus.ERROR) {
                mSensorB.setImageResource(R.drawable.p1213_error_pserror);
            }else{
                mSensorB.setImageResource(R.drawable.p1212_ok_pserror);
            }
            if(status.sensorStatusC==SensorStatus.ERROR) {
                mSensorC.setImageResource(R.drawable.p1213_error_pserror);
            }else{
                mSensorC.setImageResource(R.drawable.p1212_ok_pserror);
            }
            if(status.sensorStatusD==SensorStatus.ERROR) {
                mSensorD.setImageResource(R.drawable.p1213_error_pserror);
            }else{
                mSensorD.setImageResource(R.drawable.p1212_ok_pserror);
            }
            mItemOk.setVisibility(View.VISIBLE);
            mItemError.setVisibility(View.VISIBLE);
            mItemNoData.setVisibility(View.GONE);
            return;
        }
        mErrorLayout.setVisibility(View.GONE);
        int range1,range2,range3,range4,range5,range6;
        if (status.sensorDistanceUnit == SensorDistanceUnit._1INCH) {
            range1=SENSOR_DISTANCE_RANGE1_INCH;
            range2=SENSOR_DISTANCE_RANGE2_INCH;
            range3=SENSOR_DISTANCE_RANGE3_INCH;
            range4=SENSOR_DISTANCE_RANGE4_INCH;
            range5=SENSOR_DISTANCE_RANGE5_INCH;
            range6=SENSOR_DISTANCE_RANGE6_INCH;
        }else{
            range1=SENSOR_DISTANCE_RANGE1;
            range2=SENSOR_DISTANCE_RANGE2;
            range3=SENSOR_DISTANCE_RANGE3;
            range4=SENSOR_DISTANCE_RANGE4;
            range5=SENSOR_DISTANCE_RANGE5;
            range6=SENSOR_DISTANCE_RANGE6;
        }
        if (status.sensorStatusA == SensorStatus.NORMAL) {
            if (sensorDistanceA >= 0 && sensorDistanceA < range1) {
                mSensorA1.setAlpha(1.0f);
                mSensorA2.setAlpha(0.0f);
                mSensorA3.setAlpha(0.0f);
                mSensorA4.setAlpha(0.0f);
                mSensorA5.setAlpha(0.0f);
                mSensorA6.setAlpha(0.0f);
            } else if (sensorDistanceA >= range1 && sensorDistanceA < range2) {
                mSensorA1.setAlpha(0.3f);
                mSensorA2.setAlpha(1.0f);
                mSensorA3.setAlpha(0.0f);
                mSensorA4.setAlpha(0.0f);
                mSensorA5.setAlpha(0.0f);
                mSensorA6.setAlpha(0.0f);
            } else if (sensorDistanceA >= range2 && sensorDistanceA < range3) {
                mSensorA1.setAlpha(0.3f);
                mSensorA2.setAlpha(0.3f);
                mSensorA3.setAlpha(1.0f);
                mSensorA4.setAlpha(0.0f);
                mSensorA5.setAlpha(0.0f);
                mSensorA6.setAlpha(0.0f);
            } else if (sensorDistanceA >= range3 && sensorDistanceA < range4) {
                mSensorA1.setAlpha(0.3f);
                mSensorA2.setAlpha(0.3f);
                mSensorA3.setAlpha(0.3f);
                mSensorA4.setAlpha(1.0f);
                mSensorA5.setAlpha(0.0f);
                mSensorA6.setAlpha(0.0f);
            } else if (sensorDistanceA >= range4 && sensorDistanceA < range5) {
                mSensorA1.setAlpha(0.3f);
                mSensorA2.setAlpha(0.3f);
                mSensorA3.setAlpha(0.3f);
                mSensorA4.setAlpha(0.3f);
                mSensorA5.setAlpha(1.0f);
                mSensorA6.setAlpha(0.0f);
            } else if (sensorDistanceA >= range5) {
                mSensorA1.setAlpha(0.3f);
                mSensorA2.setAlpha(0.3f);
                mSensorA3.setAlpha(0.3f);
                mSensorA4.setAlpha(0.3f);
                mSensorA5.setAlpha(0.3f);
                mSensorA6.setAlpha(1.0f);
            }
        }
        if (status.sensorStatusB == SensorStatus.NORMAL) {
            if (sensorDistanceB >= 0 && sensorDistanceB <= range1) {
                mSensorB1.setAlpha(1.0f);
                mSensorB2.setAlpha(0.0f);
                mSensorB3.setAlpha(0.0f);
                mSensorB4.setAlpha(0.0f);
                mSensorB5.setAlpha(0.0f);
                mSensorB6.setAlpha(0.0f);
            } else if (sensorDistanceB >= range1 && sensorDistanceB < range2) {
                mSensorB1.setAlpha(0.3f);
                mSensorB2.setAlpha(1.0f);
                mSensorB3.setAlpha(0.0f);
                mSensorB4.setAlpha(0.0f);
                mSensorB5.setAlpha(0.0f);
                mSensorB6.setAlpha(0.0f);
            } else if (sensorDistanceB >= range2 && sensorDistanceB < range3) {
                mSensorB1.setAlpha(0.3f);
                mSensorB2.setAlpha(0.3f);
                mSensorB3.setAlpha(1.0f);
                mSensorB4.setAlpha(0.0f);
                mSensorB5.setAlpha(0.0f);
                mSensorB6.setAlpha(0.0f);
            } else if (sensorDistanceB >= range3 && sensorDistanceB < range4) {
                mSensorB1.setAlpha(0.3f);
                mSensorB2.setAlpha(0.3f);
                mSensorB3.setAlpha(0.3f);
                mSensorB4.setAlpha(1.0f);
                mSensorB5.setAlpha(0.0f);
                mSensorB6.setAlpha(0.0f);
            } else if (sensorDistanceB >= range4 && sensorDistanceB < range5) {
                mSensorB1.setAlpha(0.3f);
                mSensorB2.setAlpha(0.3f);
                mSensorB3.setAlpha(0.3f);
                mSensorB4.setAlpha(0.3f);
                mSensorB5.setAlpha(1.0f);
                mSensorB6.setAlpha(0.0f);
            } else if (sensorDistanceB >= range5) {
                mSensorB1.setAlpha(0.3f);
                mSensorB2.setAlpha(0.3f);
                mSensorB3.setAlpha(0.3f);
                mSensorB4.setAlpha(0.3f);
                mSensorB5.setAlpha(0.3f);
                mSensorB6.setAlpha(1.0f);
            }
        }
        if (status.sensorStatusC == SensorStatus.NORMAL) {
            if (sensorDistanceC >= 0 && sensorDistanceC <= range1) {
                mSensorC1.setAlpha(1.0f);
                mSensorC2.setAlpha(0.0f);
                mSensorC3.setAlpha(0.0f);
                mSensorC4.setAlpha(0.0f);
                mSensorC5.setAlpha(0.0f);
                mSensorC6.setAlpha(0.0f);
            } else if (sensorDistanceC >= range1 && sensorDistanceC < range2) {
                mSensorC1.setAlpha(0.3f);
                mSensorC2.setAlpha(1.0f);
                mSensorC3.setAlpha(0.0f);
                mSensorC4.setAlpha(0.0f);
                mSensorC5.setAlpha(0.0f);
                mSensorC6.setAlpha(0.0f);
            } else if (sensorDistanceC >= range2 && sensorDistanceC < range3) {
                mSensorC1.setAlpha(0.3f);
                mSensorC2.setAlpha(0.3f);
                mSensorC3.setAlpha(1.0f);
                mSensorC4.setAlpha(0.0f);
                mSensorC5.setAlpha(0.0f);
                mSensorC6.setAlpha(0.0f);
            } else if (sensorDistanceC >= range3 && sensorDistanceC < range4) {
                mSensorC1.setAlpha(0.3f);
                mSensorC2.setAlpha(0.3f);
                mSensorC3.setAlpha(0.3f);
                mSensorC4.setAlpha(1.0f);
                mSensorC5.setAlpha(0.0f);
                mSensorC6.setAlpha(0.0f);
            } else if (sensorDistanceC >= range4 && sensorDistanceC < range5) {
                mSensorC1.setAlpha(0.3f);
                mSensorC2.setAlpha(0.3f);
                mSensorC3.setAlpha(0.3f);
                mSensorC4.setAlpha(0.3f);
                mSensorC5.setAlpha(1.0f);
                mSensorC6.setAlpha(0.0f);
            } else if (sensorDistanceC >= range5) {
                mSensorC1.setAlpha(0.3f);
                mSensorC2.setAlpha(0.3f);
                mSensorC3.setAlpha(0.3f);
                mSensorC4.setAlpha(0.3f);
                mSensorC5.setAlpha(0.3f);
                mSensorC6.setAlpha(1.0f);
            }
        }

        if (status.sensorStatusD == SensorStatus.NORMAL) {
            if (sensorDistanceD >= 0 && sensorDistanceD <= range1) {
                mSensorD1.setAlpha(1.0f);
                mSensorD2.setAlpha(0.0f);
                mSensorD3.setAlpha(0.0f);
                mSensorD4.setAlpha(0.0f);
                mSensorD5.setAlpha(0.0f);
                mSensorD6.setAlpha(0.0f);
            } else if (sensorDistanceD >= range1 && sensorDistanceD < range2) {
                mSensorD1.setAlpha(0.3f);
                mSensorD2.setAlpha(1.0f);
                mSensorD3.setAlpha(0.0f);
                mSensorD4.setAlpha(0.0f);
                mSensorD5.setAlpha(0.0f);
                mSensorD6.setAlpha(0.0f);
            } else if (sensorDistanceD >= range2 && sensorDistanceD < range3) {
                mSensorD1.setAlpha(0.3f);
                mSensorD2.setAlpha(0.3f);
                mSensorD3.setAlpha(1.0f);
                mSensorD4.setAlpha(0.0f);
                mSensorD5.setAlpha(0.0f);
                mSensorD6.setAlpha(0.0f);
            } else if (sensorDistanceD >= range3 && sensorDistanceD < range4) {
                mSensorD1.setAlpha(0.3f);
                mSensorD2.setAlpha(0.3f);
                mSensorD3.setAlpha(0.3f);
                mSensorD4.setAlpha(1.0f);
                mSensorD5.setAlpha(0.0f);
                mSensorD6.setAlpha(0.0f);
            } else if (sensorDistanceD >= range4 && sensorDistanceD < range5) {
                mSensorD1.setAlpha(0.3f);
                mSensorD2.setAlpha(0.3f);
                mSensorD3.setAlpha(0.3f);
                mSensorD4.setAlpha(0.3f);
                mSensorD5.setAlpha(1.0f);
                mSensorD6.setAlpha(0.0f);
            } else if (sensorDistanceD >= range5) {
                mSensorD1.setAlpha(0.3f);
                mSensorD2.setAlpha(0.3f);
                mSensorD3.setAlpha(0.3f);
                mSensorD4.setAlpha(0.3f);
                mSensorD5.setAlpha(0.3f);
                mSensorD6.setAlpha(1.0f);
            }
        }
        Timber.d("sensorDistanceA:%d, sensorDistanceB:%d, sensorDistanceC:%d, sensorDistanceD:%d",sensorDistanceA,sensorDistanceB,sensorDistanceC,sensorDistanceD);
        ArrayList<Integer> values = new ArrayList<>();
        if (status.sensorStatusA == SensorStatus.NORMAL) {
            values.add(sensorDistanceA);
        }
        if (status.sensorStatusB == SensorStatus.NORMAL) {
            values.add(sensorDistanceB);
        }
        if (status.sensorStatusC == SensorStatus.NORMAL) {
            values.add(sensorDistanceC);
        }
        if (status.sensorStatusD == SensorStatus.NORMAL) {
            values.add(sensorDistanceD);
        }
        int min = values.size() == 0 ? -1 : values.get(0);
        //A~D距離の最小値を求める
        for (int index = 1; index < values.size(); index++) {
            min = Math.min(min, values.get(index));
        }

        String distanceText = "";
        String unit = "";
        if (min != -1) {

            if (status.sensorDistanceUnit == SensorDistanceUnit._1INCH) {
                distanceText = String.valueOf(min);
                unit = getString(R.string.pas_003);
            } else {
                float minF = (float) min / 10;
                distanceText = String.format(Locale.US, "%.1f", minF);
                unit = getString(R.string.pas_002);
            }
            if(min>= 0 && min < range2){
                mTextDistance.setTextColor(ContextCompat.getColor(getContext(),R.color.parking_sensor_color_red));
                mTextDistanceUnit.setTextColor(ContextCompat.getColor(getContext(),R.color.parking_sensor_color_red));
            }else if(min >= range2 && min < range4){
                mTextDistance.setTextColor(ContextCompat.getColor(getContext(),R.color.parking_sensor_color_orange));
                mTextDistanceUnit.setTextColor(ContextCompat.getColor(getContext(),R.color.parking_sensor_color_orange));
            }else if(min >= range4 && min < range5){
                mTextDistance.setTextColor(ContextCompat.getColor(getContext(),R.color.parking_sensor_color_yellow));
                mTextDistanceUnit.setTextColor(ContextCompat.getColor(getContext(),R.color.parking_sensor_color_yellow));
            }else if(min >= range5 && min <= range6){
                mTextDistance.setTextColor(ContextCompat.getColor(getContext(),R.color.parking_sensor_color_lime));
                mTextDistanceUnit.setTextColor(ContextCompat.getColor(getContext(),R.color.parking_sensor_color_lime));
            }else{
                distanceText ="";
                unit = "";
            }

        }
        mTextDistance.setText(distanceText);
        mTextDistanceUnit.setText(unit);
    }

    @Override
    public void dismissDialog() {
        this.dismiss();
    }

    /**
     * ダイアログ終了通知interface
     */
    public interface Callback {
        /**
         * ダイアログ終了通知
         *
         * @param fragment 終了ダイアログ
         */
        void onClose(ParkingSensorDialogFragment fragment);
    }
}
