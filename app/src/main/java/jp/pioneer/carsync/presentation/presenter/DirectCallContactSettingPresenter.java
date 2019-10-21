package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.ContactsCursorLoader;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.domain.content.ContactsContract.Phone;
import jp.pioneer.carsync.domain.content.ContactsContract.Contact;
import jp.pioneer.carsync.domain.content.ContactsContract.QueryParamsBuilder;
import jp.pioneer.carsync.presentation.view.DirectCallContactSettingView;
import timber.log.Timber;

/**
 * 連絡先選択リスト画面のpresenter
 */
@PresenterLifeCycle
public class DirectCallContactSettingPresenter extends Presenter<DirectCallContactSettingView>
        implements LoaderManager.LoaderCallbacks<Cursor> {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject QueryContact mContactCase;
    private static final int LOADER_ID_CONTACT = -1;
    private static final String KEY_CONTACTS_ID = "contacts_id";
    private LoaderManager mLoaderManager;

    /**
     * コンストラクタ
     */
    @Inject
    public DirectCallContactSettingPresenter() {
    }

    @Override
    void onInitialize() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setTargetContact(mPreference.getDirectCallContactLookupKey()));
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Optional.ofNullable(getView())
                .ifPresent(view -> view.setTargetContact(mPreference.getDirectCallContactLookupKey()));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl;
        if (id == LOADER_ID_CONTACT) {
            cl = mContactCase.execute(QueryParamsBuilder.createContacts());
        } else {
            long contactId = args.getLong(KEY_CONTACTS_ID);
            cl = mContactCase.execute(QueryParamsBuilder.createPhones(contactId));
        }
        return cl;
    }
    /**
     * LoaderManagerの設定と親項目のロード
     *
     * @param loaderManager LoaderManager
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
    public void onSelectGroup(Cursor cursor) {
        int position = cursor.getPosition();
        long id = Contact.getId(cursor);
        Timber.d("Position(" + position + ") : ID(" + id + ")");

        Bundle args = new Bundle();
        args.putLong(KEY_CONTACTS_ID, id);
        mLoaderManager.restartLoader(position, args, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ContactsCursorLoader load = (ContactsCursorLoader) loader;
        setCursor(loader.getId(), data, load.getExtras());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setCursor(loader.getId(), null, null);
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
     * 子項目選択時の処理
     * <p>
     * 選択した子項目からKEYとIDを取得し、保存する。
     *
     * @param cursor Cursor 子項目
     */
    public void onNumberAction(Cursor cursor) {
        mPreference.setDirectCallContactLookupKey(Phone.getLookupKey(cursor));
        mPreference.setDirectCallContactNumberId(Phone.getId(cursor));
        mEventBus.post(new GoBackEvent());
    }
}
