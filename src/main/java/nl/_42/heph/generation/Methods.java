package nl._42.heph.generation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Methods {

    /**
     * Returns a method handle to execute a default interface method.
     * This is required to directly execute the method in the proxied superclass of the DefaultBuildCommand (see https://cs.au.dk/~mis/dOvs/jvmspec/ref--33.html).
     * If the method were not to be directly executed, we would end up in an infinite loop between the proxied instance and this advice class.
     * @param method Method to get the method handle for
     * @return Method handle.
     */
    public static MethodHandle getMethodHandle(Method method) {
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
