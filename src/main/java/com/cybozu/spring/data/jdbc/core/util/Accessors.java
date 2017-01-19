package com.cybozu.spring.data.jdbc.core.util;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

final class Accessors {
    private Accessors() {
        throw new AssertionError();
    }

    static Accessor ofProperty(Class<?> klass, PropertyDescriptor pd) {
        return new PropertyAccessor(klass, pd);
    }

    static Accessor ofField(Field field) {
        return new FieldAccessor(field);
    }

    static class PropertyAccessor implements Accessor {
        private final Class<?> klass;
        private final PropertyDescriptor propertyDescriptor;

        private PropertyAccessor(Class<?> klass, PropertyDescriptor pd) {
            this.klass = klass;
            this.propertyDescriptor = pd;
        }

        @Override
        public String getName() {
            return propertyDescriptor.getName();
        }

        @Override
        public Class<?> getValueType() {
            return propertyDescriptor.getPropertyType();
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            A result;

            Method readMethod = propertyDescriptor.getReadMethod();
            result = readMethod == null ? null : readMethod.getAnnotation(annotationClass);
            if (result != null) {
                return result;
            }

            Method writeMethod = propertyDescriptor.getWriteMethod();
            result = writeMethod == null ? null : writeMethod.getAnnotation(annotationClass);
            if (result != null) {
                return result;
            }

            Field f = ReflectionUtils.findField(klass, propertyDescriptor.getName());
            result = f.getAnnotation(annotationClass);
            return result;
        }

        @Override
        public Object getValue(Object target) {
            return ReflectionUtils.invokeMethod(propertyDescriptor.getReadMethod(), target);
        }

        @Override
        public void setValue(Object target, Object value) {
            ReflectionUtils.invokeMethod(propertyDescriptor.getWriteMethod(), target, value);
        }
    }

    static class FieldAccessor implements Accessor {
        private final Field field;

        private FieldAccessor(Field field) {
            this.field = field;
        }

        @Override
        public Class<?> getValueType() {
            return field.getType();
        }

        @Override
        public String getName() {
            return field.getName();
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            return field.getAnnotation(annotationClass);
        }

        @Override
        public Object getValue(Object target) {
            return ReflectionUtils.getField(field, target);
        }

        @Override
        public void setValue(Object target, Object value) {
            ReflectionUtils.setField(field, target, value);
        }
    }
}
