package edu.umd.cs.findbugs.annotations;
/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates that all members of the class or package should be annotated with the default value of the supplied
 * annotation class. This would be used for behavior annotations such as @NonNull, @CheckForNull,
 * or @CheckReturnValue. In particular, you can use @DefaultAnnotation(NonNull.class) on a class or package,
 * and then use @Nullable only on those parameters, methods or fields that you want to allow to be null.
 *
 * @deprecated use the JSR305 annotations instead
 */
@Documented
@Target(value = {TYPE, PACKAGE})
@Retention(value = CLASS)
@Deprecated
public @interface DefaultAnnotation {
    /**
     * Annotation class objects. More than one class can be specified.
     *
     * @return Annotation class objects. More than one class can be specified.
     */
    Class<? extends Annotation>[] value();

    /**
     * Default priority.
     *
     * @return Default priority.
     * @deprecated use {@link #confidence()} instead
     */
    @Deprecated
    Priority priority() default Priority.MEDIUM;

    /**
     * Default confidence.
     *
     * @return Default confidence.
     */
    Confidence confidence() default Confidence.MEDIUM;

}
