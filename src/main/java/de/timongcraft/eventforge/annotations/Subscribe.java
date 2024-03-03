package de.timongcraft.eventforge.annotations;

import de.timongcraft.eventforge.events.PostOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the method is an event handler.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

    /**
     * Devines the priority of the event handler
     *
     * @return the priority
     */
    PostOrder priority() default PostOrder.NORMAL;

}