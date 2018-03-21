package cn.migu.hasika.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: hasika
 * Time: 2018/3/4
 * Any questions can send email to lbhasika@gmail.com
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseTable {
    String value();
}
