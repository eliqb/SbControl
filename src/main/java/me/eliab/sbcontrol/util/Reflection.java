package me.eliab.sbcontrol.util;

import org.bukkit.Bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utility class for working with reflection, providing methods for obtaining classes,
 * fields, methods, and creating instances dynamically.
 *
 * <p>
 * This class aims to simplify common tasks involving reflection in a Bukkit/Spigot
 * environment, providing methods to work with NMS (net.minecraft.server) and Bukkit classes.
 * </p>
 */
public class Reflection {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final String PACKAGE_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private static volatile Object theUnsafe;
    private static boolean repackage;

    private Reflection() {
        throw new UnsupportedOperationException("Reflection is a utility class therefore it cannot be instantiated");
    }

    static {

        try {
            Class.forName("net.minecraft.network.protocol.Packet");
            repackage = true;
        } catch (ClassNotFoundException e) {
            repackage = false;
        }

    }

    public static Optional<Class<?>> getOptionalNmsClass(String path_1_17, String className) {
        try {
            return Optional.of(getNmsClass(path_1_17, className));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    public static Class<?> getNmsClass(String path_1_17, String className) throws ClassNotFoundException {

        String path = "net.minecraft.";

        if(repackage) {
            path += path_1_17 != null ? (path_1_17 + ".") : "";
        } else {
            path += "server." + PACKAGE_VERSION + ".";
        }

        return Class.forName(path + className);

    }

    public static Class<?> getBukkitClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + PACKAGE_VERSION + "." + className);
    }

    public static Enum<?>[] getEnumConstants(Class<?> clazz) {
        return clazz.asSubclass(Enum.class).getEnumConstants();
    }

    public static MethodHandle getMethod(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameters)
            throws NoSuchMethodException, IllegalAccessException {

        MethodType methodType = MethodType.methodType(returnType, parameters);
        return LOOKUP.findVirtual(clazz, methodName, methodType);

    }

    public static MethodHandle findFieldGetter(Class<?> clazz, Class<?> fieldType) throws NoSuchFieldException, IllegalAccessException {

        return toMethodHandle(Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getType() == fieldType)
                .findFirst()
                .orElseThrow(NoSuchFieldException::new));

    }

    public static MethodHandle findMethod(Class<?> clazz, Class<?> returnType, Class<?>... parameters) throws NoSuchMethodException, IllegalAccessException {

        return toMethodHandle(Stream.of(clazz.getMethods())
                .filter(method -> method.getReturnType() == returnType && Arrays.equals(method.getParameterTypes(), parameters))
                .findFirst()
                .orElseThrow(NoSuchMethodException::new));

    }

    public static MethodHandle toMethodHandle(Field field) throws IllegalAccessException {
        field.setAccessible(true);
        return LOOKUP.unreflectGetter(field);
    }

    public static MethodHandle toMethodHandle(Method method) throws IllegalAccessException {
        method.setAccessible(true);
        return LOOKUP.unreflect(method);
    }

    public static Object createInstance(Class<?> clazz) throws Throwable {

        try {

            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();

        } catch (ReflectiveOperationException ignored) {
            // allocate instance with theUnsafe
        }

        MethodType allocateMethodType = MethodType.methodType(Object.class, Class.class);
        MethodHandle allocateMethod = LOOKUP.findVirtual(getTheUnsafe().getClass(), "allocateInstance", allocateMethodType);
        return allocateMethod.invoke(getTheUnsafe(), clazz);

    }

    public static boolean isRepackage() {
        return repackage;
    }

    public static Object getTheUnsafe() {

        if (theUnsafe == null) {
            synchronized (Reflection.class) {
                if (theUnsafe == null) {

                    try {
                        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                        Field field = unsafeClass.getDeclaredField("theUnsafe");
                        field.setAccessible(true);
                        theUnsafe = field.get(null);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }

                }
            }

        }
        return theUnsafe;

    }

}
