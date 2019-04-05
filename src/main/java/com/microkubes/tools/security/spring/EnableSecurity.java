package com.microkubes.tools.security.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables the full security configuration integrated with Microkubes platform.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JwtOauth2Config.class)
public @interface EnableSecurity {
}
