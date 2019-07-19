package io.choerodon.kb.infra.annotation;

import java.lang.annotation.*;

/**
 * Created by Zenger on 2019/5/16.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface DataLog {

    String type() default "";

    boolean single() default true;

}
