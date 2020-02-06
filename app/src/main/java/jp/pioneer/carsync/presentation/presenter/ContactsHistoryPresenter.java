package jp.pioneer.carsync.presentation.presenter;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.model.ContactsHistoryItem;
import jp.pioneer.carsync.presentation.view.ContactsHistoryView;

/**
 * 電話帳 発着信履歴リストのPresenter
 * <p>
 *
 * @see ContactsHistoryView
 */
@PresenterLifeCycle
public class ContactsHistoryPresenter extends Presenter<ContactsHistoryView> implements LoaderManager.LoaderCallbacks<Cursor> {

    @Inject EventBus mEventBus;
    @Inject QueryContact mContactCase;
    @Inject
    AnalyticsEventManager mAnalytics;
    private static final int LOADER_ID_HISTORY = 0;
    private LoaderManager mLoaderManager;

    /**
     * コンストラクタ
     */
    @Inject
    public ContactsHistoryPresenter() {
    }

    /**
     * LoaderManagerの設定
     *
     * @param loaderManager マネージャ
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(LOADER_ID_HISTORY, Bundle.EMPTY, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mContactCase.execute(ContactsContract.QueryParamsBuilder.createCalls());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setHistoryList(createHistoryList(data)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setHistoryList(null));
    }

    private ArrayList<ContactsHistoryItem> createHistoryList(Cursor data){
        ArrayList<ContactsHistoryItem> historyList = new ArrayList<>();
        boolean isEof = data.moveToFirst();
        while (isEof) {
            int callType = ContactsContract.Call.getType(data);
            String name = ContactsContract.Call.getDisplayName(data);
            String number = ContactsContract.Call.getNumber(data);
            if (name == null || name.isEmpty()) {
                name = number;
            }
            int count = 1;
            Date date = ContactsContract.Call.getDate(data);
            String strDate = (String) android.text.format.DateFormat.format("yyyy/MM/dd", date);
            boolean isExist=false;
            if(historyList.size()>0) {
                for (int i = historyList.size() - 1;i >= 0;i--) {
                    Date dateNew = historyList.get(i).date;
                    String strDateNew = (String) android.text.format.DateFormat.format("yyyy/MM/dd", dateNew);
                    if (!strDateNew.equals(strDate)) break;
                    String nameNew = historyList.get(i).displayName;
                    int callTypeNew = historyList.get(i).callType;
                    if (nameNew.equals(name) && callTypeNew == callType) {
                        count = historyList.get(i).count + 1;
                        isExist = true;
                        historyList.set(i, new ContactsHistoryItem(callType, name, count, dateNew, number));
                        break;
                    }
                }
            }
            if(!isExist){
                historyList.add(new ContactsHistoryItem(callType, name, count, date, number));
            }
            isEof = data.moveToNext();
        }
        return historyList;
    }

    /**
     * 履歴選択時のアクション
     *
     * @param item 発着信履歴
     */
    public void onContactsHistoryAction(ContactsHistoryItem item) {
        String number = item.number;
        Uri uri = Uri.parse("tel:" + number);
        Optional.ofNullable(getView()).ifPresent(view -> view.dial(new Intent(Intent.ACTION_DIAL, uri)));
        mAnalytics.sendTelephoneCallEvent(Analytics.AnalyticsTelephoneCall.phoneBook);
    }
}
