package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.SourceAppSettingPresenter;
import jp.pioneer.carsync.presentation.view.SourceAppSettingView;
import jp.pioneer.carsync.presentation.view.adapter.SourceAppAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;


/**
 * Source App Setting の画面
 */

public class SourceAppSettingFragment extends AbstractScreenFragment<SourceAppSettingPresenter, SourceAppSettingView> implements SourceAppSettingView {
    @Inject SourceAppSettingPresenter mPresenter;
    @BindView(R.id.directory_pass_text) TextView mPass;
    @BindView(R.id.list_view) ListView mListView;
    private SourceAppAdapter mAdapter;
    private Unbinder mUnbinder;


    public static SourceAppSettingFragment newInstance(Bundle args) {
        SourceAppSettingFragment fragment = new SourceAppSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_source_app_setting, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        view.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if(event.getAction() == KeyEvent.ACTION_UP) {
                    getPresenter().onBackAction();
                }
                return true;
            }
            return false;
        });
        view.setFocusableInTouchMode(true);
        view.requestFocus();

        mAdapter = new SourceAppAdapter(getContext());
        mListView.setEmptyView(view.findViewById(R.id.empty_view));
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener((parent, v, groupPosition, id) -> {
            CheckBox chk = (CheckBox) v.findViewById(R.id.checkBox1);
            chk.setChecked(!chk.isChecked());
            mPresenter.onMusicAppDecided();
            return;
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
    protected SourceAppSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SOURCE_APP_SETTING;
    }

    @Override
    public void setInstalledMusicApps(List<ApplicationInfo> musicApps) {
        mAdapter.clear();
        mAdapter.addAll(musicApps);
    }

    @Override
    public void setCheckedItemPositions(SparseBooleanArray positions) {
        for (int i = 0; i < positions.size(); i++) {
            if (positions.valueAt(i)) {
                mListView.setItemChecked(positions.keyAt(i), true);
            }
        }
        mAdapter.setCheckedPositions(positions);
    }

    @Override
    public SparseBooleanArray getCheckedItemPositions() {
        return mListView.getCheckedItemPositions();
    }

    @Override
    public void setPass(String pass) {
        mPass.setText(pass);
    }

    @OnClick(R.id.back_button)
    public void onClickBackButton(View view) {
        getPresenter().onBackAction();
    }
}
