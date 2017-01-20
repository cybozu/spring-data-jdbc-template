package com.cybozu.spring.data.jdbc.core.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

public final class BeanFactoryUtil {
    private BeanFactoryUtil() {
        throw new AssertionError();
    }

    public static <T> T getBeanByNameOrType(BeanFactory beanFactory, String name, Class<T> requiredType) {
        if (StringUtils.isEmpty(name)) {
            return beanFactory.getBean(requiredType);
        } else {
            return beanFactory.getBean(name, requiredType);
        }
    }
}
