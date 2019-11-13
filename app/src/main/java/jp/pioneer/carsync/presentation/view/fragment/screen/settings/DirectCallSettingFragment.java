package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.DirectCallSettingPresenter;
import jp.pioneer.carsync.presentation.view.DirectCallSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.wasabeef.glide.transformations.MaskTransformation;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * Phone機能設定の画面
 */
@RuntimePermissions
public class DirectCallSettingFragment extends AbstractScreenFragment<DirectCallSettingPresenter,DirectCallSettingView>
        implements DirectCallSettingView {
    @Inject DirectCallSettingPresenter mPresenter;
    private Unbinder mUnbinder;
    @BindView(R.id.name_text) TextView mName;
    @BindView(R.id.contact_icon) ImageView mContact;
    @BindView(R.id.number_type) ImageView mType;
    @BindView(R.id.number_text) TextView mNumber;
    @BindView(R.id.delete_button) RelativeLayout mDelete;
    @BindView(R.id.register_button) RelativeLayout mRegister;

    /**
     * コンストラクタ
     */
    public DirectCallSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return DirectCallSettingFragment
     */
    public static DirectCallSettingFragment newInstance(Bundle args) {
        DirectCallSettingFragment fragment = new DirectCallSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_direct_call, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DirectCallSettingFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        DirectCallSettingFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @OnPermissionDenied(Manifest.permission.READ_CONTACTS)
    public void deniedPermission() {
        setDisable();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected DirectCallSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.DIRECT_CALL_SETTING;
    }

    /**
     * 連絡先項目設定
     * @param name 連絡先名
     * @param photoUri 連絡先アイコン
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setContactItem(String name, Uri photoUri) {
        Glide.with(getContext())
                .load(photoUri)
                .error(R.drawable.p0071_nocontact)
                .bitmapTransform(new MaskTransformation(getContext(),R.drawable.p0071_nocontact))
                .into(mContact);
        mName.setText(name);
        mDelete.setEnabled(true);
        mDelete.setAlpha(1.0f);
    }

    /**
     * 番号項目設定
     * @param number 連絡先番号
     * @param type 連絡先タイプ
     */
    @Override
    public void setPhoneItem(String number, int type) {
        Glide.with(getContext())
                .load(type)
                .error(R.drawable.p0050_phone)
                .into(mType);
        mNumber.setText(number);
        mDelete.setEnabled(true);
        mDelete.setAlpha(1.0f);
    }

    /**
     * 無効設定
     */
    @Override
    public void setDisable() {
        mName.setText("");
        Glide.with(getContext())
                .load(0)
                .error(0)
                .into(mContact);
        Glide.with(getContext())
                .load(0)
                .error(0)
                .into(mType);
        mNumber.setText("");
        mDelete.setEnabled(false);
        mDelete.setAlpha(0.5f);

    }

    /**
     * 登録ボタン有効無効設定
     * @param isEnabled 有効無効
     */
    @Override
    public void setRegisterEnabled(boolean isEnabled) {
        if (isEnabled){
            mRegister.setAlpha(1.0f);
        }else{
            mRegister.setAlpha(0.5f);
        }
        mRegister.setEnabled(isEnabled);
    }

    /**
     * 削除ボタン
     */
    @OnClick(R.id.delete_button)
    public void onClickDelete() {
        getPresenter().onDeleteAction();
    }

    /**
     * 登録ボタン
     */
    @OnClick(R.id.register_button)
    public void onClickRegister() {
        getPresenter().onRegisterAction();
    }
}
