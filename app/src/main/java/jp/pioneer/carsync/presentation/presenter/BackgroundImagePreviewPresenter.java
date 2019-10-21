package jp.pioneer.carsync.presentation.presenter;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.presentation.view.BackgroundImagePreviewView;

public class BackgroundImagePreviewPresenter extends Presenter<BackgroundImagePreviewView>{
    @Inject AppSharedPreference mPreference;
    /**
     * コンストラクタ
     */
    @Inject
    public BackgroundImagePreviewPresenter() {
    }

    public void setMyPhoto(){
        mPreference.setThemeMyPhotoEnabled(true);
    }

    public void cancelMyPhoto(){
        //mPreference.setThemeMyPhotoEnabled(false);
    }
}
