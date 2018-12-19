package com.atguigu.gmall.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//@Target(ElementType.METHOD)//用在方法上的注解
//@Retention(RetentionPolicy.RUNTIME)//注解作用范围：运行时

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {
    boolean isNeededSuccess() default true;
}
