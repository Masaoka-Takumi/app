package jp.pioneer.carsync.application.di;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * FragmentComponentの生存期間でSingletonにしたい場合に付与するScopeアノテーション.
 * <p>
 * FragmentModule提供:
 * <pre>
 *  &#064;FragmentLifeCycle
 *  &#064;Provides
 *  public Foo provideFoo() {
 *      return new Foo();
 *  }
 * </pre>
 * FragmentModule非提供:
 * <pre>
 *  &#064;FragmentLifeCycle
 *  public class Bar {
 *      &#064;Inject
 *      public Bar() {
 *      }
 *  }
 * </pre>
 */
@Documented
@Scope
@Retention(RUNTIME)
public @interface FragmentLifeCycle {
}
