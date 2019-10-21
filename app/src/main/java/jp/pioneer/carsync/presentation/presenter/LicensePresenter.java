package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.view.LicenseView;
import timber.log.Timber;

/**
 * Licence画面のPresenter
 */
@PresenterLifeCycle
public class LicensePresenter extends Presenter<LicenseView> {
    private static final String LICENSE_FILE_PATH_FORMAT = "license/license_%s.txt";

    @Inject Context mContext;

    @Inject
    public LicensePresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setText(getText()));
    }

    private String getText(){
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder text = new StringBuilder();

        try {
            try {
                // assetsフォルダ内の sample.txt をオープンする
                is = mContext.getAssets().open(String.format(LICENSE_FILE_PATH_FORMAT, mContext.getString(R.string.url_002)));
                br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF8")));

                // １行ずつ読み込み、改行を付加する
                String str;
                while ((str = br.readLine()) != null) {
                    text.append(str).append("\n");
                }
            } finally {
                if (is != null) is.close();
                if (br != null) br.close();
            }
        } catch (Exception e){
            Timber.e(e);
        }
        return text.toString();
    }
}
