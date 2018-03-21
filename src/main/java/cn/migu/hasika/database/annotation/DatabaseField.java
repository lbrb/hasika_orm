package cn.migu.hasika.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.migu.hasika.database.DatabaseColumnInfo;

/**
 * Author: hasika
 * Time: 2018/3/4
 * Any questions can send email to lbhasika@gmail.com
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {
    String value();
    int type() default DatabaseColumnInfo.ColumnTypeConstant.TEXT;
    DatabaseConstraints constraints() default @DatabaseConstraints;
}
