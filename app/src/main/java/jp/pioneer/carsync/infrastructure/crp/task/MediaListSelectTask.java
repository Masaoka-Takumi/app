package jp.pioneer.carsync.infrastructure.crp.task;

import android.support.annotation.NonNull;

import com.google.common.base.MoreObjects;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.component.CarRemoteSessionComponent;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandlerFactory;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.task.SendTaskId.MEDIA_LIST_SELECT;

/**
 * メディアリスト選択タスク.
 * <p>
 * P.CHリストからの選局は、車載機にフォーカス位置変更要求を行い、車載機操作コマンド通知で
 * CENTER PUSHを通知することで実現する。
 * 本タスクはフォーカス位置変更とCENTER PUSHを行うタスクである。
 * DAB（サービスリスト）は、ダイレクト選局用のコマンド（リストアイテム選択通知 : DAB）が
 * 用意されているため、そちらを使用することとし、本タスクの対象外とする。
 */
public class MediaListSelectTask extends SendTask {
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject ResponsePacketHandlerFactory mHandlerFactory;
    private int mListIndex;

    /**
     * コンストラクタ.
     *
     * @param listIndex リストインデックス
     */
    public MediaListSelectTask(int listIndex) {
        mListIndex = listIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("taskId", getSendTaskId())
                .add("listIndex", mListIndex)
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendTask inject(@NonNull CarRemoteSessionComponent component) {
        checkNotNull(component).inject(this);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public SendTaskId getSendTaskId() {
        return MEDIA_LIST_SELECT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void doTask() throws Exception {
        OutgoingPacketBuilder packetBuilder = getPacketBuilder();
        // フォーカス位置変更要求
        request(packetBuilder.createListFocusPositionChangeRequest(mListIndex));
        if (mStatusHolder.getListInfo().focusListIndex != mListIndex) {
            Timber.w("doTask() Unexpected focusListIndex.");
            return;
        }
        // 車載機操作コマンド通知（CENTER PUSH）
        post(packetBuilder.createDeviceControlCommand(CarDeviceControlCommand.CENTER_PUSH));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doResponsePacket(@NonNull IncomingPacket packet) throws Exception {
        mHandlerFactory.create(packet.getPacketIdType()).handle(packet);
    }
}
