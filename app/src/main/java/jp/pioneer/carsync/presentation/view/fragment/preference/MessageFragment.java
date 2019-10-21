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
import jp.pioneer.carsync.domain.model.IncomingMessageColorSetting;
import jp.pioneer.carsync.presentation.presenter.MessagePresenter;
import jp.pioneer.carsync.presentation.view.MessageView;
import jp.pioneer.carsync.presentation.view.adapter.MessagingAppAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Message設定の画面
 */

public class MessageFragment extends AbstractScreenFragment<MessagePresenter, MessageView>
        implements MessageView {

    @Inject MessagePresenter mPresenter;
    @BindView(R.id.list_view) ExpandableListView mListView;
    private MessagingAppAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    @Inject
    public MessageFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return MessageFragment
     */
    public static MessageFragment newInstance(Bundle args) {
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_message, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mListView.setDivider(null);
        mListView.setGroupIndicator(null);
        mListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            if (groupPosition == MessagingAppAdapter.MESSAGE_READING) {
                boolean result = mAdapter.getMessageReading();
                mPresenter.onSwitchReadNotificationEnabledChange(!result);
            } else if (groupPosition == MessagingAppAdapter.MESSAGE_COLOR) {
                if(mAdapter.isMessageColorEnabled()) {
                    mPresenter.onMessagingColorClickAction();
                }
            }

            return true;
        });

        mListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            if(mAdapter.getMessageReading()) {
                Object clickObject = mAdapter.getChild(groupPosition, childPosition);
                if (clickObject != null &&
                        clickObject instanceof ApplicationInfo) {
                    mPresenter.onMessageAppCheckedAction((ApplicationInfo) clickObject);
                }
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
    protected MessagePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_MESSAGE;
    }

    @Override
    public void setAdapter(ArrayList<String> types) {
        mAdapter = new MessagingAppAdapter(getContext(), types) {
            @Override
            protected void onClickSwitch(boolean setting) {
                mPresenter.onSwitchReadNotificationEnabledChange(setting);
            }
        };

        mListView.setAdapter(mAdapter);
        mListView.expandGroup(1);
    }

    @Override
    public void setApplicationList(List<ApplicationInfo> apps, List<ApplicationInfo> selectedApp) {
        mAdapter.setApps(apps, selectedApp);
    }

    @Override
    public void setMessageReading(boolean setting) {
        mAdapter.setMessageReading(setting);
    }

    @Override
    public void setMessageColor(boolean isEnabled, @Nullable IncomingMessageColorSetting setting) {
        mAdapter.setMessageColor(isEnabled, setting);
    }
}
