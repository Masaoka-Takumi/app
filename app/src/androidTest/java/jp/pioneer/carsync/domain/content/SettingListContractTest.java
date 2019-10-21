package jp.pioneer.carsync.domain.content;

import android.database.Cursor;
import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.content.SettingListContract.DeviceList;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by NSW00_008320 on 2017/06/13.
 */
@RunWith(Enclosed.class)
public class SettingListContractTest {

    public static class QuerySettingListParamsBuilderTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        }

        @Test
        public void createDeviceList() throws Exception {
            // exercise
            QuerySettingListParams params = SettingListContract.QuerySettingListParamsBuilder.createDeviceList();

            // verify
            assertThat(params, is(new QuerySettingListParams(
                    SettingListType.DEVICE_LIST,
                    false,
                    false,
                    false
            )));
        }

        @Test
        public void createSearchList() throws Exception {
            // exercise
            QuerySettingListParams params = SettingListContract.QuerySettingListParamsBuilder.createSearchList();

            // verify
            assertThat(params, is(new QuerySettingListParams(
                    SettingListType.SEARCH_LIST,
                    false,
                    false,
                    false
            )));
        }

        @Test
        public void createA2dpList() throws Exception {
            // exercise
            QuerySettingListParams params = SettingListContract.QuerySettingListParamsBuilder.createA2dpList();

            // verify
            assertThat(params, is(new QuerySettingListParams(
                    SettingListType.DEVICE_LIST,
                    true,
                    false,
                    false
            )));
        }

        @Test
        public void createAudioConnectedDevice() throws Exception {
            // exercise
            QuerySettingListParams params = SettingListContract.QuerySettingListParamsBuilder.createAudioConnectedDevice();

            // verify
            assertThat(params, is(new QuerySettingListParams(
                    SettingListType.DEVICE_LIST,
                    false,
                    false,
                    true
            )));
        }
    }

    public static class DeviceListTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        private static final String[] DEVICE_LIST_COLUMNS = {
                SettingListContract.DeviceList._ID,
                SettingListContract.DeviceList.BD_ADDRESS,
                SettingListContract.DeviceList.DEVICE_NAME,
                SettingListContract.DeviceList.AUDIO_SUPPORTED,
                SettingListContract.DeviceList.PHONE_SUPPORTED,
                SettingListContract.DeviceList.AUDIO_CONNECTED,
                SettingListContract.DeviceList.PHONE_1_CONNECTED,
                SettingListContract.DeviceList.PHONE_2_CONNECTED,
                SettingListContract.DeviceList.LAST_AUDIO_DEVICE,
                SettingListContract.DeviceList.SESSION_CONNECTED,
                SettingListContract.DeviceList.AUDIO_CONNECT_STATUS,
                SettingListContract.DeviceList.PHONE_CONNECT_STATUS,
                SettingListContract.DeviceList.DELETE_STATUS
        };

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            MatrixCursor cursor = new MatrixCursor(DEVICE_LIST_COLUMNS);
            cursor.addRow(new Object[]{ 1, "TEST_BD_ADDRESS_1", "TEST_DEVICE_NAME_1", 1, 1, 1, 1, 1, 1, 1, DeviceList.AudioConnectStatus.STATUS_CONNECTED.code, DeviceList.PhoneConnectStatus.STATUS_DISCONNECTED.code, DeviceList.DeleteStatus.STATUS_DELETED.code});
            cursor.addRow(new Object[]{ 2, "TEST_BD_ADDRESS_2", "TEST_DEVICE_NAME_2", 0, 0, 0, 0, 0, 0, 0, DeviceList.AudioConnectStatus.STATUS_CONNECT_FAILED.code, DeviceList.PhoneConnectStatus.STATUS_DISCONNECT_FAILED.code, DeviceList.DeleteStatus.STATUS_DELETE_FAILED.code});
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            // exercise
            long actual = SettingListContract.DeviceList.getId(mCursor);

            // verify
            assertThat(actual, is(1L));
        }

        @Test
        public void getBdAddress() throws Exception {
            // exercise
            String actual = SettingListContract.DeviceList.getBdAddress(mCursor);

            // verify
            assertThat(actual, is("TEST_BD_ADDRESS_1"));
        }

        @Test
        public void getDeviceName() throws Exception {
            // exercise
            String actual = SettingListContract.DeviceList.getDeviceName(mCursor);

            // verify
            assertThat(actual, is("TEST_DEVICE_NAME_1"));
        }

        @Test
        public void getAudioSupported() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isAudioSupported(mCursor);

            // verify
            assertThat(actual, is(true));
        }

        @Test
        public void getPhoneSupported() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isPhoneSupported(mCursor);

            // verify
            assertThat(actual, is(true));
        }

        @Test
        public void getAudioConnected() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isAudioConnected(mCursor);

            // verify
            assertThat(actual, is(true));
        }

        @Test
        public void getPhone1Connected() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isPhone1Connected(mCursor);

            // verify
            assertThat(actual, is(true));
        }

        @Test
        public void getPhone2Connected() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isPhone2Connected(mCursor);

            // verify
            assertThat(actual, is(true));
        }

        @Test
        public void getLastAudioDevice() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isLastAudioDevice(mCursor);

            // verify
            assertThat(actual, is(true));
        }

        @Test
        public void getSessionConnected() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isSessionConnected(mCursor);

            // verify
            assertThat(actual, is(true));
        }

        @Test
        public void getAudioConnectStatus() throws Exception {
            // exercise
            DeviceList.AudioConnectStatus actual = SettingListContract.DeviceList.getAudioConnectStatus(mCursor);

            // verify
            assertThat(actual, is(DeviceList.AudioConnectStatus.STATUS_CONNECTED));
        }

        @Test
        public void getPhoneConnectStatus() throws Exception {
            // exercise
            DeviceList.PhoneConnectStatus actual = SettingListContract.DeviceList.getPhoneConnectStatus(mCursor);

            // verify
            assertThat(actual, is(DeviceList.PhoneConnectStatus.STATUS_DISCONNECTED));
        }

        @Test
        public void getDeleteStatus() throws Exception {
            // exercise
            DeviceList.DeleteStatus actual = SettingListContract.DeviceList.getDeleteStatus(mCursor);

            // verify
            assertThat(actual, is(DeviceList.DeleteStatus.STATUS_DELETED));
        }

        @Test(expected = NullPointerException.class)
        public void getId_ArgNull() throws Exception {
            // exercise
            long actual = SettingListContract.DeviceList.getId(null);
        }

        @Test(expected = NullPointerException.class)
        public void getBdAddress_ArgNull() throws Exception {
            // exercise
            String actual = SettingListContract.DeviceList.getBdAddress(null);
        }

        @Test(expected = NullPointerException.class)
        public void getDeviceName_ArgNull() throws Exception {
            // exercise
            String actual = SettingListContract.DeviceList.getDeviceName(null);
        }

        @Test(expected = NullPointerException.class)
        public void getAudioSupported_ArgNull() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isAudioSupported(null);
        }

        @Test(expected = NullPointerException.class)
        public void getPhoneSupported_ArgNull() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isPhoneSupported(null);
        }

        @Test(expected = NullPointerException.class)
        public void getAudioConnected_ArgNull() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isAudioConnected(null);
        }

        @Test(expected = NullPointerException.class)
        public void getPhone1Connected_ArgNull() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isPhone1Connected(null);
        }

        @Test(expected = NullPointerException.class)
        public void getPhone2Connected_ArgNull() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isPhone2Connected(null);
        }

        @Test(expected = NullPointerException.class)
        public void getLastAudioDevice_ArgNull() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isLastAudioDevice(null);
        }

        @Test(expected = NullPointerException.class)
        public void getSessionConnected_ArgNull() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isSessionConnected(null);
        }

        @Test(expected = NullPointerException.class)
        public void getAudioConnectStatus_ArgNull() throws Exception {
            // exercise
            DeviceList.AudioConnectStatus actual = SettingListContract.DeviceList.getAudioConnectStatus(null);
        }

        @Test(expected = NullPointerException.class)
        public void getPhoneConnectStatus_ArgNull() throws Exception {
            // exercise
            DeviceList.PhoneConnectStatus actual = SettingListContract.DeviceList.getPhoneConnectStatus(null);
        }

        @Test(expected = NullPointerException.class)
        public void getDeleteStatus_ArgNull() throws Exception {
            // exercise
            DeviceList.DeleteStatus actual = SettingListContract.DeviceList.getDeleteStatus(null);
        }
    }

    public static class SearchListTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        private static final String[] SEARCH_LIST_COLUMNS = {
                SettingListContract.SearchList._ID,
                SettingListContract.SearchList.BD_ADDRESS,
                SettingListContract.SearchList.DEVICE_NAME,
                SettingListContract.SearchList.AUDIO_SUPPORTED,
                SettingListContract.SearchList.PHONE_SUPPORTED
        };

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            MatrixCursor cursor = new MatrixCursor(SEARCH_LIST_COLUMNS);
            cursor.addRow(new Object[]{ 1, "TEST_BD_ADDRESS_1", "TEST_DEVICE_NAME_1", 1, 1});
            cursor.addRow(new Object[]{ 2, "TEST_BD_ADDRESS_2", "TEST_DEVICE_NAME_2", 0, 0});
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            // exercise
            long actual = SettingListContract.DeviceList.getId(mCursor);

            // verify
            assertThat(actual, is(1L));
        }

        @Test
        public void getBdAddress() throws Exception {
            // exercise
            String actual = SettingListContract.DeviceList.getBdAddress(mCursor);

            // verify
            assertThat(actual, is("TEST_BD_ADDRESS_1"));
        }

        @Test
        public void getDeviceName() throws Exception {
            // exercise
            String actual = SettingListContract.DeviceList.getDeviceName(mCursor);

            // verify
            assertThat(actual, is("TEST_DEVICE_NAME_1"));
        }

        @Test
        public void getAudioSupported() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isAudioSupported(mCursor);

            // verify
            assertThat(actual, is(true));
        }

        @Test
        public void getPhoneSupported() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isPhoneSupported(mCursor);

            // verify
            assertThat(actual, is(true));
        }

        @Test(expected = NullPointerException.class)
        public void getId_ArgNull() throws Exception {
            // exercise
            long actual = SettingListContract.DeviceList.getId(null);
        }

        @Test(expected = NullPointerException.class)
        public void getBdAddress_ArgNull() throws Exception {
            // exercise
            String actual = SettingListContract.DeviceList.getBdAddress(null);
        }

        @Test(expected = NullPointerException.class)
        public void getDeviceName_ArgNull() throws Exception {
            // exercise
            String actual = SettingListContract.DeviceList.getDeviceName(null);
        }

        @Test(expected = NullPointerException.class)
        public void getAudioSupported_ArgNull() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isAudioSupported(null);
        }

        @Test(expected = NullPointerException.class)
        public void getPhoneSupported_ArgNull() throws Exception {
            // exercise
            boolean actual = SettingListContract.DeviceList.isPhoneSupported(null);
        }
    }
}