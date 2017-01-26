package com.cybozu.spring.data.jdbc.template.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.annotation.QueryAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@QueryAnnotation
public @interface Query {
    String value();
}
