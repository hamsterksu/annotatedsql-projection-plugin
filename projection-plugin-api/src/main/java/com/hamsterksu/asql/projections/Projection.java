package com.hamsterksu.asql.projections;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by hamsterksu on 19.10.2014.
 */
@Target(ElementType.FIELD)
public @interface Projection {

    String[] value() default {};
}
