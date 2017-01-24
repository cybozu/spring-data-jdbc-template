package com.cybozu.spring.data.jdbc.template.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

public class EntityUtils {
    private EntityUtils() {
        throw new AssertionError();
    }

    private static final Set<String> OBJECT_METHOD_NAMES = Collections.unmodifiableSet(Stream
            .of(Object.class.getDeclaredMethods()).map(Method::getName).collect(Collectors.toSet()));

    public static String columnName(Accessor accessor) {
        Column c = accessor.getAnnotation(Column.class);
        if (c != null && !c.name().isEmpty()) {
            return c.name();
        }
        return accessor.getName();
    }

    public static String tableName(Class<?> klass) {
        Table t = klass.getAnnotation(Table.class);
        if (t != null) {
            return t.name();
        }
        return klass.getSimpleName();
    }

    private static boolean isObjectMethod(Method method) {
        return OBJECT_METHOD_NAMES.contains(method.getName()) && ReflectionUtils.isObjectMethod(method);
    }

    private static List<Accessor> getPropertyAccessors(Class<?> klass) {
        return Arrays.stream(BeanUtils.getPropertyDescriptors(klass)).filter(pd -> !isObjectMethod(pd.getReadMethod()))
                .map(pd -> Accessors.ofProperty(klass, pd))
                .filter(accessor -> accessor.getAnnotation(Transient.class) == null && !accessor.isTransient())
                .collect(Collectors.toList());
    }

    private static List<Accessor> getFieldAccessors(Class<?> klass) {
        return Arrays.stream(klass.getFields()).filter(f -> !Modifier.isStatic(f.getModifiers()))
                .map(Accessors::ofField)
                .filter(accessor -> accessor.getAnnotation(Transient.class) == null && !accessor.isTransient())
                .collect(Collectors.toList());
    }

    private static Map<String, Accessor> columnMap(Class<?> klass, boolean includesGeneratedValue) {

        List<Accessor> accessors = new ArrayList<>();
        accessors.addAll(getPropertyAccessors(klass));
        accessors.addAll(getFieldAccessors(klass));

        Map<String, Accessor> result = new HashMap<>();

        for (Accessor accessor : accessors) {
            String columnName = columnName(accessor);
            if (!result.containsKey(columnName)
                    && (includesGeneratedValue || accessor.getAnnotation(GeneratedValue.class) == null)) {
                result.put(columnName, accessor);
            }
        }
        return result;
    }

    public static List<Accessor> getAccessors(Class<?> klass) {
        return new ArrayList<>(columnMap(klass, true).values());
    }

    private static Map<String, Accessor> keyMap(Class<?> klass) {
        Map<String, Accessor> result = new HashMap<>();
        for (Map.Entry<String, Accessor> entry : columnMap(klass, true).entrySet()) {
            String columnName = entry.getKey();
            Accessor accessor = entry.getValue();
            if (accessor.getAnnotation(Id.class) != null) {
                result.put(columnName, accessor);
            }
        }
        return result;
    }

    public static List<String> keyNames(Class<?> klass) {
        return new ArrayList<>(keyMap(klass).keySet());
    }

    public static Set<String> columnNames(Class<?> klass) {
        return new HashSet<>(columnMap(klass, true).keySet());
    }

    public static List<String> columnNamesExceptKeys(Class<?> klass) {
        Set<String> columnNames = columnNames(klass);
        columnNames.removeAll(keyNames(klass));
        return new ArrayList<>(columnNames);
    }

    public static Map<String, Object> values(Object object, Class<?> klass, boolean includesGeneratedValue) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Accessor> entry : columnMap(klass, includesGeneratedValue).entrySet()) {
            Object value = entry.getValue().getValue(object);
            result.put(entry.getKey(), value);
        }
        return result;
    }
}
