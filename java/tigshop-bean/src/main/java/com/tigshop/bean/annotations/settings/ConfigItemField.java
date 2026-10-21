package com.tigshop.bean.annotations.settings;

import com.tigshop.bean.enums.setting.SettingsEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigItemField {

    SettingsEnum value();

}
