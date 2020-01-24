package jp.pioneer.carsync.domain.model;

import android.content.Context;
import android.content.Intent;

public interface BaseApp {
    String getPackageName();
    Intent createMainIntent(Context context);
}
