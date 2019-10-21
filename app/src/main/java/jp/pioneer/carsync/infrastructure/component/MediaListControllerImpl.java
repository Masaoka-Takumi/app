package jp.pioneer.carsync.infrastructure.component;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.MediaListController;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubDisplayInfo;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.MediaListSelectTask;
import jp.pioneer.carsync.infrastructure.crp.task.SendTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.model.ListType.ABC_SEARCH_LIST;
import static jp.pioneer.carsync.domain.model.ListType.LIST;

/**
 * MediaListControllerの実装.
 */
public class MediaListControllerImpl implements MediaListController {
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     */
    @Inject
    public MediaListControllerImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterList(@NonNull ListType listType) {
        Timber.i("enterList() listType = " + listType);
        checkNotNull(listType);

        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        if (!status.sourceType.isListSupported()
                || !status.listType.canEnter()) {
            return;
        }

        mCarDeviceConnection.sendPacket(mPacketBuilder.createListTransitionNotification(
                TransitionDirection.ENTER, status.sourceType, listType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitList() {
        Timber.i("exitList()");

        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        if (!status.sourceType.isListSupported()
                || !status.listType.canExit()) {
            return;
        }

        mCarDeviceConnection.sendPacket(mPacketBuilder.createListTransitionNotification(
                TransitionDirection.EXIT, status.sourceType, ListType.EXIT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySelectedListInfo(boolean hasParent, boolean hasChild, int currentPosition,
                                       @NonNull SubDisplayInfo subDisplayInfo, @NonNull String text) {
        Timber.i("notifySelectedListInfo() hasParent = %s, hasChild = %s, subDisplayInfo = %s, text = %s",
                hasParent, hasChild, subDisplayInfo, text);
        checkNotNull(subDisplayInfo);
        checkNotNull(text);

        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        if (status.listType != LIST && status.listType != ABC_SEARCH_LIST) {
            return;
        }

        ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();

        mCarDeviceConnection.sendPacket(mPacketBuilder.createSelectedListDisplayInfoNotification(
                version, MediaSourceType.APP_MUSIC, hasParent, hasChild, currentPosition, subDisplayInfo, text));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectListItem(@NonNull ListInfo.ListItem listItem) {
        Timber.i("selectListItem() listItem = " + listItem);
        checkNotNull(listItem);

        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        // 退場通知可能＝リスト状態と判断
        if (!status.listType.canExit()) {
            return;
        }

        // DABはMediaListSelectTaskを使用しない
        if (listItem instanceof ListInfo.DabListItem) {
            ListInfo.DabListItem item = (ListInfo.DabListItem) listItem;
            mCarDeviceConnection.sendPacket(mPacketBuilder.createDabListItemSelectedNotification(
                    item.index, item.eid, item.sid, item.scids));
        } else {
            mCarDeviceConnection.executeSendTask(createMediaListSelectTask(listItem.listIndex));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void goBack() {
        Timber.i("goBack()");

        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        // 退場通知可能＝リスト状態と判断
        if (!status.listType.canExit()) {
            return;
        }

        OutgoingPacket packet = mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.CROSS_LEFT);
        mCarDeviceConnection.sendPacket(packet);
    }

    @VisibleForTesting
    SendTask createMediaListSelectTask(int listIndex) {
        return new MediaListSelectTask(listIndex);
    }
}
