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

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.VoiceCommand;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.SearchContainerView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 音認検索コンテナのpresenterテスト
 */
public class SearchContainerPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SearchContainerPresenter mPresenter = new SearchContainerPresenter();
    @Mock SearchContainerView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testOnInitializeMusic() throws Exception {
        String[] words = new String[]{"mr.", "MR."};
        Bundle args = SearchContentParams.toBundle(VoiceCommand.ARTIST, words);

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.setArgument(args);
        mPresenter.takeView(mView);
        mPresenter.initialize();

        verify(mView).setTitle(any(String.class));
        verify(mView).onNavigate(eq(ScreenId.SEARCH_MUSIC_RESULTS), any(Bundle.class));
    }

    @Test
    public void testOnInitializeContacts() throws Exception {
        String[] words = new String[]{"たなか", "タナカ"};
        Bundle args = SearchContentParams.toBundle(VoiceCommand.PHONE, words);

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.setArgument(args);
        mPresenter.takeView(mView);
        mPresenter.initialize();

        verify(mView).setTitle(any(String.class));
        verify(mView).onNavigate(eq(ScreenId.SEARCH_CONTACT_RESULTS), any(Bundle.class));
    }

    @Test
    public void testSetTitle() throws Exception {
        String[] words = new String[]{"mr.", "MR."};
        Bundle args = SearchContentParams.toBundle(VoiceCommand.ARTIST, words);
        MusicParams params = new MusicParams();
        params.pass = "Mr.KK";

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.setArgument(args);
        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.setTitle(params.toBundle());

        verify(mView).setTitle(eq("Mr.KK"));
    }

    @Test
    public void testRemoveTitle() throws Exception {
        String[] words = new String[]{"mr.", "MR."};
        Bundle args = SearchContentParams.toBundle(VoiceCommand.ARTIST, words);
        MusicParams params = new MusicParams();
        params.pass = "Mr.KK";

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.setArgument(args);
        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.setTitle(params.toBundle());
        mPresenter.removeTitle();

        String format = getTargetContext().getResources().getString(R.string.search_result_title);
        String expected = String.format(format, "mr., MR.");
        verify(mView, times(2)).setTitle(eq(expected));
    }

    @Test
    public void testOnBackAction() throws Exception {
        mPresenter.takeView(mView);
        mPresenter.onBackAction();

        verify(mEventBus).post(any(GoBackEvent.class));
    }
}