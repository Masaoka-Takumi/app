package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import jp.pioneer.carsync.presentation.presenter.NavigationPresenter;
import jp.pioneer.carsync.presentation.view.NavigationView;
import jp.pioneer.carsync.presentation.view.adapter.NavigationAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Navigation設定の画面
 */

public class NavigationFragment extends AbstractScreenFragment<NavigationPresenter, NavigationView>
        implements NavigationView {
    @Inject NavigationPresenter mPresenter;
    @BindView(R.id.list_view) ExpandableListView mListView;
    private NavigationAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public NavigationFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return ImpactDetectionSettingsFragment
     */
    public static NavigationFragment newInstance(Bundle args) {
        NavigationFragment fragment = new NavigationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_navi, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mListView.setDivider(null);
        mListView.setGroupIndicator(null);
        mListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            if (groupPosition == NavigationAdapter.mixingSettingIndex) {
                boolean result = mAdapter.getMixingSetting();
                getPresenter().onMixingSettingAction(!result);
            } else if (groupPosition == NavigationAdapter.mixingVolumeIndex) {
                if(mAdapter.getMixingVolumeEnabled()) {
                    getPresenter().onMixingVolumeSettingAction();
                }
            }

            return true;
        });

        mListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Object clickObject = mAdapter.getChild(groupPosition, childPosition);
            if (clickObject != null &&
                    clickObject instanceof ApplicationInfo) {
                getPresenter().onNavigationAppSelectedAction((ApplicationInfo) clickObject);
            }
            return true;
        });
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
    protected NavigationPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_NAVIGATION;
    }

    @Override
    public void setAdapter(ArrayList<String> types, boolean isMarin) {
        mAdapter = new NavigationAdapter(getContext(), types, isMarin) {
            @Override
            protected void onClickSwitch(boolean setting) {
                getPresenter().onMixingSettingAction(setting);
            }
        };

        mListView.setAdapter(mAdapter);
        mListView.expandGroup(0);
        if(isMarin) {
            mListView.expandGroup(1);
            mListView.expandGroup(2);
        }
    }

    @Override
    public void setApplicationList(List<ApplicationInfo> apps, @Nullable ApplicationInfo selectedApp) {
        mAdapter.setApps(
                apps,
                selectedApp
        );
    }

    @Override
    public void setApplicationList(List<ApplicationInfo> weatherApps, List<ApplicationInfo> boatingApps, List<ApplicationInfo> fishingApps, @Nullable ApplicationInfo selectedApp) {
        mAdapter.setMarinApps(
                weatherApps,
                boatingApps,
                fishingApps,
                selectedApp
        );
    }

    @Override
    public void setMixingSetting(boolean isEnabled, boolean setting) {
        mAdapter.setMixingSetting(
                isEnabled,
                setting
        );
    }

    @Override
    public void setMixingVolumeSetting(boolean isEnabled, @Nullable NaviGuideVoiceVolumeSetting setting) {
        mAdapter.setMixingVolume(
                isEnabled,
                setting
        );
    }
}
