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
 * Created by NSW00_008316 on 2017/04/07.
 */
@PresenterLifeCycle
public class MessagingAppModel {
    @State(BooleanPropertyBundler.class) public Property<Boolean> isReadNotificationEnabled = new Property<>();
    public Property<List<ApplicationInfo>> installedMessagingApps = new Property<>();
    @State(SparseBooleanArrayPropertyBundler.class)
    public Property<SparseBooleanArray> checkedItemPositions = new Property<>();

    @Inject
    public MessagingAppModel() {
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        Icepick.saveInstanceState(this, outState);
    }

    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        Icepick.restoreInstanceState(this, savedInstanceState);
    }
}
