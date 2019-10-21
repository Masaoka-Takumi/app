package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.ImpactDetectionContactRegisterView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * 衝突検知緊急連絡先設定画面のpresenter.
 */
@PresenterLifeCycle
public class ImpactDetectionContactRegisterPresenter extends Presenter<ImpactDetectionContactRegisterView>
        implements LoaderManager.LoaderCallbacks<Cursor> {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject QueryContact mContactCase;
    private LoaderManager mLoaderManager;
    private static final int LOADER_ID_NAME = 1;
    private static final int LOADER_ID_NUMBER = 2;
    private static final String KEY_NAME_ID = "name_id";
    private static final String KEY_NUMBER = "number";
    private Cursor NameDataCursor;
    private Cursor PhoneDataCursor;

    /**
     * コンストラクタ
     */
    @Inject
    public ImpactDetectionContactRegisterPresenter() {
    }

    @Override
    void onResume() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setRegisterEnabled(mPreference.isPhoneBookAccessible()));
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            String number = mPreference.getImpactNotificationContactNumber();
            if (!TextUtils.isEmpty(number)) {
                view.setPhoneItem(number, R.drawable.p0050_phone);
            }
        });
    }

    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        String lookup = mPreference.getImpactNotificationContactLookupKey();
        String number = mPreference.getImpactNotificationContactNumber();

        if (lookup.isEmpty() || TextUtils.isEmpty(number)) {
            Optional.ofNullable(getView()).ifPresent(ImpactDetectionContactRegisterView::setDisable);
            return;
        }

        NameDataCursor = null;
        PhoneDataCursor = null;

        Bundle nameArgs = new Bundle();
        nameArgs.putString(KEY_NAME_ID, lookup);
        loaderManager.restartLoader(LOADER_ID_NAME, nameArgs, this);

        Bundle numberArgs = new Bundle();
        numberArgs.putString(KEY_NUMBER, number);
        loaderManager.restartLoader(LOADER_ID_NUMBER, numberArgs, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID_NAME && !TextUtils.isEmpty(args.getString(KEY_NAME_ID))) {
            //noinspection ConstantConditions
            return mContactCase.execute(ContactsContract.QueryParamsBuilder.createContact(args.getString(KEY_NAME_ID)));
        } else if (id == LOADER_ID_NUMBER && !TextUtils.isEmpty(args.getString(KEY_NUMBER))) {
            return mContactCase.execute(ContactsContract.QueryParamsBuilder.createPhone(args.getString(KEY_NUMBER)));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if ((data != null) && data.getCount() > 0) {
                if (loader.getId() == LOADER_ID_NAME) {
                    NameDataCursor = data;
                } else if (loader.getId() == LOADER_ID_NUMBER) {
                    PhoneDataCursor = data;
                }

                if(PhoneDataCursor != null){
                    PhoneDataCursor.moveToFirst();
                    String number = ContactsContract.Phone.getNumber(PhoneDataCursor);
                    int type;
                    switch(ContactsContract.Phone.getNumberType(PhoneDataCursor)) {
                        case 1:
                            type = R.drawable.p0052_home;
                            break;
                        case 2:
                            type = R.drawable.p0051_mobile;
                            break;
                        case 3:
                            type = R.drawable.p0053_business;
                            break;
                        default:
                            type = R.drawable.p0050_phone;
                            break;
                    }
                    view.setPhoneItem(number, type);
                    PhoneDataCursor = null;
                }

                if(NameDataCursor != null){
                    NameDataCursor.moveToFirst();
                    String name = ContactsContract.Contact.getDisplayName(NameDataCursor);
                    Uri photoUri = ContactsContract.Contact.getPhotoUri(NameDataCursor);
                    view.setContactItem(name, photoUri);
                    NameDataCursor = null;
                }
            }
            else {
                if (loader.getId() == LOADER_ID_NAME) {
                    view.setContactItem("", null);
                }

                String number = mPreference.getImpactNotificationContactNumber();
                if (!TextUtils.isEmpty(number)) {
                    view.setPhoneItem(number, R.drawable.p0050_phone);
                    return;
                } else {
                    view.setDisable();
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    /**
     * 削除ボタン押下時の処理
     */
    public void onDeleteAction() {
        mPreference.removeImpactNotificationContactLookupKey();
        mLoaderManager.destroyLoader(LOADER_ID_NAME);
        mPreference.removeImpactNotificationContactNumber();
        mLoaderManager.destroyLoader(LOADER_ID_NUMBER);
        NameDataCursor = null;
        PhoneDataCursor = null;
        Optional.ofNullable(getView()).ifPresent(ImpactDetectionContactRegisterView::setDisable);
    }

    /**
     * 登録ボタン押下時の処理
     */
    public void onRegisterAction() {
        SettingsParams params = new SettingsParams();
        params.pass = mContext.getString(R.string.set_252);
        mEventBus.post(new NavigateEvent(ScreenId.IMPACT_DETECTION_CONTACT_SETTING, params.toBundle()));
    }
}