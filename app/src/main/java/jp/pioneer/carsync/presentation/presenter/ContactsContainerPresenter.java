package jp.pioneer.carsync.presentation.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.ContactsContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

/**
 * 電話帳 コンテナのpresenter
 */
@PresenterLifeCycle
public class ContactsContainerPresenter extends Presenter<ContactsContainerView> {
    /**
     * タブタイトル
     */
    public enum ContactsTab {
        CONTACTS,   // Contacts
        HISTORY,        // History
        FAVORITE,;       // Favorites
    }

    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @VisibleForTesting ContactsTab mTab;

    /**
     * コンストラクタ
     */
    @Inject
    public ContactsContainerPresenter() {
    }

    @Override
    void onInitialize() {
        mTab = ContactsTab.CONTACTS;
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.onNavigate(ScreenId.CONTACTS_LIST, Bundle.EMPTY);
            view.setCurrentTab(mTab);
        });
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mPreference.getUiColor().getResource()));
    }

    @Override
    void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("screen", mTab.name());
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        String strTab = (String) savedInstanceState.get("screen");
        ContactsTab tab = ContactsTab.valueOf(strTab);
        mTab = tab;
        Optional.ofNullable(getView()).ifPresent(view -> view.setCurrentTab(tab));
    }

/*
    /**
     * 左スワイプでの画面遷移
     *
     * @param shown タブ画面ID
     */
/*
   public void onLeftFlingAction(ScreenId shown) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (shown) {
                case CONTACTS_LIST:
                    mEventBus.post(new NavigateEvent(ScreenId.CONTACTS_FAVORITE, Bundle.EMPTY));
                    view.setCurrentTab(ContactsTab.TAB_FAVORITE);
                    mTab = ContactsTab.TAB_FAVORITE;
                    break;
                case CONTACTS_HISTORY:
                    mEventBus.post(new NavigateEvent(ScreenId.CONTACTS_LIST, Bundle.EMPTY));
                    view.setCurrentTab(ContactsTab.TAB_CONTACTS);
                    mTab = ContactsTab.TAB_CONTACTS;
                    break;
                case CONTACTS_FAVORITE:
                    mEventBus.post(new NavigateEvent(ScreenId.CONTACTS_HISTORY, Bundle.EMPTY));
                    view.setCurrentTab(ContactsTab.TAB_HISTORY);
                    mTab = ContactsTab.TAB_HISTORY;
                    break;
                default:
                    Timber.w("This case is impossible.");
                    break;
            }
        });
    }
*/
 /*
    /**
     * 右スワイプでの画面遷移
     *
     * @param shown タブ画面ID
     */
 /*    public void onRightFlingAction(ScreenId shown) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (shown) {
                case CONTACTS_LIST:
                    mEventBus.post(new NavigateEvent(ScreenId.CONTACTS_HISTORY, Bundle.EMPTY));
                    view.setCurrentTab(ContactsTab.TAB_HISTORY);
                    mTab = ContactsTab.TAB_HISTORY;
                    break;
                case CONTACTS_HISTORY:
                    mEventBus.post(new NavigateEvent(ScreenId.CONTACTS_FAVORITE, Bundle.EMPTY));
                    view.setCurrentTab(ContactsTab.TAB_FAVORITE);
                    mTab = ContactsTab.TAB_FAVORITE;
                    break;
                case CONTACTS_FAVORITE:
                    mEventBus.post(new NavigateEvent(ScreenId.CONTACTS_LIST, Bundle.EMPTY));
                    view.setCurrentTab(ContactsTab.TAB_CONTACTS);
                    mTab = ContactsTab.TAB_CONTACTS;
                    break;
                default:
                    Timber.w("This case is impossible.");
                    break;
            }
        });
    }
*/

    /**
     * タブ選択時のアクション
     *
     * @param tab 遷移先タブ
     */
    public void onTabAction(ContactsTab tab) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (tab) {
                case HISTORY:
                    if (mTab != ContactsTab.HISTORY) {
                        mEventBus.post(new NavigateEvent(ScreenId.CONTACTS_HISTORY, Bundle.EMPTY));
                        view.setCurrentTab(ContactsTab.HISTORY);
                        mTab = ContactsTab.HISTORY;
                    }
                    break;
                case FAVORITE:
                    if (mTab != ContactsTab.FAVORITE) {
                        mEventBus.post(new NavigateEvent(ScreenId.CONTACTS_FAVORITE, Bundle.EMPTY));
                        view.setCurrentTab(ContactsTab.FAVORITE);
                        mTab = ContactsTab.FAVORITE;
                    }
                    break;
                case CONTACTS:
                    if (mTab != ContactsTab.CONTACTS) {
                        mEventBus.post(new NavigateEvent(ScreenId.CONTACTS_LIST, Bundle.EMPTY));
                        view.setCurrentTab(ContactsTab.CONTACTS);
                        mTab = ContactsTab.CONTACTS;
                    }
                    break;
                default:
                    Timber.w("This case is impossible.");
                    break;
            }
        });
    }

    /**
     * 遷移元画面に戻る処理
     */
    public void onBackAction() {
        Optional.ofNullable(getView()).ifPresent(ContactsContainerView::callbackClose);
    }
}
