package jp.pioneer.carsync.presentation.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QueryTunerItem;
import jp.pioneer.carsync.domain.interactor.SelectDabFavorite;
import jp.pioneer.carsync.domain.interactor.SelectHdRadioFavorite;
import jp.pioneer.carsync.domain.interactor.SelectRadioFavorite;
import jp.pioneer.carsync.domain.interactor.SelectSiriusXmFavorite;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.TunerSeekStep;
import jp.pioneer.carsync.presentation.view.RadioFavoriteView;

/**
 * ラジオお気に入りリストのpresenter
 */
@PresenterLifeCycle
public class RadioFavoritePresenter extends Presenter<RadioFavoriteView> implements LoaderManager.LoaderCallbacks<Cursor> {

    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetCase;
    @Inject QueryTunerItem mTunerCase;
    @Inject SelectRadioFavorite mRadioCase;
    @Inject SelectSiriusXmFavorite mSxmCase;
    @Inject SelectDabFavorite mDabCase;
    @Inject SelectHdRadioFavorite mHdRadioCase;
    private LoaderManager mLoaderManager;

    /**
     * コンストラクタ
     */
    @Inject
    public RadioFavoritePresenter() {
    }

    @Override
    void onInitialize() {
    }

    /**
     * LoaderManager登録
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(0, Bundle.EMPTY, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        MediaSourceType type = mGetCase.execute().getCarDeviceStatus().sourceType;
        if (type == MediaSourceType.RADIO) {
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createRadio());
        } else if (type == MediaSourceType.SIRIUS_XM) {
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createSiriusXm());
        } else if (type == MediaSourceType.DAB) {
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createDab());
        } else if (type == MediaSourceType.HD_RADIO) {
            return mTunerCase.getFavoriteList(TunerContract.FavoriteContract.QueryParamsBuilder.createHdRadio());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setCursor(data, Bundle.EMPTY));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setCursor(null, Bundle.EMPTY));
    }

    /**
     * お気に入り選択ハンドラ
     *
     * @param cursor 選択項目のカーソル
     */
    public void onSelectFavoriteAction(Cursor cursor) {
        MediaSourceType type = mGetCase.execute().getCarDeviceStatus().sourceType;
        if (type == MediaSourceType.RADIO) {
            mRadioCase.execute(
                    TunerContract.FavoriteContract.Radio.getIndex(cursor),
                    TunerContract.FavoriteContract.Radio.getBandType(cursor),
                    TunerContract.FavoriteContract.Radio.getPi(cursor));
        } else if (type == MediaSourceType.SIRIUS_XM) {
            mSxmCase.execute(
                    TunerContract.FavoriteContract.SiriusXm.getChannelNo(cursor),
                    TunerContract.FavoriteContract.SiriusXm.getBandType(cursor),
                    TunerContract.FavoriteContract.SiriusXm.getSid(cursor));
        } else if (type == MediaSourceType.DAB) {
            mDabCase.selectFavorite(
                    TunerContract.FavoriteContract.Dab.getIndex(cursor),
                    TunerContract.FavoriteContract.Dab.getBandType(cursor),
                    TunerContract.FavoriteContract.Dab.getEid(cursor),
                    TunerContract.FavoriteContract.Dab.getSid(cursor),
                    TunerContract.FavoriteContract.Dab.getScids(cursor));
        }else if (type == MediaSourceType.HD_RADIO) {
            mHdRadioCase.selectFavorite(
                    TunerContract.FavoriteContract.HdRadio.getIndex(cursor),
                    TunerContract.FavoriteContract.HdRadio.getBandType(cursor),
                    TunerContract.FavoriteContract.HdRadio.getMulticastChNumber(cursor));
        }
    }

    public MediaSourceType getSourceType(){
        return mGetCase.execute().getCarDeviceStatus().sourceType;
    }

    public TunerSeekStep getTunerSeekStep(){
        CarDeviceStatus status = mGetCase.execute().getCarDeviceStatus();
        return status.seekStep;
    }
}
