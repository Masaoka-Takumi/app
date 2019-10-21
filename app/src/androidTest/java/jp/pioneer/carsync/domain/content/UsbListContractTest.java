package jp.pioneer.carsync.domain.content;

import android.database.Cursor;
import android.database.MatrixCursor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.UsbInfoType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by NSW00_008320 on 2017/12/22.
 */
public class UsbListContractTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    Cursor mCursor;
    String[] mColumnNames = {
            UsbListContract._ID,
            UsbListContract.LIST_INDEX,
            UsbListContract.TEXT,
            UsbListContract.TYPE,
            UsbListContract.DATA_ENABLED
    };

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        MatrixCursor cursor = new MatrixCursor(mColumnNames);
        cursor.addRow(new Object[]{10, 1, "TEST_TEXT_1", UsbInfoType.FILE.code, 1});
        cursor.addRow(new Object[]{20, 2, "TEST_TEXT_2", UsbInfoType.FOLDER_MUSIC_EXIST.code, 1});
        cursor.addRow(new Object[]{30, 3, "TEST_TEXT_3", UsbInfoType.FOLDER_MUSIC_NOT_EXIST.code, 1});
        cursor.addRow(new Object[]{40, 4, "", null, 0});
        mCursor = cursor;
        mCursor.moveToFirst();
    }

    @Test
    public void getId() throws Exception {
        // exercise
        long actual = UsbListContract.getId(mCursor);

        // verify
        assertThat(actual, is(10L));
    }

    @Test
    public void getListIndex() throws Exception {
        // exercise
        int actual = UsbListContract.getListIndex(mCursor);

        // verify
        assertThat(actual, is(1));
    }

    @Test
    public void getText() throws Exception {
        // exercise
        String actual = UsbListContract.getText(mCursor);

        // verify
        assertThat(actual, is("TEST_TEXT_1"));
    }

    @Test
    public void getInfoType() throws Exception {
        // exercise
        UsbInfoType actual = UsbListContract.getInfoType(mCursor);

        // verify
        assertThat(actual, is(UsbInfoType.FILE));
    }

    @Test
    public void getDataEnabled() throws Exception {
        // exercise
        boolean actual = UsbListContract.getDataEnabled(mCursor);

        // verify
        assertThat(actual, is(true));
    }

}