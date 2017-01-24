package com.cybozu.spring.data.jdbc.template.util;

import java.lang.annotation.Annotation;

public interface Accessor {
    String getName();

    Class<?> getValueType();

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    Object getValue(Object target);

    void setValue(Object target, Object value);

    boolean isTransient();
}
