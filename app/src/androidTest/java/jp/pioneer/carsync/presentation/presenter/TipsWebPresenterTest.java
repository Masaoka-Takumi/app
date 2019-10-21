package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.presentation.view.TipsWebView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2018/01/24.
 */
public class TipsWebPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks TipsWebPresenter mPresenter = new TipsWebPresenter();
    @Mock TipsWebView mView;
    @Mock EventBus mEventBus;
    @Mock Context mContext;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testOnTakeView() throws Exception {
        Bundle args = new Bundle();
        args.putString("url", "https://www.google.co.jp/");
        mPresenter.setArgument(args);
        mPresenter.onTakeView();
        verify(mView).loadUrl("https://www.google.co.jp/");
    }

    @Test
    public void testOnBackAction() throws Exception {
        mPresenter.onBackAction();
        //verify(mEventBus).post(any(GoBackEvent.class));

    }

}