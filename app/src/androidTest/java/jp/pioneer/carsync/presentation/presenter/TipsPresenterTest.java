package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.UpdateTipsItemEvent;
import jp.pioneer.carsync.presentation.model.TipsItem;
import jp.pioneer.carsync.presentation.model.TipsTag;
import jp.pioneer.carsync.presentation.util.TipsList;
import jp.pioneer.carsync.presentation.view.TipsView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TipsPresenterTest
 */
public class TipsPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks TipsPresenter mPresenter = new TipsPresenter();
    @Mock TipsView mView;
    @Mock EventBus mEventBus;
    @Mock Context mContext;
    @Mock TipsList mTipsList;
    private TipsItem[] mItems;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        mItems= new TipsItem[3];
        TipsTag[] tag1 = new TipsTag[1];
        tag1[0] = new TipsTag(1,"manual","manual","");
        mItems[0] = new TipsItem(1,"https://www.google.co.jp/","Title Manual","Sample Text",null, tag1,"Sample Text");
        TipsTag[] tag2 = new TipsTag[1];
        tag2[0] =new TipsTag(1,"tips","tips","");
        mItems[1] = new TipsItem(2,"https://www.google.co.jp/","Title Tips","Sample Text",null, tag2,"Sample Text");
        TipsTag[] tag3 = new TipsTag[1];
        tag3[0] = new TipsTag(1,"information","information","");
        mItems[2] = new TipsItem(3,"https://www.google.co.jp/","Title Information","Sample Text",null, tag3,"Sample Text");

    }

    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        mPresenter.onResume();
        verify(mEventBus).register(mPresenter);
        verify(mTipsList).update();
    }

    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void setArgument() throws Exception {
    }

    @Test
    public void testShowTips() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Bundle args = new Bundle();
        args.putString("url", "https://www.google.co.jp/");
        mTipsList.items = mItems;
        mTipsList.isError = false;
        mPresenter.onUpdateTipsItemEvent(new UpdateTipsItemEvent());

        mPresenter.showTips(1);
        verify(mView).setAdapter(mItems);
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.TIPS_WEB));
    }

    @Test
    public void testOnSettingAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onSettingAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.SETTINGS_CONTAINER));
    }

    @Test
    public void testOnBtAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onBtAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.EASY_PAIRING));

    }

    @Test
    public void testOnUpdateTipsItemEventListError() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Bundle args = new Bundle();
        args.putString("url", "https://www.google.co.jp/");
        mTipsList.items = mItems;
        mTipsList.isError = true;
        mPresenter.onUpdateTipsItemEvent(new UpdateTipsItemEvent());
        //TODO:スプラッシュスクリーン画像を表示する
    }

}