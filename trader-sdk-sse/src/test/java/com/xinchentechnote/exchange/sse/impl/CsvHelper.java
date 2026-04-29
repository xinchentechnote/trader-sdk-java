package com.xinchentechnote.exchange.sse.impl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvHelper {


    private static <T> Map<String, Method> createMethodMap(Class<T> clazz) {
        Method[] methods = clazz.getMethods();
        Map<String, Method> methodMap = new HashMap<>();
        for (Method method : methods) {
            String name = method.getName();
            if (method.getParameterCount()==1 && name.startsWith("set") && name.length() > 3) {
                if (method.getReturnType() != void.class && method.getReturnType() != Void.class) {
                    continue; // 只考虑返回类型为void的setter方法
                }
                String fieldName = name.substring(3);
                methodMap.put(fieldName, method);
                methodMap.put(fieldName.toLowerCase(), method);
            }
        }
        return methodMap;

    }

    public static <T> List<T> parse(String csvContent, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        Map<String,Method> methodMap = createMethodMap(clazz);
        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            // 读取表头
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return result;
            }
            String[] headers = headerLine.split(",");
            // 处理每一行数据
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length != headers.length) {
                    // 跳过列数不匹配的行
                    throw new IllegalArgumentException("CSV行列数不匹配: " + line);
                }

                T instance = clazz.getDeclaredConstructor().newInstance();
                // 设置字段值
                for (int i = 0; i < headers.length; i++) {
                    String header = headers[i].trim();
                    String value = values[i].trim();
                    if (value.isEmpty()) {
                        continue;
                    }
                    Method method = methodMap.get(header);
                    if (method == null) {
                        method = methodMap.get(header.toLowerCase());
                    }
                    if (method == null) {
                        System.out.println("Warning: No setter found for header '" + header.toLowerCase() + "' in class " + clazz.getName());
                        continue; // 没有找到对应的setter方法，跳过
                    }
                    method.invoke(instance, convertValue(value, method.getParameterTypes()[0]));
                }

                result.add(instance);
            }
        } catch (Exception e) {
            System.err.println("Error parsing CSV: " + e.getMessage());
        }

        return result;
    }


    private static Object convertValue(String value, Class<?> targetType) {
        if (value == null || value.isEmpty()) {
            return getDefaultValue(targetType);
        }

        try {
            if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(value.trim());
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(value.trim());
            } else if (targetType == byte.class || targetType == Byte.class) {
                return Byte.parseByte(value.trim());
            } else if (targetType == short.class || targetType == Short.class) {
                return Short.parseShort(value.trim());
            } else if (targetType == float.class || targetType == Float.class) {
                return Float.parseFloat(value.trim());
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(value.trim());
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(value.trim());
            } else if (targetType == char.class || targetType == Character.class) {
                return value.trim().charAt(0);
            } else if (targetType == String.class) {
                return value;
            } else {
                // 对于枚举或其他类型，可能需要特殊处理
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultValue(targetType);
        }
    }

    private static Object getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == float.class) return 0.0f;
            if (type == double.class) return 0.0;
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
        }
        return null;
    }
}