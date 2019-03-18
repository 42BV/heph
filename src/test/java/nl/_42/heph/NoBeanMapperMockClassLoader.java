package nl._42.heph;

/**
 * Mocked ClassLoader which does not load the class used to detect if BeanMapper is on the classpath.
 * This way, we can test if the copy() method throws the correct error in such cases.
 */
public class NoBeanMapperMockClassLoader extends ClassLoader {

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name == null || name.equals("io.beanmapper.BeanMapper")) {
            throw new NoClassDefFoundError();
        }

        return super.loadClass(name);
    }
}
