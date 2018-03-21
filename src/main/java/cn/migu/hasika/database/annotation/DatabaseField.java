package cn.migu.hasika.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.migu.hasika.database.DatabaseColumnInfo;

/**
 * Created by hasika on 2018/3/14.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {
    String value();
    int type() default DatabaseColumnInfo.ColumnTypeConstant.TEXT;
    DatabaseConstraints constraints() default @DatabaseConstraints;
}
