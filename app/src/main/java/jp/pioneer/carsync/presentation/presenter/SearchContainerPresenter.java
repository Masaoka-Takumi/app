package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.SearchContainerView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * 音声認識検索コンテナのpresenter
 */
@PresenterLifeCycle
public class SearchContainerPresenter extends Presenter<SearchContainerView> {

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    private ArrayList<String> mTitles = new ArrayList<>();
    private Bundle mArguments;
    private boolean mIsMusicSearch = false;
    /**
     * コンストラクタ
     */
    @Inject
    public SearchContainerPresenter() {
    }

    @Override
    void onInitialize() {
        SearchContentParams params = SearchContentParams.from(mArguments);
        String searchWords;
        StringBuilder buf = new StringBuilder();
        for(int i=0;i<params.searchWords.length;i++){
            if(i != 0){
                buf.append(", ");
            }
            buf.append(params.searchWords[i]);
        }
        searchWords = buf.toString();
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (params.voiceCommand) {
                case ARTIST:
                case ALBUM:
                case SONG:
                    mIsMusicSearch = true;
                    mTitles.add(mContext.getResources().getString(R.string.rec_010) + " \"" + searchWords + "\"");
                    view.onNavigate(ScreenId.SEARCH_MUSIC_RESULTS, mArguments);
                    break;
                case PHONE:
                    mIsMusicSearch = false;
                    mTitles.add(String.format(mContext.getResources().getString(R.string.rec_002), searchWords));
                    view.onNavigate(ScreenId.SEARCH_CONTACT_RESULTS, mArguments);
                    break;
                default:
                    mTitles.add(mContext.getResources().getString(R.string.rec_010) + " \"" + searchWords + "\"");
                    break;
            }
            view.setTitle(getTitle(),mTitles.size()==1);
        });
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(getTitle(),mTitles.size()==1));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent ev) {
        if(mIsMusicSearch){
            StatusHolder holder = mGetStatusHolder.execute();
            //ローカルソースでなくなったら音楽検索結果ダイアログを閉じる
            if(holder.getCarDeviceStatus().sourceType!= MediaSourceType.APP_MUSIC){
                Optional.ofNullable(getView()).ifPresent(SearchContainerView::onClose);
            }
        }
    }

    /**
     * 引き継ぎ情報設定
     *
     * @param args Bundle
     */
    public void setArgument(Bundle args) {
        mArguments = args;
    }

    /**
     * 表示カテゴリの設定
     *
     * @param args Bundle
     */
    public void setTitle(Bundle args) {
        MusicParams params = MusicParams.from(args);
        if (params.pass != null) {
            mTitles.add(params.pass);
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(getTitle(),mTitles.size()==1));
    }

    /**
     * 表示カテゴリの削除
     * <p>
     * 画面戻しの際にひとつ前のカテゴリを表示する
     */
    public void removeTitle() {
        int index = mTitles.size() - 1;
        mTitles.remove(index);
        Optional.ofNullable(getView()).ifPresent(view -> view.setTitle(getTitle(),mTitles.size()==1));
    }

    /**
     * 画面戻り処理
     */
    public void onBackAction() {
        mEventBus.post(new GoBackEvent());
    }

    private String getTitle() {
        String title = "";
        if(mTitles.size() > 0) {
            int index = mTitles.size() - 1;
            title = mTitles.get(index);
        }


        return title;
    }
}
