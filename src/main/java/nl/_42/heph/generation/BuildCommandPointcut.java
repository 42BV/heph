package nl._42.heph.generation;

import java.lang.reflect.Method;

import org.springframework.aop.support.StaticMethodMatcherPointcut;

/**
 * AOP Pointcut which matches the following:
 * - Methods which start with "with" AND have 1 argument
 * - Custom (in interface terms: default) methods.
 */
public class BuildCommandPointcut extends StaticMethodMatcherPointcut {

    public static final String WITH_PREFIX = "with";

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return (method.getName().startsWith(WITH_PREFIX) && method.getParameterCount() == 1) || method.isDefault();
    }
}
