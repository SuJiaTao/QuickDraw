// Bailey JT Brown
// 2026
// QImmutable.java

package ca.bael.QDraw;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Instances never change after construction; they are safe to share and retain. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QImmutable { }
