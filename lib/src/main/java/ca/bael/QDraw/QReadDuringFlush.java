// Bailey JT Brown
// 2026
// QReadDuringFlush.java

package ca.bael.QDraw;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Captured by reference when bound to a {@link QViewer}: do not modify between the {@code draw()} that captures it and the {@code flush()} that consumes it. */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QReadDuringFlush { }
