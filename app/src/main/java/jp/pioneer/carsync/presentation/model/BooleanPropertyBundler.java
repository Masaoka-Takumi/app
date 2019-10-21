package jp.pioneer.carsync.presentation.model;


import android.os.Bundle;

import icepick.Bundler;

/**
 * Created by BP06565 on 2017/02/16.
 */

public class BooleanPropertyBundler implements Bundler<Property<Boolean>> {
    @Override
    public void put(String key, Property<Boolean> property, Bundle bundle) {
        if (property.peekValue() != null) {
            bundle.putBoolean(key, property.peekValue());
        }
    }

    @Override
    public Property<Boolean> get(String key, Bundle bundle) {
        Property<Boolean> property = new Property<>();
        if (bundle.containsKey(key)) {
            property.setValue(bundle.getBoolean(key));
        }
        return property;
    }
}
