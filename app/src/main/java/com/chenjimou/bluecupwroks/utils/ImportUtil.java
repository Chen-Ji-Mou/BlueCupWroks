package com.chenjimou.bluecupwroks.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import com.chenjimou.bluecupwroks.annotation.FindViewById;
import com.chenjimou.bluecupwroks.annotation.IntentExtra;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ImportUtil {

    public static void importView(Activity activity){
        Class<? extends Activity> cls = activity.getClass();
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(FindViewById.class)) {
                FindViewById annotation = field.getAnnotation(FindViewById.class);
                if(annotation != null){
                    int id = annotation.id();
                    View view = activity.findViewById(id);
                    field.setAccessible(true);
                    try {
                        field.set(activity, view);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void importIntentExtras(Activity activity){
        Class<? extends Activity> cls = activity.getClass();
        //获得数据
        Intent intent = activity.getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        //获得此类所有的成员
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(IntentExtra.class)) {
                IntentExtra annotation = field.getAnnotation(IntentExtra.class);
                //获得key
                String key = TextUtils.isEmpty(annotation.key()) ? field.getName() : annotation.key();
                if (extras.containsKey(key)) {
                    Object obj = extras.get(key);
                    // todo Parcelable数组类型不能直接设置，其他的都可以.
                    //获得该字段的数组类型的Class对象，如果不是数组类型则为null
                    Class<?> componentType = field.getType().getComponentType();
                    //当前属性是数组并且是 Parcelable（子类）数组
                    if (field.getType().isArray() && Parcelable.class.isAssignableFrom(componentType)) {
                        Object[] objs = (Object[]) obj;
                        //创建对应类型的数组并由objs拷贝
                        Object[] objects = Arrays.copyOf(objs, objs.length, (Class<? extends Object[]>) field.getType());
                        obj = objects;
                    }
                    field.setAccessible(true);
                    try {
                        field.set(activity, obj);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
