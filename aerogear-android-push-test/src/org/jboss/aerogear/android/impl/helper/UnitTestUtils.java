/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.impl.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.Mockito;
import org.jboss.aerogear.android.impl.reflection.FieldNotFoundException;

public class UnitTestUtils {

    public static void setPrivateField(Object target, String fieldName,
            Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        List<Field> fields = getAllFields(new ArrayList<Field>(), target.getClass());

        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                field.set(target, value);
                return;
            }
        }

        throw new FieldNotFoundException(target.getClass(), fieldName);

    }

    public static Object getPrivateField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    public static Object getSuperPrivateField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    public static <T> T getPrivateField(Object target, String fieldName,
            Class<T> type) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    /**
     * 
     * This method extracts a named field, replaces it with a spy, and returns 
     * the spy.
     * 
     * @param target The object requiring a hot spy injection
     * @param fieldName the field to spy on
     * @param type The type of the spy
     * @return a spy which has replaced the field from fieldName
     * 
     * @param <T> the class of object which is being replaced with a spy.
     * 
     * @throws NoSuchFieldException if a field is not found. (Thrown from the java reflection API)
     * @throws IllegalArgumentException if an argument is illegal. (Thrown from the java reflection API)
     * @throws IllegalAccessException if access is exceptional.  (Thrown from the java reflection API)
     */
    public static <T> T replaceWithSpy(Object target, String fieldName,
            Class<T> type) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        T object = (T) field.get(target);
        object = Mockito.spy(object);
        setPrivateField(target, fieldName, object);
        return object;
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        Collections.addAll(fields, type.getDeclaredFields());

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    public static void callMethod(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(target);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static Object getPrivateEnum(Class<?> klass, String enumName, String constantName) {
        for (Class declaredClass : klass.getDeclaredClasses()) {
            if (declaredClass.getCanonicalName().equals(enumName)) {
                for (Object t : declaredClass.getEnumConstants()) {
                    if (t.toString().equals(constantName)) {
                        return t;
                    }
                }
            }
        }
        return null;
    }
}