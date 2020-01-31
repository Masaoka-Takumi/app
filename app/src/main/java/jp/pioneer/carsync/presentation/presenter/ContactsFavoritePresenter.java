package jp.pioneer.carsync.presentation.presenter;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.content.ContactsCursorLoader;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.view.ContactsFavoriteView;
import timber.log.Timber;

/**
 * 電話帳 お気に入りリストのPresenter
 * <p>
 *
 * @see ContactsFavoriteView
 */
@PresenterLifeCycle
public class ContactsFavoritePresenter extends Presenter<ContactsFavoriteView> implements LoaderManager.LoaderCallbacks<Cursor> {

    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject QueryContact mContactCase;
    @Inject
    AnalyticsEventManager mAnalytics;
    private static final int LOADER_ID_CONTACT = -1;
    private static final String KEY_CONTACTS_ID = "contacts_id";
    private LoaderManager mLoaderManager;

    /**
     * コンストラクタ
     */
    @Inject
    public ContactsFavoritePresenter() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl;
        if (id == LOADER_ID_CONTACT) {
            cl = mContactCase.execute(ContactsContract.QueryParamsBuilder.createFavoriteContacts());
        } else {
            long contactId = args.getLong(KEY_CONTACTS_ID);
            cl = mContactCase.execute(ContactsContract.QueryParamsBuilder.createPhones(contactId));
        }
        return cl;
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager マネージャ
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(LOADER_ID_CONTACT, null, this);
    }

    /**
     * 親項目選択時の処理
     * <p>
     * 子項目のロードを行う。
     *
     * @param cursor Cursor 親カーソル
     */
    public void setGroupCursor(Cursor cursor) {
        int position = cursor.getPosition();
        long id = ContactsContract.Contact.getId(cursor);
        Timber.d("Position(" + position + ") : ID(" + id + ")");

        Bundle args = new Bundle();
        args.putLong(KEY_CONTACTS_ID, id);
        /*
         * 電話帳に連絡先の追加・更新・削除が行われた場合に、
         * 項目のポジションと指定IDの関係性が失われるため、
         * 毎回ローダーの作成を行う。
         */
        mLoaderManager.restartLoader(position, args, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ContactsCursorLoader load = (ContactsCursorLoader) loader;
        setCursor(loader.getId(), data, load.getExtras());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setCursor(loader.getId(), null,null);
    }

    private void setCursor(int id, Cursor data, Bundle args) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (id == LOADER_ID_CONTACT) {
                view.setGroupCursor(data, args);
            } else {
                if (data != null && !data.isClosed()) {
                    try {
                        Timber.d("Position(" + id + ")");
                        view.setChildrenCursor(id, data);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
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
     * 電話番号選択時のアクション
     *
     * @param cursor 選択番号のカーソル
     */
    public void onNumberAction(Cursor cursor) {
        String number = ContactsContract.Phone.getNumber(cursor);
        Uri uri = Uri.parse("tel:" + number);
        Optional.ofNullable(getView()).ifPresent(view -> view.dial(new Intent(Intent.ACTION_DIAL, uri)));
        mAnalytics.sendTelephoneCallEvent(Analytics.AnalyticsTelephoneCall.phoneBook);
    }
}
