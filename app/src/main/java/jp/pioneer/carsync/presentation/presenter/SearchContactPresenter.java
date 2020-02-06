package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.SearchContactView;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;
import timber.log.Timber;

/**
 * 連絡先検索画面のpresenter
 */
@PresenterLifeCycle
public class SearchContactPresenter extends Presenter<SearchContactView> implements LoaderManager.LoaderCallbacks<Cursor> {

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject QueryContact mContactCase;
    @Inject
    AnalyticsEventManager mAnalytics;
    private static final int LOADER_ID_CONTACT = -1;
    private static final String KEY_CONTACTS_ID = "contacts_id";
    private LoaderManager mLoaderManager;
    private String[] mKeywords;

    /**
     * コンストラクタ
     */
    @Inject
    public SearchContactPresenter() {
    }

    /**
     * 引き継ぎ情報設定
     *
     * @param args Bundle
     */
    public void setArguments(Bundle args) {
        SearchContentParams params = SearchContentParams.from(args);
        mKeywords = params.searchWords;
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(LOADER_ID_CONTACT, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl;
        if (id == LOADER_ID_CONTACT) {
            cl = mContactCase.execute(ContactsContract.QueryParamsBuilder.createContactsByKeywords(mKeywords));
        } else {
            long contactId = args.getLong(KEY_CONTACTS_ID);
            cl = mContactCase.execute(ContactsContract.QueryParamsBuilder.createPhones(contactId));
        }
        return cl;
    }

    /**
     * 親項目選択時の処理
     * <p>
     * 子項目のロードを行う。
     *
     * @param cursor Cursor 親カーソル
     */
    public void onSelectGroup(Cursor cursor) {
        int position = cursor.getPosition();
        long id = ContactsContract.Contact.getId(cursor);
        Timber.d("Position(" + position + ") : ID(" + id + ")");

        Bundle args = new Bundle();
        args.putLong(KEY_CONTACTS_ID, id);
        mLoaderManager.restartLoader(position, args, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        setCursor(loader.getId(), data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setCursor(loader.getId(), null);
    }

    private void setCursor(int id, Cursor data) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (id == LOADER_ID_CONTACT) {
                view.setGroupCursor(data);

            } else {
                if (data != null && !data.isClosed()) {
                    Timber.d("Position(" + id + ")");
                    view.setChildrenCursor(id, data);
                }
            }
        });
    }

    /**
     * 子項目を閉じた場合の処理
     *
     * @param position 親項目番号
     */
    public void onGroupCollapseAction(int position) {
        mLoaderManager.destroyLoader(position);
    }

    /**
     * 子項目選択時の処理
     *
     * @param cursor Cursor 子項目
     */
    public void onNumberAction(Cursor cursor) {
        String number = ContactsContract.Phone.getNumber(cursor);
        Optional.ofNullable(getView()).ifPresent(view -> view.dial(number));
        mAnalytics.sendTelephoneCallEvent(Analytics.AnalyticsTelephoneCall.voiceRecognizer);
        mEventBus.post(new GoBackEvent());
    }
}
