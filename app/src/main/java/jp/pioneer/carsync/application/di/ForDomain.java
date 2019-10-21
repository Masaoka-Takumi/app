package jp.pioneer.carsync.application.di;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Domain層に限定したDIを行いたい場合に使用するQualifierアノテーション.
 */
@Documented
@Qualifier
@Retention(RUNTIME)
public @interface ForDomain {
}
