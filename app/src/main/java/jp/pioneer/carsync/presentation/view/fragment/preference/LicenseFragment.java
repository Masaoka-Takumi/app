package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.LicensePresenter;
import jp.pioneer.carsync.presentation.view.LicenseView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Licenceの画面
 */

public class LicenseFragment extends AbstractScreenFragment<LicensePresenter, LicenseView> implements LicenseView {
    @Inject LicensePresenter mPresenter;
    private Unbinder mUnbinder;
    @BindView(R.id.scroll_view) ScrollView mScrollView;
    @BindView(R.id.text_view) TextView mTextView;

    /**
     * コンストラクタ
     */
    public LicenseFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return LicenseFragment
     */
    public static LicenseFragment newInstance(Bundle args) {
        LicenseFragment fragment = new LicenseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_information_license, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected LicensePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.LICENSE;
    }

    @Override
    public void setText(String text) {
        mTextView.setText(text);
        mScrollView.post(() -> mScrollView.scrollTo(0, 0));
    }
}
