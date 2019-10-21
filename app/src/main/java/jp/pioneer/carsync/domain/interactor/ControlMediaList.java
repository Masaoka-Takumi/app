package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.MediaListController;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.event.RotaryKeyEvent;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubDisplayInfo;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * メディアリスト制御.
 */
public class ControlMediaList {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject MediaListController mMediaListController;
    @Inject CarDeviceMediaRepository mCarDeviceMediaRepository;

    /**
     * コンストラクタ.
     */
    @Inject
    public ControlMediaList() {
    }

    /**
     * リスト入場.
     * <p>
     * リスト画面に遷移する場合に呼び出す。
     * 車載機のリスト状態と連携するため、連絡先リストのような車載機にないリストは呼び出し不要。（そもそも
     * リスト種別がない）<br>
     * お気に入りが同じ画面に含まれる（タブで切り替える）場合、タブを切り替える度に入退場を行う必要はないが、
     * 車載機はリスト状態のままのためロータリーキー操作によりフォーカス位置（{@link ListInfo#focusListIndex}）が
     * 変わったり、ロータリーキーイベント（{@link RotaryKeyEvent}）が発行されることに注意する。<br>
     * ABCサーチリストの状態から下階層に遷移する場合も呼び出すこと。その際に指定する{@code listType}は
     * 遷移後のリスト種別（{@link ListType#LIST}等）で、リスト退場を挟む必要はない。
     * ABCサーチリストに対応していないリストにおいて、リスト種別がABCサーチリストになった（車載機操作で
     * ABCサーチリストモードにした）場合、本メソッドを呼び出しキャンセルする（{@link ListType#LIST}
     * 等に遷移する）こと。
     *
     * @param listType 遷移するリスト種別
     * @throws NullPointerException {@code listType}がnull
     * @throws IllegalArgumentException {@code listType}が現在のソース種別で対応していない
     */
    public void enterList(@NonNull ListType listType) {
        checkArgument(checkNotNull(listType).types.contains(mStatusHolder.getCarDeviceStatus().sourceType));

        MediaSourceType sourceType = mStatusHolder.getCarDeviceStatus().sourceType;
        mHandler.post(() -> {
            if (sourceType == mStatusHolder.getCarDeviceStatus().sourceType) {
                mMediaListController.enterList(listType);
            } else {
                Timber.w("enterList() Source changed.");
            }
        });
    }

    /**
     * リスト退場.
     * <p>
     * リスト画面を終了する場合に呼び出す。連絡先リストのようにリスト入場を行わないものは呼び出し不要。
     */
    public void exitList() {
        MediaSourceType sourceType = mStatusHolder.getCarDeviceStatus().sourceType;
        mHandler.post(() -> {
            if (sourceType == mStatusHolder.getCarDeviceStatus().sourceType) {
                mMediaListController.exitList();
            } else {
                Timber.w("exitList() Source changed.");
            }
        });
    }

    /**
     * 選択リスト情報通知.
     * <p>
     * {@link MediaSourceType#APP_MUSIC}ソースのリスト表示中に、フォーカスの当たっているリスト項目や
     * タブの表示情報を車載機に通知する。
     * 呼び出す契機は以下を想定。
     * <ul>
     *     <li>リスト種別がリスト状態（{@link ListType#LIST} or {@link ListType#ABC_SEARCH_LIST}）になった</li>
     *     <li>カテゴリ（タブ）が変わった</li>
     *     <li>リスト項目のフォーカスが変わった</li>
     *     <li>ABCサーチのインデックスが変わった</li>
     * </ul>
     * 通知する情報（{@code text}に指定する内容は以下を想定。
     * <pre>
     * リスト種別が{@link ListType#LIST}の場合:
     *  カテゴリ名（{@link SubDisplayInfo#label}）名　※ロータリー操作でタブが切り替え出来る場合
     *  フォーカス項目の情報（曲名やアルバム名、アーティスト名等カテゴリに応じたもの）
     *  フォーカスが無い場合は空文字列
     *  項目が無い場合は”No Song"といった無いことを示す文字列
     * </pre>
     * <pre>
     * リスト種別が{@link ListType#ABC_SEARCH_LIST}の場合:
     *  ABCサーチのインデックス
     * </pre>
     *
     * @param hasParent 上方向の階層を持つか否か
     * @param hasChild 下方向の階層を持つか否か
     * @param currentPosition リスト階層数
     * @param subDisplayInfo 車載機のサブディスプレイに表示する現在のカテゴリ情報
     * @param text 車載機側の画面に表示する文字列
     * @throws NullPointerException {@code subDisplayInfo}、または、{@code text}がnull
     * @throws IllegalStateException 現在のソースが{@link MediaSourceType#APP_MUSIC}ではない
     */
    public void notifySelectedListInfo(boolean hasParent, boolean hasChild, int currentPosition,
                                       @NonNull SubDisplayInfo subDisplayInfo, @NonNull String text) {
        checkNotNull(subDisplayInfo);
        checkNotNull(text);

        MediaSourceType sourceType = mStatusHolder.getCarDeviceStatus().sourceType;
        checkState(sourceType == MediaSourceType.APP_MUSIC);
        mHandler.post(() -> {
            if (sourceType == mStatusHolder.getCarDeviceStatus().sourceType) {
                mMediaListController.notifySelectedListInfo(hasParent, hasChild, currentPosition, subDisplayInfo, text);
            } else {
                Timber.w("notifySelectedListInfo() Source changed.");
            }
        });
    }

    /**
     * リスト項目選択.
     * <p>
     * リスト項目を選択（タップ）した場合に呼び出す。リスト項目は現在のソース種別が対象となる。
     * プリセットリストからの選局やUSBソースのリストからの再生や階層移動は本呼び出しにて行う。
     * {@link MediaSourceType#APP_MUSIC}は{@link ControlAppMusicSource#play(AppMusicContract.PlayParams)}を
     * 使用するため、本メソッドの呼び出しは不要である。
     *
     * @param listIndex リストインデックス。リスト項目の属性にあるリストインデックスを指定すること。
     * @throws IllegalArgumentException {@code listIndex}が1以上でない
     */
    public void selectListItem(@IntRange(from = 1) int listIndex) {
        checkArgument(1 <= listIndex);

        MediaSourceType sourceType = mStatusHolder.getCarDeviceStatus().sourceType;
        ListInfo.ListItem listItem = mCarDeviceMediaRepository.getListItem(sourceType, listIndex);
        if (listItem == null) {
            return;
        }

        mHandler.post(() -> {
            if (sourceType == mStatusHolder.getCarDeviceStatus().sourceType) {
                mMediaListController.selectListItem(listItem);
            } else {
                Timber.w("selectListItem() Source changed.");
            }
        });
    }

    /**
     * 戻る.
     * <p>
     * USBリスト専用。
     * 前の階層に戻る場合に呼び出す。
     */
    public void goBack(){
        mHandler.post(() -> {
            MediaSourceType sourceType = mStatusHolder.getCarDeviceStatus().sourceType;

            if(sourceType != MediaSourceType.USB){
                Timber.w("goBack() Not usb source.");
                return ;
            }

            if (sourceType == mStatusHolder.getCarDeviceStatus().sourceType) {
                mMediaListController.goBack();
            } else {
                Timber.w("goBack() Source changed.");
            }
        });
    }
}

