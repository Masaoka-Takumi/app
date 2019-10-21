package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.v4.content.CursorLoader;

import javax.inject.Inject;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;

/**
 * USBリスト生成.
 * <p>
 * 画面表示に必要なアイテムの取得は{@link #addWantedListItemIndex(int)}を使用し、
 * 欲しいアイテムのインデックスを追加していく。
 * アイテムが取得できた場合はCursorが更新される。
 * <p>
 * 画面表示に必要なくなったアイテムのインデックスは{@link #removeWantedListItemIndex(int)}を使用し、
 * 余計なアイテムを取得しないようにする。
 */
public class CreateUsbList {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject CarDeviceMediaRepository mCarDeviceMediaRepository;

    /**
     * コンストラクタ
     */
    @Inject
    public CreateUsbList() {
    }

    /**
     * 実行.
     *
     * @return {@link CursorLoader}
     */
    public CursorLoader execute() {
        return mCarDeviceMediaRepository.getUsbList();
    }

    /**
     * リストアイテムインデックス追加.
     *
     * @param index 欲しいアイテムのindex
     */
    public void addWantedListItemIndex(int index){
        mCarDeviceMediaRepository.addWantedUsbListItemIndex(index);
    }

    /**
     * リストアイテムインデックス削除.
     *
     * @param index 必要なくなったアイテムのindex
     */
    public void removeWantedListItemIndex(int index){
        mCarDeviceMediaRepository.removeWantedUsbListItemIndex(index);
    }
}
