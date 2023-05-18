package io.github.kamarias.utils.bean;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * spring BeanUtils 增强
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/30 11:52
 */
public class BeanUtils  extends org.springframework.beans.BeanUtils {


    /**
     * Bean方法名中属性名开始的下标
     */
    private static final int BEAN_METHOD_PROP_INDEX = 3;

    /**
     * 匹配getter方法的正则表达式
     */
    private static final Pattern GET_PATTERN = Pattern.compile("get(\\p{javaUpperCase}\\w*)");

    /**
     * 匹配setter方法的正则表达式
     */
    private static final Pattern SET_PATTERN = Pattern.compile("set(\\p{javaUpperCase}\\w*)");

    /**
     * BeanCopier缓存Map
     */
    private static final Map<String, BeanCopier> BEAN_COPIER_MAP = new ConcurrentHashMap<>();

    /**
     * Flag结尾
     */
    private static final String FLAG_PATTERN = "Flag";

    private static Map<Integer,String> flag2StringMap = new HashMap();
    private static Map<String,Integer> string2flagMap = new HashMap();

    static {
        flag2StringMap.put(1,"是");
        flag2StringMap.put(0,"否");
        flag2StringMap.put(2,"未填写");

        string2flagMap.put("是",1);
        string2flagMap.put("否",0);
        string2flagMap.put("未填写",2);

    }

    /**
     * Bean属性复制工具方法。
     *
     * @param dest 目标对象
     * @param src  源对象
     */
    public static void copyBeanProp(Object dest, Object src) {
        try {
            copyProperties(src, dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Bean属性复制工具方法。
     *
     * @param dest 目标对象
     * @param src  源对象
     */
    public static void copyBeanPropWithFlag(Object dest, Object src) {
//        copyPropertiesFlag(dest,src);

        // 赋值Flag变更值
        copyPropertiesFlag(dest,src,(Class)null, (String[])null);
    }

    private static void copyPropertiesFlag(Object source, Object target, @Nullable Class<?> editable, @Nullable String... ignoreProperties) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");
        Class<?> actualEditable = target.getClass();
        if (editable != null) {
            if (!editable.isInstance(target)) {
                throw new IllegalArgumentException("Target class [" + target.getClass().getName() + "] not assignable to Editable class [" + editable.getName() + "]");
            }

            actualEditable = editable;
        }

        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
        List<String> ignoreList = ignoreProperties != null ? Arrays.asList(ignoreProperties) : null;
        PropertyDescriptor[] var7 = targetPds;
        int var8 = targetPds.length;

        for(int var9 = 0; var9 < var8; ++var9) {
            PropertyDescriptor targetPd = var7[var9];
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                        try {
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }

                            Object value = readMethod.invoke(source);
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }

                            writeMethod.invoke(target, value);
                        } catch (Throwable var15) {
                            throw new FatalBeanException("Could not copy property '" + targetPd.getName() + "' from source to target", var15);
                        }
                    }else if(readMethod != null && isFlagEnd(sourcePd.getName())){
                        // flag结尾的
                        try {
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            Object value = readMethod.invoke(source);

                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }

                            // 如果类型是整型 使用字典赋值
                            if(value.getClass() == Integer.class){
                                String strValue = flag2StringMap.get(value);
                                if(strValue!=null){
                                    writeMethod.invoke(target, strValue);
                                }
                            }
                        } catch (Throwable var15) {
                            throw new FatalBeanException("Could not copy property '" + targetPd.getName() + "' from source to target", var15);
                        }
                    }
                }
            }
        }

    }

    /**
     * 获取对象的setter方法。
     *
     * @param obj 对象
     * @return 对象的setter方法列表
     */
    public static List<Method> getSetterMethods(Object obj) {
        // setter方法列表
        List<Method> setterMethods = new ArrayList<Method>();

        // 获取所有方法
        Method[] methods = obj.getClass().getMethods();

        // 查找setter方法

        for (Method method : methods) {
            Matcher m = SET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 1)) {
                setterMethods.add(method);
            }
        }
        // 返回setter方法列表
        return setterMethods;
    }

    /**
     * 获取对象的getter方法。
     *
     * @param obj 对象
     * @return 对象的getter方法列表
     */

    public static List<Method> getGetterMethods(Object obj) {
        // getter方法列表
        List<Method> getterMethods = new ArrayList<Method>();
        // 获取所有方法
        Method[] methods = obj.getClass().getMethods();
        // 查找getter方法
        for (Method method : methods) {
            Matcher m = GET_PATTERN.matcher(method.getName());
            if (m.matches() && (method.getParameterTypes().length == 0)) {
                getterMethods.add(method);
            }
        }
        // 返回getter方法列表
        return getterMethods;
    }

    /**
     * 检查Bean方法名中的属性名是否相等。<br>
     * 如getName()和setName()属性名一样，getName()和setAge()属性名不一样。
     *
     * @param m1 方法名1
     * @param m2 方法名2
     * @return 属性名一样返回true，否则返回false
     */

    public static boolean isMethodPropEquals(String m1, String m2) {
        return m1.substring(BEAN_METHOD_PROP_INDEX).equals(m2.substring(BEAN_METHOD_PROP_INDEX));
    }

    /**
     * 复制对象且跳过源对象中的null值
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyBean(Object source, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 复制对象且跳过源对象中的null值
     *
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreProperties 忽略的属性
     */
    public static void copyBean(Object source, Object target, @Nullable String... ignoreProperties) {
        org.springframework.beans.BeanUtils.copyProperties(source, target, ArrayUtils.addAll(getNullPropertyNames(source), ignoreProperties));
    }

    /**
     * 复制对象且替换源对象中的localDateTIme与localDate
     *
     * @param source           源对象
     * @param target           目标对象
     * @param ignoreProperties 忽略的属性
     */
    public static void copyBeanCoverLocalDateTime(Object source, Object target, @Nullable String... ignoreProperties) {

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        Class<?> actualEditable = target.getClass();
        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);

        for (PropertyDescriptor targetPd : targetPds) {
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod == null) {
                continue;
            }
            if (ignoreList == null || !ignoreList.contains(targetPd.getName())) {
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    System.out.println(sourcePd.getReadMethod().getName());
                    Class<?> propertyType = sourcePd.getPropertyType();
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null &&
                            ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                        try {
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            Object value = readMethod.invoke(source);
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            writeMethod.invoke(target, value);

                        } catch (Throwable ex) {
                            throw new FatalBeanException(
                                    "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                        }
                    } else {
                        try {
                            if (readMethod != null) {
                                if (propertyType.equals(LocalDateTime.class)) {
                                    Object value = readMethod.invoke(source);
                                    if (value instanceof LocalDateTime) {
                                        ZoneId zoneId = ZoneId.systemDefault();
                                        ZonedDateTime zdt = ((LocalDateTime) value).atZone(zoneId);
                                        writeMethod.invoke(target, Date.from(zdt.toInstant()));
                                    }
                                } else if (propertyType.equals(LocalDate.class)) {
                                    Object value = readMethod.invoke(source);
                                    if (value instanceof LocalDate) {
                                        ZoneId zoneId = ZoneId.systemDefault();
                                        ZonedDateTime zdt = ((LocalDate) value).atStartOfDay(zoneId);
                                        writeMethod.invoke(target, Date.from(zdt.toInstant()));
                                    }
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取null值的属性名数组
     *
     * @param source 源对象
     * @return
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        // null值的属性集
        Set<String> emptyNames = new HashSet<>(pds.length);
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                // 值为null跳过
                emptyNames.add(pd.getName());
            } else {
                if (Iterable.class.isAssignableFrom(srcValue.getClass())) {
                    // 属性类型为可迭代容器，判断容器是否为空
                    Iterable iterable = (Iterable) srcValue;
                    Iterator iterator = iterable.iterator();
                    if (!iterator.hasNext()) {
                        emptyNames.add(pd.getName());
                    }
                }
                if (Map.class.isAssignableFrom(srcValue.getClass())) {
                    // 属性类型为Map容器，判断Map是否为空
                    Map map = (Map) srcValue;
                    if (map.isEmpty()) {
                        emptyNames.add(pd.getName());
                    }
                }
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 快速复制对象属性(使用cglib,null值会覆盖原有值)
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void fastCopyProperties(Object source, Object target) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");

        BeanCopier beanCopier = getBeanCopier(source.getClass(), target.getClass());
        beanCopier.copy(source, target, null);
    }

    /**
     * 获取或新增缓存Map中的BeanCopier对象
     *
     * @param source 源对象类型
     * @param target 目标对象类型
     * @return 缓存Map中的BeanCopier
     */
    private static BeanCopier getBeanCopier(Class<?> source, Class<?> target) {
        String key = generateKey(source, target);
        return BEAN_COPIER_MAP.computeIfAbsent(key, x -> BeanCopier.create(source, target, false));
    }

    /**
     * 生成缓存Map中的key
     *
     * @param source 源对象类型
     * @param target 目标对象类型
     * @return 缓存Map中的key
     */
    private static String generateKey(Class<?> source, Class<?> target) {
        return source.getCanonicalName().concat(target.getCanonicalName());
    }

    /**
     * 转换对象到指定类
     *
     * @param source      源对象
     * @param targetClass 目标类
     * @return 复制源对象属性的目标对象
     */
    public static <T> T convertBean(Object source, Class<T> targetClass) {
        T result;
        try {
            result = targetClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("fail to create instance of type" + targetClass.getCanonicalName(), e);
        }
        if (!Objects.isNull(source)) {
            fastCopyProperties(source, result);
        }
        return result;
    }

    /**
     * 转换对象List到指定类List
     *
     * @param sourceList  源对象List
     * @param targetClass 目标对象List
     * @param <S>         source对象类型
     * @param <T>         target对象类型
     * @return target对象List
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetClass) {
        if (!CollectionUtils.isEmpty(sourceList)) {
            List<T> targetList = new ArrayList<>(sourceList.size());
            sourceList.forEach(source -> targetList.add(convertBean(source, targetClass)));
            return targetList;
        }
        return Collections.EMPTY_LIST;
    }


    private static boolean isFlagEnd(String prop){
        int index = prop.indexOf(FLAG_PATTERN);
        return index>=0;
    }
}
