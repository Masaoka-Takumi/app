package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AlexaDisplayCardPresenter;
import jp.pioneer.carsync.presentation.view.AlexaDisplayCardView;

import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderTemplateItem;
import jp.pioneer.mbg.alexa.CustomVoiceChromeView;
import timber.log.Timber;

/**
 * AlexaのDisplayCard
 */
public class AlexaDisplayCardFragment extends AbstractDialogFragment<AlexaDisplayCardPresenter, AlexaDisplayCardView, AbstractDialogFragment.Callback>
        implements AlexaDisplayCardView {
    @Inject
    AlexaDisplayCardPresenter mPresenter;
    @BindView(R.id.alexa_start_button_group)
    RelativeLayout mAlexaBtnGroup;
    @BindView(R.id.alexa_start_button)
    ImageView mAlexaBtn;
    @BindView(R.id.alexa_notification_circle)
    ImageView mAlexaNotification;
    @BindView(R.id.alexa_voice_chrome_large)
    CustomVoiceChromeView mVoiceChrome;
    @BindView(R.id.text_field)
    TextView mTextField;
    @BindView(R.id.close_button)
    ImageView mCloseBtn;
    @BindView(R.id.image)
    ImageView mImage;
    @BindView(R.id.body_template2)
    ConstraintLayout mBodyTemplate2;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public AlexaDisplayCardFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return AlexaDisplayCardFragment
     */
    public static AlexaDisplayCardFragment newInstance(android.support.v4.app.Fragment target, Bundle args) {
        AlexaDisplayCardFragment fragment = new AlexaDisplayCardFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setCancelable(false);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.BehindScreenStyle);
        return dialog;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof AlexaDisplayCardFragment.Callback;
    }

    @NonNull
    @Override
    protected AlexaDisplayCardPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alexa_display_card, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick(R.id.alexa_start_button)
    public void onClickAlexaBtn() {
        getPresenter().startAlexa();
    }

    @OnClick(R.id.close_button)
    public void onClickDismissBtn() {
        callbackClose();
    }

    @Override
    public void setTemplate(final RenderTemplateItem renderTemplateItem) {
        Timber.d("setTemplate:type=" + renderTemplateItem.type);
        switch (renderTemplateItem.type) {
            case "BodyTemplate2":
                setBodyTemplate2(renderTemplateItem);
                AlphaAnimation animation = new AlphaAnimation(0, 1);
                animation.setDuration(1000);
                mBodyTemplate2.startAnimation(animation);
                break;
            case "ListTemplate1":
                break;
            case "WeatherTemplate":
                break;
        }
    }

    private void setBodyTemplate2(final RenderTemplateItem renderTemplateItem) {
        mTextField.setText(renderTemplateItem.textField);
        if (renderTemplateItem.image != null) {
            AlexaIfDirectiveItem.Source source = null;
            AlexaIfDirectiveItem.ImageStructure imageStructure = renderTemplateItem.image;
            List<AlexaIfDirectiveItem.Source> sources = imageStructure.getSources();
            if (sources != null && sources.size() > 0) {
                //small→...→x-largeと仮定して、Listの最後の画像(Large)を取得
                int logoSize = sources.size() - 1;
                source = sources.get(logoSize);
            }
            String imageUrl = null;
            if (source != null) {
                imageUrl = source.getUrl();
            }
            if (mImage != null) {
                if (imageUrl != null) {
                    setImage(Uri.parse(imageUrl));
                } else {
                    setImage(null);
                }
            }
        }
    }

    private void setImage(Uri uri) {
        Glide.with(getContext())
                .load(uri)
                .error(R.drawable.p0070_noimage)
                .into(mImage);
    }
}
