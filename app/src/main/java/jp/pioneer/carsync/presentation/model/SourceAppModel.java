package jp.pioneer.carsync.presentation.model;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import java.util.List;

import javax.inject.Inject;

import icepick.Icepick;
import icepick.State;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;

/**
 * Created by NSW00_007906 on 2017/10/23.
 */
@PresenterLifeCycle
public class SourceAppModel {
    public Property<List<ApplicationInfo>> installedMessagingApps = new Property<>();
    @State(SparseBooleanArrayPropertyBundler.class)
    public Property<SparseBooleanArray> checkedItemPositions = new Property<>();

    @Inject
    public SourceAppModel() {
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        Icepick.saveInstanceState(this, outState);
    }

    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        Icepick.restoreInstanceState(this, savedInstanceState);
    }
}
