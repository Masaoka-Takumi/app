package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.pioneer.carsync.presentation.presenter.Presenter;

/**
 * Created by BP06566 on 2017/03/03.
 */

public abstract class AbstractTimeShiftRadioFragment<P extends Presenter<V>, V> extends AbstractRadioFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
