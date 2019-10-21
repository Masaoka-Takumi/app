package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.MediaListController;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubDisplayInfo;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.model.MediaSourceType.APP_MUSIC;
import static jp.pioneer.carsync.domain.model.MediaSourceType.CD;
import static jp.pioneer.carsync.domain.model.MediaSourceType.DAB;
import static jp.pioneer.carsync.domain.model.MediaSourceType.RADIO;
import static jp.pioneer.carsync.domain.model.MediaSourceType.SIRIUS_XM;
import static jp.pioneer.carsync.domain.model.MediaSourceType.USB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * ControlMediaListのテスト.
 */
@RunWith(Theories.class)
public class ControlMediaListTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public ExpectedException mExpectedException = ExpectedException.none();

    @InjectMocks ControlMediaList mControlMediaList;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock MediaListController mMediaListController;
    @Mock CarDeviceMediaRepository mCarDeviceMediaRepository;
    @Mock CarDeviceStatus mCarDeviceStatus;
    CountDownLatch mSignal;
    Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        // Handler#post(Runnable)がfinal methodなので、止むを得ずsendMessageAtTimeを使用
        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });
        mSignal = new CountDownLatch(1);
    }

    static class EnterListFixture {
        ListType listType;
        MediaSourceType sourceType;

        EnterListFixture(ListType listType, MediaSourceType sourceType) {
            this.listType = listType;
            this.sourceType = sourceType;
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

    @DataPoint
    public static final int INVALID_INDEX = 0;

    @DataPoints
    public static final EnterListFixture[] ENTER_LIST_FIXTURES = new EnterListFixture[] {
            new EnterListFixture(ListType.PCH_LIST, RADIO),
            new EnterListFixture(ListType.SERVICE_LIST, DAB),
            new EnterListFixture(ListType.LIST, APP_MUSIC)
    };

    @Theory
    public void enterList_HappyPath(EnterListFixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = fixture.sourceType;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mControlMediaList.enterList(fixture.listType);
        mSignal.await();

        // verify
        verify(mMediaListController).enterList(fixture.listType);
    }

    @Test
    public void enterList_SourceChanged() throws Exception {
        // setup
        CarDeviceStatus status = new CarDeviceStatus();
        status.sourceType = SIRIUS_XM;
        mCarDeviceStatus.sourceType = RADIO;
        when(mStatusHolder.getCarDeviceStatus())
                .thenReturn(mCarDeviceStatus)
                .thenReturn(mCarDeviceStatus)
                .thenReturn(status);

        // exercise
        mControlMediaList.enterList(ListType.PCH_LIST);
        mSignal.await();

        // verify
        verify(mMediaListController, never()).enterList(any(ListType.class));
    }

    @Test(expected = NullPointerException.class)
    public void enterList_ArgNull() throws Exception {
        // exercise
        mControlMediaList.enterList(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void enterList_SourceIsNotSupportListType() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = CD;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mControlMediaList.enterList(ListType.PCH_LIST);
    }

    @Test
    public void exitList_HappyPath() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = RADIO;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mControlMediaList.exitList();
        mSignal.await();

        // verify
        verify(mMediaListController).exitList();
    }

    @Test
    public void exitList_SourceChanged() throws Exception {
        // setup
        CarDeviceStatus status = new CarDeviceStatus();
        status.sourceType = SIRIUS_XM;
        mCarDeviceStatus.sourceType = RADIO;
        when(mStatusHolder.getCarDeviceStatus())
                .thenReturn(mCarDeviceStatus)
                .thenReturn(status);

        // exercise
        mControlMediaList.exitList();
        mSignal.await();

        // verify
        verify(mMediaListController, never()).exitList();
    }

    @Theory
    public void notifySelectedListInfo_HappyPath(boolean hasParent, boolean hasChild, SubDisplayInfo subDisplayInfo, String text) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = APP_MUSIC;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mControlMediaList.notifySelectedListInfo(hasParent, hasChild, subDisplayInfo, text);
        mSignal.await();

        // verify
        verify(mMediaListController).notifySelectedListInfo(eq(hasParent), eq(hasChild), eq(subDisplayInfo), eq(text));
    }

    @Test
    public void notifySelectedListInfo_SourceChanged() throws Exception {
        // setup
        CarDeviceStatus status = new CarDeviceStatus();
        status.sourceType = CD;
        mCarDeviceStatus.sourceType = APP_MUSIC;
        when(mStatusHolder.getCarDeviceStatus())
                .thenReturn(mCarDeviceStatus)
                .thenReturn(status);

        // exercise
        mControlMediaList.notifySelectedListInfo(false, false, SubDisplayInfo.ARTISTS, "Artists");
        mSignal.await();

        // verify
        verify(mMediaListController, never()).notifySelectedListInfo(anyBoolean(), anyBoolean(), any(SubDisplayInfo.class), anyString());
    }

    @Test(expected = NullPointerException.class)
    public void notifySelectedListInfo_ArgNull_SubDisplayInfo() throws Exception {
        // exercise
        mControlMediaList.notifySelectedListInfo(false, false, null, "");
    }

    @Test(expected = NullPointerException.class)
    public void notifySelectedListInfo_ArgNull_Text() throws Exception {
        // exercise
        mControlMediaList.notifySelectedListInfo(false, false, SubDisplayInfo.ARTISTS, null);
    }

    @Test(expected = IllegalStateException.class)
    public void notifySelectedListInfo_SourceIsNotAppMusic() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = CD;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mControlMediaList.notifySelectedListInfo(false, false, SubDisplayInfo.ARTISTS, "Artists");
    }

    @Test
    public void selectListItem_HappyPath() throws Exception {
        // setup
        ListInfo.ListItem expected = mock(ListInfo.ListItem.class);
        mCarDeviceStatus.sourceType = RADIO;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mCarDeviceMediaRepository.getListItem(RADIO, 1)).thenReturn(expected);

        // exercise
        mControlMediaList.selectListItem(1);
        mSignal.await();

        // verify
        verify(mMediaListController).selectListItem(same(expected));
    }

    @Test
    public void selectListItem_SourceChanged() throws Exception {
        // setup
        CarDeviceStatus status = new CarDeviceStatus();
        status.sourceType = CD;
        mCarDeviceStatus.sourceType = RADIO;
        when(mStatusHolder.getCarDeviceStatus())
                .thenReturn(mCarDeviceStatus)
                .thenReturn(status);
        when(mCarDeviceMediaRepository.getListItem(RADIO, 2)).thenReturn(mock(ListInfo.ListItem.class));

        // exercise
        mControlMediaList.selectListItem(2);
        mSignal.await();

        // verify
        verify(mMediaListController, never()).selectListItem(any(ListInfo.ListItem.class));
    }

    @Test
    public void selectListItem_DoNotGetListItem() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = RADIO;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mCarDeviceMediaRepository.getListItem(RADIO, 3)).thenReturn(null);

        // exercise
        mControlMediaList.selectListItem(3);

        // verify
        verify(mMediaListController, never()).selectListItem(any(ListInfo.ListItem.class));
    }

    @Theory
    public void selectListItem_InvalidListIndex(int listIndex) throws Exception {
        // setup
        mExpectedException.expect(IllegalArgumentException.class);

        // exercise
        mControlMediaList.selectListItem(listIndex);
    }

    @Test
    public void goBack() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = USB;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mControlMediaList.goBack();
        mSignal.await();

        // verify
        verify(mMediaListController).goBack();
    }

    @Test
    public void goBack_NotUsbSource() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = RADIO;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        // exercise
        mControlMediaList.goBack();
        mSignal.await();

        // verify
        verify(mMediaListController, never()).goBack();
    }

    @Test
    public void goBack_SourceChanged() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = USB;
        CarDeviceStatus afterStatus = new CarDeviceStatus();
        afterStatus.sourceType = APP_MUSIC;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus).thenReturn(afterStatus);

        // exercise
        mControlMediaList.goBack();
        mSignal.await();

        // verify
        verify(mMediaListController, never()).goBack();
    }

}