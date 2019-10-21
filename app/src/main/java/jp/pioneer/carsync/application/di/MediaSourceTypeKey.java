package jp.pioneer.carsync.application.di;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import dagger.MapKey;
import jp.pioneer.carsync.domain.model.MediaSourceType;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * MediaSourceTypeと何かのMapをDIしたい場合に使用するMapKeyアノテーション.
 */
@Documented
@MapKey
@Retention(RUNTIME)
public @interface MediaSourceTypeKey {
    MediaSourceType value();
}
