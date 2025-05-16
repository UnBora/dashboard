package com.boraun.dashboard.common.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RootAuthority {
    String name() default "";

    boolean menu() default true;

    String icon() default "";

}
