package nl._42.heph.generation;

import static java.lang.String.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nl._42.heph.DefaultBuildCommand;
import nl._42.heph.lazy.EntityField;
import nl._42.heph.lazy.EntityId;
import nl._42.heph.lazy.Resolve;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * AOP advice which handles called interface methods as following:
 * - If the method is "default", i.e. has a custom implementation, call the original implementation.
 * - If the method starts with "with", resolve the affected field name and pass the call through the base implementation (DefaultBuildCommand).
 */
public class BuildCommandAdvice implements MethodInterceptor {

    private final DefaultBuildCommand<?, ?> buildCommand;

    private Object proxy;

    public BuildCommandAdvice(DefaultBuildCommand<?, ?> buildCommand) {
        this.buildCommand = buildCommand;
    }

    // We need to store a reference of the proxied class (with this Advice within it) to allow calling original methods in case of default interface method implementations
    public void setProxy(Object proxy) {
        Assert.notNull(proxy, "Proxied instance cannot be null");
        this.proxy = proxy;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Method method = invocation.getMethod();
        final Object[] args = invocation.getArguments();

        // If the called method is a default method (a.k.a. has a custom implementation), call the original method definition.
        if (method.isDefault()) {
            MethodHandle handle = getMethodHandle(method);
            return handle.bindTo(proxy).invokeWithArguments(args);
        } else {
            // Otherwise, set the generated field value to the internal entity.
            if (args == null || args.length == 0) {
                throw new IllegalArgumentException(format("Failed to resolve method [%s] in buildCommand of type [%s]: Expected one argument or varargs type, but got none", method.getName(), method.getDeclaringClass().getName()));
            }

            if (args.length == 1) {
                Object argumentValue = args[0];
                String fieldName = getAffectedFieldName(method);
                Resolve resolveAnnotation = method.getAnnotation(Resolve.class);
                EntityField entityFieldAnnotation = method.getAnnotation(EntityField.class);
                EntityId entityIdAnnotation = method.getAnnotation(EntityId.class);
                return buildCommand.withValue(fieldName, argumentValue, resolveAnnotation, entityFieldAnnotation, entityIdAnnotation);
            }

            throw new IllegalArgumentException(format("Failed to resolve method [%s] in buildCommand of type [%s]: Expected one argument or varargs type, but got multiple arguments", method.getName(), method.getDeclaringClass().getName()));
        }
    }

    /**
     * Returns the field name to set in the entity of a "with method".
     * Example: Method name = "withFirstName". Property name is then "firstName".
     * @param method Method to set property
     * @return Field name
     */
    private String getAffectedFieldName(final Method method) {
        String fieldName = method.getName().substring(BuildCommandPointcut.WITH_PREFIX.length());
        return StringUtils.uncapitalize(fieldName);
    }

    /**
     * Returns a method handle to execute a default interface method.
     * This is required to directly execute the method in the proxied superclass of the DefaultBuildCommand (see https://cs.au.dk/~mis/dOvs/jvmspec/ref--33.html).
     * If the method were not to be directly executed, we would end up in an infinite loop between the proxied instance and this advice class.
     * @param method Method to get the method handle for
     * @return Method handle.
     */
    private static MethodHandle getMethodHandle(Method method) {
        final Class<?> declaringClass = method.getDeclaringClass();

        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE).unreflectSpecial(method, declaringClass);
        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("Could not retrieve method handle. Is the BuildCommand placed in a public interface?", e);
        }
    }
}
