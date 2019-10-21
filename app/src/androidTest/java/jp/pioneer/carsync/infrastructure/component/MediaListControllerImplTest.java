package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubDisplayInfo;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.MediaListSelectTask;
import jp.pioneer.carsync.infrastructure.crp.task.SendTask;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.model.ListType.EXIT;
import static jp.pioneer.carsync.domain.model.ListType.LIST;
import static jp.pioneer.carsync.domain.model.ListType.NOT_LIST;
import static jp.pioneer.carsync.domain.model.ListType.PCH_LIST;
import static jp.pioneer.carsync.domain.model.ListType.SERVICE_LIST;
import static jp.pioneer.carsync.domain.model.MediaSourceType.CD;
import static jp.pioneer.carsync.domain.model.MediaSourceType.RADIO;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * MediaListControllerImplのテスト.
 */
@RunWith(Theories.class)
public class MediaListControllerImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks MediaListControllerImpl mMediaListControllerImpl = new MediaListControllerImpl() {
        @Override
        SendTask createMediaListSelectTask(int listIndex) {
            assertThat(listIndex, is(mExceptedListIndex));
            return super.createMediaListSelectTask(listIndex);
        }
    };
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock StatusHolder mStatusHolder;
    @Mock CarDeviceStatus mCarDeviceStatus;
    private int mExceptedListIndex;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }


    static class EnterListFixture {
        ListType listType;

        EnterListFixture(ListType listType) {
            this.listType = listType;
        }
    }

    @DataPoints
    public static final boolean[] BOOLEANS = new boolean[] {
            true,
            false
    };

    @DataPoints
    public static final SubDisplayInfo[] SUB_DISPLAY_INFOS = SubDisplayInfo.values();

    @DataPoints
    public static final String[] TEXTS = new String[] {
            "",
            "Playlists"
    };

    @DataPoints
    public static final EnterListFixture[] ENTER_LIST_FIXTURES = new EnterListFixture[] {
            new EnterListFixture(ListType.PCH_LIST),
            new EnterListFixture(ListType.SERVICE_LIST),
            new EnterListFixture(LIST)
    };

    @Theory
    public void enterList_HappyPath(EnterListFixture fixture) throws Exception {
        // setup
        OutgoingPacket packet = mock(OutgoingPacket.class);
        mCarDeviceStatus.sourceType = RADIO;
        mCarDeviceStatus.listType = NOT_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mPacketBuilder.createListTransitionNotification(
                eq(TransitionDirection.ENTER), eq(mCarDeviceStatus.sourceType), eq(fixture.listType)))
                .thenReturn(packet);

        // exercise
        mMediaListControllerImpl.enterList(fixture.listType);

        // verify
        verify(mCarDeviceConnection).sendPacket(same(packet));
    }

    @Test
    public void enterList_SourceIsNotSupportList() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = CD;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mMediaListControllerImpl.enterList(PCH_LIST);

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Test
    public void enterList_ListTypeCanNotEnter() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = RADIO;
        mCarDeviceStatus.listType = PCH_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mMediaListControllerImpl.enterList(PCH_LIST);

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Test(expected = NullPointerException.class)
    public void enterList_ArgNull() {
        // exercise
        mMediaListControllerImpl.enterList(null);
    }

    @Test
    public void exitList_HappyPath() throws Exception {
        // setup
        OutgoingPacket packet = mock(OutgoingPacket.class);
        mCarDeviceStatus.sourceType = RADIO;
        mCarDeviceStatus.listType = PCH_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mPacketBuilder.createListTransitionNotification(
                eq(TransitionDirection.EXIT), eq(mCarDeviceStatus.sourceType), eq(EXIT)))
                .thenReturn(packet);

        // exercise
        mMediaListControllerImpl.exitList();

        // verify
        verify(mCarDeviceConnection).sendPacket(same(packet));
    }

    @Test
    public void exitList_SourceIsNotSupportList() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = CD;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mMediaListControllerImpl.exitList();

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Test
    public void exitList_ListTypeCanNotExit() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = RADIO;
        mCarDeviceStatus.listType = NOT_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mMediaListControllerImpl.exitList();

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Theory
    public void notifySelectedListInfo_HappyPath(boolean hasParent, boolean hasChild, SubDisplayInfo subDisplayInfo, String text) throws Exception {
        // setup
        OutgoingPacket packet = mock(OutgoingPacket.class);
        mCarDeviceStatus.listType = LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mPacketBuilder.createSelectedListDisplayInfoNotification(
                eq(MediaSourceType.APP_MUSIC), eq(hasParent), eq(hasChild), eq(subDisplayInfo), eq(text)))
                .thenReturn(packet);

        // exercise
        mMediaListControllerImpl.notifySelectedListInfo(hasParent, hasChild, subDisplayInfo, text);

        // verify
        verify(mCarDeviceConnection).sendPacket(same(packet));
    }

    @Test
    public void notifySelectedListInfo_NotAppMusicList() throws Exception {
        // setup
        mCarDeviceStatus.listType = NOT_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mMediaListControllerImpl.notifySelectedListInfo(true, true, SubDisplayInfo.ALBUMS, "Albums");

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Test(expected = NullPointerException.class)
    public void notifySelectedListInfo_ArgNull_SubDisplayInfo() throws Exception {
        // exercise
        mMediaListControllerImpl.notifySelectedListInfo(true, true, null, "");
    }

    @Test(expected = NullPointerException.class)
    public void notifySelectedListInfo_ArgNull_Text() throws Exception {
        // exercise
        mMediaListControllerImpl.notifySelectedListInfo(true, true, SubDisplayInfo.ALBUMS, null);
    }

    @Test
    public void selectListItem_RadioListItem() throws Exception {
        // setup
        OutgoingPacket packet = mock(OutgoingPacket.class);
        mCarDeviceStatus.listType = PCH_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        ListInfo.RadioListItem listItem = new ListInfo.RadioListItem();
        listItem.listIndex = 1;
        mExceptedListIndex = 1;

        // exercise
        mMediaListControllerImpl.selectListItem(listItem);

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
        verify(mCarDeviceConnection).executeSendTask(any(MediaListSelectTask.class));
    }

    @Test
    public void selectListItem_DabListItem() throws Exception {
        // setup
        OutgoingPacket packet = mock(OutgoingPacket.class);
        mCarDeviceStatus.listType = SERVICE_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        ListInfo.DabListItem listItem = new ListInfo.DabListItem();
        listItem.listIndex = 3;
        listItem.eid = 4;
        listItem.sid = 5;
        listItem.scids = 6;
        when(mPacketBuilder.createDabListItemSelectedNotification(3, 4, 5, 6)).thenReturn(packet);

        // exercise
        mMediaListControllerImpl.selectListItem(listItem);

        // verify
        verify(mCarDeviceConnection).sendPacket(same(packet));
        verify(mCarDeviceConnection, never()).executeSendTask(any(MediaListSelectTask.class));
    }

    @Test
    public void selectListItem_SxmListItem() throws Exception {
        // setup
        mCarDeviceStatus.listType = PCH_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        ListInfo.SxmListItem listItem = new ListInfo.SxmListItem();
        listItem.listIndex = 2;
        mExceptedListIndex = 2;

        // exercise
        mMediaListControllerImpl.selectListItem(listItem);

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
        verify(mCarDeviceConnection).executeSendTask(any(MediaListSelectTask.class));
    }

    @Test
    public void selectListItem_NotList() throws Exception {
        // setup
        mCarDeviceStatus.listType = NOT_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mMediaListControllerImpl.selectListItem(mock(ListInfo.ListItem.class));

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
        verify(mCarDeviceConnection, never()).executeSendTask(any(MediaListSelectTask.class));
    }

    @Test(expected = NullPointerException.class)
    public void selectListItem_ArgNull() throws Exception {
        // exercise
        mMediaListControllerImpl.selectListItem(null);
    }

    @Test
    public void goBack() throws Exception {
        // setup
        OutgoingPacket packet = mock(OutgoingPacket.class);
        mCarDeviceStatus.listType = LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.BAND_ESC)).thenReturn(packet);

        // exercise
        mMediaListControllerImpl.goBack();

        // verify
        verify(mCarDeviceConnection).sendPacket(same(packet));
    }

    @Test
    public void goBack_NoList() throws Exception {
        // setup
        OutgoingPacket packet = mock(OutgoingPacket.class);
        mCarDeviceStatus.listType = NOT_LIST;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mPacketBuilder.createDeviceControlCommand(CarDeviceControlCommand.BAND_ESC)).thenReturn(packet);

        // exercise
        mMediaListControllerImpl.goBack();

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }
}
