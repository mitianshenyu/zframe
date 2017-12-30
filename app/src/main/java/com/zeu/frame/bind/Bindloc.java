package com.zeu.frame.bind;

import android.os.Handler;
import android.view.View;

import com.zeu.frame.BuildConfig;

import java.lang.*;
import java.lang.Integer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Bindloc {
    private static Map<String, Module> mModules = new HashMap();

    public interface BindListener {
        void onBind(Object key, Object obj, Object tag, boolean cover);
        void onUnBind(Object key, Object obj, Object tag);
    }

    protected static class Instance {
        protected Object obj;
        protected Instance(Object obj) {this.obj = obj;}
        protected Object getObj() {return this.obj;}
    }

    public static class Module {
        private String mModelName = "";
        private Map<Object, Instance> mInstances = new HashMap();
        //监听可能先于对象存在,当对象绑定的时候会触发监听,用户不指定监听名称,则以每个对象的类名作为名称
        private static Map<Object, LinkedHashMap<BindListener, Object>> mLiteners = new HashMap();

        public Module(String modelName) {
            this.mModelName = modelName;
        }

        /**
         * 根据类获取,所有相关的对象,包括子类
         * @param key
         * @param subclass
         * @return
         */
        private Map<Object, Instance> getInstances(Object key, boolean subclass) {
            Map<Object, Instance> values = new LinkedHashMap<>();
            if (null != key) {
                for (Map.Entry<Object, Instance> entry : mInstances.entrySet()) {
                    Instance instance;
                    if (null != entry && (null != (instance = entry.getValue()))) {
                        if (null != instance && null != instance.getObj()) {
                            if (subclass) {
                                if (key instanceof Class) {
                                    if (((Class)key).isInstance(instance.getObj())) {
                                        values.put(entry.getKey(), instance);
                                    }
                                }
                            } else if (key.equals(entry.getKey())) {
                                values.put(entry.getKey(), instance);
                            }
                        }
                    }
                }
            }
            return values;
        }

        private List<Instance> getInstances() {
            return new ArrayList<>(mInstances.values());
        }

        protected Instance getInstance(Object key, boolean subclass, int index) {
            Map<Object, Instance> instances = getInstances(key, subclass);
            return (index < instances.size()) ? new ArrayList<>(instances.values()).get(index) : null;
        }

        /**
         * 判断对象是否绑定
         * @param instname
         * @return
         */
        public boolean isBinded(String instname) {
            return this.mInstances.containsKey(instname);
        }


        private void excBindListener(Object key, Object instance, Object param, int type) {
            if (null != key && null != instance) {
                if (key instanceof String) {
                    //直接根据key 查找监听函数, 并执行监听函数
                    LinkedHashMap<BindListener, Object> listeners = mLiteners.get(key);
                    if (null != listeners) {
                        for (LinkedHashMap.Entry<BindListener, Object> listener : listeners.entrySet()) {
                            if (null != listener) {
                                if (1 == type) {
                                    listener.getKey().onBind(key, instance, param, false);
                                } else if (2 == type) {
                                    listener.getKey().onBind(key, instance, param, true);
                                } else if (0 == type) {
                                    listener.getKey().onUnBind(key, instance, param);
                                }
                            }
                        }
                    }
                } else {
                    Class keyclass = (key instanceof Class) ? (Class) key : key.getClass();
                    //遍历判断对象与监听的key之间是是否相等或者是子类关系,相等则执行所有的监听,子类关系则遍历并执行所有支持子类的监听
                    for (Map.Entry<Object, LinkedHashMap<BindListener, Object>> entry : mLiteners.entrySet()) {
                        if (null != entry) {
                            //判断是否为类,是则判断是否为派生类
                            if (entry.getKey() instanceof String) {
                                //不处理, 当绑定的对象为class或者非String对象的时候, 而监听的key却为String,不处理
                            } else  {
                                Class listenerClass = (entry.getKey() instanceof Class) ? (Class) entry.getKey() : entry.getKey().getClass();
                                if (listenerClass.equals(keyclass)) {
                                    //相等则执行所有监听
                                    for (LinkedHashMap.Entry<BindListener, Object> listener : entry.getValue().entrySet()) {
                                        if (null != listener) {
                                            if (1 == type) {
                                                listener.getKey().onBind(key, instance, param, false);
                                            } else if (2 == type) {
                                                listener.getKey().onBind(key, instance, param, true);
                                            } else if (0 == type) {
                                                listener.getKey().onUnBind(key, instance, param);
                                            }
                                        }
                                    }
                                } else if (listenerClass.isAssignableFrom(keyclass)){
                                    //遍历子类,并执行监听
                                    for (LinkedHashMap.Entry<BindListener, Object> listener : entry.getValue().entrySet()) {
                                        if (null != listener) {
                                            boolean needSubclass = (listener.getValue() instanceof java.lang.Boolean) ? (boolean) listener.getValue() : false;
                                            if (needSubclass) {
                                                if (1 == type) {
                                                    listener.getKey().onBind(key, instance, param, false);
                                                } else if (2 == type) {
                                                    listener.getKey().onBind(key, instance, param, true);
                                                } else if (0 == type) {
                                                    listener.getKey().onUnBind(key, instance, param);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * 绑定对象
         * @param cover 是否覆盖之前已经绑定的对象
         * @param byclass 如果key为null, 是否以对象自己的类名作为key
         * @param key 索引对象到对象实例, 如果为null, 则根据byclass判断是否以类或者对象作为key
         *  @param param 参数, 以对象作为参数
         * @param values
         * @return
         */
        public boolean bind(Object key, boolean cover, boolean byclass, Object param, Object... values) {
            if (null != values && values.length > 0) {
                for (Object value : values) {
                    int type = 1;
                    key = (null == key) ? (byclass ? value.getClass() : value) : key;
                    Instance instance = mInstances.get(key);
                    if (instance == null) {
                        instance = new Instance(value);
                        mInstances.put(key, instance);
                    } else if (cover && !value.equals(instance.getObj())) {
                        instance.obj = value;
                        type = 2;
                    }

                    excBindListener(key, instance.obj, param, type);
                }
                return true;
            }
            return false;
        }

        /**
         * 根据类名, 给所有已注册的对象添加监听, 监听的名称以类名归类
         * 当监听以字符串归类,则直接根据字符串创建监听,并根据字符串查询对象
         * 当监听以类名归类,则先索引所有对象(包括子对象), 然后在依次绑定
         * @param listener
         * @param subclass
         * @param keys
         * @return
         */
        public boolean addBindListener(BindListener listener, Object param, boolean subclass, Object ...keys) {
            if (null != keys && null != listener) {
                for (Object key : keys) {
                    //当监听以字符串归类,则直接根据字符串创建监听,并根据字符串查询对象
                    LinkedHashMap<BindListener, Object> listeners = mLiteners.get(key);
                    if (null == listeners) {
                        listeners = new LinkedHashMap<>();
                        mLiteners.put(key, listeners);
                    }
                    if (!listeners.containsKey(listener)) {
                        listeners.put(listener, subclass);

                        //执行监听
                        if (subclass) {
                            Map<Object, Instance> instances = getInstances(key, subclass);
                            for (Map.Entry<Object, Instance> entry : instances.entrySet()) {
                                if (null != entry && null != entry.getValue()) {
                                    listener.onBind(entry.getKey(), entry.getValue().getObj(), param, false);
                                }
                            }
                        } else {
                            Instance instance = mInstances.get(key);
                            if (null != instance) {
                                listener.onBind(key, instance.getObj(), param, false);
                            }
                        }
                    }
                }
            }
            return false;
        }

        public boolean removeBindListener(BindListener listener, Object param) {
            List<Object> removes = new ArrayList<>();
            for (Map.Entry<Object, LinkedHashMap<BindListener, Object>> entrys : mLiteners.entrySet()) {
                if (null != entrys) {
                    LinkedHashMap<BindListener, Object> listeners = entrys.getValue();
                    Instance instobj = mInstances.get(entrys.getKey());
                    if (null != listeners && listeners.containsKey(listener)) {
                        listeners.remove(listener);
                        if (listeners.isEmpty()) {
                            removes.add(entrys.getKey());
                        }
                        listener.onUnBind(entrys.getKey(), (null != instobj) ? instobj.getObj() : null, param);
                    }
                }
            }

            for (Object obj : removes) {
                mLiteners.remove(obj);
            }
            return false;
        }

        /**
         * 解除绑定的对象,并遍历每一个监听的key,判断对象与监听的key之间是是否相等或者是子类关系,
         * 相等则执行所有的监听,子类关系则遍历并执行所有支持子类的监听
         * @param keys
         */
        public void unbind(Object param, Object... keys) {
            for (Object key : keys) {
                if (key != null) {
                    Instance instance = mInstances.get(key);
                    if (null != instance) {
                        mInstances.remove(key);
                        excBindListener(key, instance.obj, param, 0);
                    }
                }
            }
        }

        /**
         * 获取模块时间的名字
         * @param methodname
         * 如果用户指定了名字, 那么直接返回
         * 如果没有指定名字则以on+模块名+Event为函数
         * @return
         */
        private String getModelEventMethodName(String methodname) {
            if (methodname != null && !BuildConfig.FLAVOR.equals(methodname)) {
                return methodname;
            }
            if (this.mModelName.matches("^\\w+")) {
                return "on" + this.mModelName + "Event";
            }
            return "on" + Bindloc.class.getSimpleName() + "Event";
        }

        /**
         * 执行函数
         * @param declared 是否为私有
         * @param subclass 执行的是否为key的子类
         * @param key 可以为string, class, object
         * @param methodname 方法名字
         * @param args 参数
         * @return
         */
        private Object[] invokes(boolean declared, boolean subclass, Object key, String methodname, Object... args) {
            int index = 0;
            Map<Object, Instance> instances = (null != key) ? getInstances(key, subclass) : mInstances;
            Object[] rets = new Object[instances.size()];
            for (Map.Entry<Object, Instance> instance : instances.entrySet()) {
                rets[index++] = execMethod(declared, instance.getValue(), getModelEventMethodName(methodname), null, args);
            }
            return rets;
        }

        /**
         * 执行函数
         * @param subclass 执行的是否为key的子类
         * @param key 可以为string, class, object
         * @param methodname 方法名字
         * @param args 参数
         * @return
         */
        public Object[] postDeclareds(boolean subclass, Object key, String methodname, Object... args) {
            return invokes(true, subclass, key, methodname, args);
        }

        /**
         * 执行函数
         * @param subclass 执行的是否为key的子类
         * @param key 可以为string, class, object
         * @param methodname 方法名字
         * @param args 参数
         * @return
         */
        public Object[] posts(boolean subclass, Object key, String methodname, Object... args) {
            return invokes(false, subclass, key, methodname, args);
        }
    }

    /**
     * 判断模块空间是否注册
     * @param moduleNames
     * @return
     */
    public static boolean isRegistered(String... moduleNames) {
        if (moduleNames == null) {
            return false;
        }
        for (String name : moduleNames) {
            if (name == null || !mModules.containsKey(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 注册模块空间
     * @param moduleNames
     */
    public static void register(String... moduleNames) {
        if (moduleNames != null) {
            for (String name : moduleNames) {
                if (((Module) mModules.get(name)) == null) {
                    mModules.put(name, new Module(name));
                }
            }
        }
    }

    /**
     * 取消注册模块空间
     * @param moduleNames
     */
    public static void unregister(String... moduleNames) {
        for (String name : moduleNames) {
            if (name != null) {
                mModules.remove(name);
            }
        }
    }

    public static Module getModule(String moduleName) {
        return (Module) mModules.get(moduleName);
    }

    public static Module getModule(boolean createModelIfNotExist, String moduleName) {
        Module module = null;
        if (null != moduleName) {
            module = mModules.get(moduleName);
            if (null == module && createModelIfNotExist) {
                module = new Module(moduleName);
                mModules.put(moduleName, module);
            }
        }
        return module;
    }

    public static Collection<Module> getModels() {
        return mModules.values();
    }

    public static void clearModel() {
        mModules.clear();
    }

    private static Class<?>[] getArgmsClassTypes(int offset, Object... argms) {
        List<Class> ret = new ArrayList();
        if (argms != null) {
            for (int i = offset; i < argms.length; i++) {
                Object obj = argms[i];
                if (obj != null) {
                    ret.add(obj.getClass());
                }
            }
        }
        return (Class[]) ret.toArray(new Class[0]);
    }

    private static List<Class<?>> enumInterfaces(Class clas) {
        List<Class<?>> faces = new ArrayList();
        if (clas != null) {
            do {
                for (Class<?> itf : clas.getInterfaces()) {
                    if (itf != null) {
                        faces.add(itf);
                    }
                }
                clas = clas.getSuperclass();
            } while (!clas.equals(Object.class));
        }
        return faces;
    }

    private static Set<Method> getMethods(boolean declared, Object obj, String methodname, Class... classes) {
        Set<Method> map = new HashSet();
        if (obj != null && (methodname != null || classes.length > 0)) {
            for (Method method : declared ? obj.getClass().getDeclaredMethods() : obj.getClass().getMethods()) {
                if (method != null) {
                    boolean equals = false;
                    if (methodname == null) {
                        equals = true;
                    } else if (method.getName().equals(methodname)) {
                        equals = true;
                    }
                    if (equals && classes.length > 0) {
                        Class<?>[] types = method.getParameterTypes();
                        if (types.length == classes.length) {
                            for (int i = 0; i < types.length; i++) {
                                if (!types[i].isAssignableFrom(classes[i])) {
                                    equals = false;
                                    break;
                                }
                            }
                        } else {
                            equals = false;
                        }
                    }
                    if (equals) {
                        if (declared) {
                            method.setAccessible(true);
                        }
                        map.add(method);
                    }
                }
            }
        }
        return map;
    }

    /**
     * 执行函数
     * execMethod(false, new String(), "valueOf(1)")
     * @param declared 是否为私有函数
     * @param key 对象
     * @param methodname 函数名字
     * @param classes 参数类名
     * @param args 入口参数
     * @return
     */
    public static Object execMethod(boolean declared, Object key, String methodname, Class[] classes, Object... args) {
        Object obj = null;
        if (null != key && null != methodname) {
            Class keyClass = null;
            Object keyObj = null; //当keyObj为null表示静态函数
            if (obj instanceof Class) { //is class
                keyClass = (Class) obj;
            } else if (obj instanceof String) {//is string
                try {
                    keyClass = Class.forName((String)obj);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (obj != null) { //is object
                keyClass = obj.getClass();
                keyObj = obj;
            }

            if (null != keyClass) {
                try {
                    Method method;
                    if (null != args && args.length > 0) {
                        if (declared) {
                            method = keyClass.getDeclaredMethod(methodname, classes != null ? classes : getArgmsClassTypes(0, args));
                            method.setAccessible(true);
                        } else {
                            method = keyClass.getMethod(methodname, classes != null ? classes : getArgmsClassTypes(0, args));
                        }
                        obj = method.invoke(keyObj, args);
                    } else {
                        if (declared) {
                            method = keyClass.getDeclaredMethod(methodname);
                            method.setAccessible(true);
                        } else {
                            method = keyClass.getMethod(methodname);
                        }
                        obj = method.invoke(keyObj);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e2) {
                    e2.printStackTrace();
                } catch (IllegalAccessException e3) {
                    e3.printStackTrace();
                }
            }
        }
        return obj;
    }

    /**
     * 在绑定的所有对象中执行函数
     * @param modelname 模块名
     * @param methodname 对象名字
     * @param args 参数
     * @return
     */
    public static Object[] posts(String modelname, boolean subclass, Object key, String methodname, Object... args) {
        Module module = getModule(modelname);
        if (module != null) {
            return module.posts(subclass, key, methodname, args);
        }
        return null;
    }

    public static Object[] postAll(String modelname, String methodname, Object... args) {
        return posts(modelname, false, null, modelname, args);
    }

    public static Object[] postDeclareds(String modelname, boolean subclass, Object key, String methodname, Object... args) {
        Module module = getModule(modelname);
        if (module != null) {
            module.postDeclareds(subclass, key, methodname, args);
        }
        return null;
    }

    public static Object[] postAllDeclared(String modelname, String methodname, Object... args) {
        return postDeclareds(modelname, false, null, modelname, args);
    }

    /**
     * 判断模块空间是否注册对象
     * @param modelname
     * @param instname
     * @return
     */
    public static boolean isBinded(String modelname, String instname) {
        Module module = getModule(modelname);
        if (module != null) {
            return module.isBinded(instname);
        }
        return false;
    }

    /**
     * 将对象绑定到模块空间中
     * @param modelname
     * @param key
     * @param cover
     * @param obj
     * @param param
     * @return
     */
    public static boolean bindByObjWithParam(String modelname, String key, Object obj, Object param, boolean cover) {
        Module module = getModule(true, modelname);
        if (module != null) {
            return module.bind(key, cover, false, param, obj);
        }
        return false;
    }

    public static boolean bindByObjWithParam(String modelname, Object param, boolean cover, Object... strClassObjs) {
        Module module = getModule(true, modelname);
        if (module != null) {
            return module.bind(null, cover, false, param, strClassObjs);
        }
        return false;
    }

    public static boolean bindByObj(String modelname, String key, Object obj, boolean cover) {
        return bindByObjWithParam(modelname, key, obj, null, cover);
    }

    public static boolean bindByObj(String modelname, boolean cover, Object... strClassObjs) {
        return bindByObjWithParam(modelname, null, cover, strClassObjs);
    }

    public static boolean bindByClassWithParam(String modelname, String key, Object obj, Object param, boolean cover) {
        Module module = getModule(true, modelname);
        if (module != null) {
            return module.bind(key, cover, true, param, obj);
        }
        return false;
    }

    public static boolean bindByClassWithParam(String modelname, Object param, boolean cover, Object... canStrClassObjs) {
        Module module = getModule(true, modelname);
        if (module != null) {
            return module.bind(null, cover, true, param, canStrClassObjs);
        }
        return false;
    }

    public static boolean bindByClass(String modelname, String key, Object obj, boolean cover) {
        return bindByClassWithParam(modelname, key, obj, null, cover);
    }

    public static boolean bindByClass(String modelname, boolean cover, Object... canStrClassObjs) {
        return bindByClassWithParam(modelname, null, cover, canStrClassObjs);
    }

    private static void unbindObjs(String modelname, Object param, boolean byclass, Object... keys) {
        if (null != keys && keys.length > 0) {
            Module module = getModule(modelname);
            if (module != null) {
                Object[] objs = new Object[keys.length];
                for (int i = 0; i < objs.length; i++) {
                    Object obj = keys[i];
                    if (!(obj instanceof String || obj instanceof Class)) {
                        if (byclass) {
                            objs[i] = obj.getClass();
                        }
                    } else {
                        objs[i] = obj;
                    }
                }
                module.unbind(param, objs);
            }
        }
    }

    public static void unbindByObjWithParam(String modelname, Object param, Object... keys) {
        unbindObjs(modelname, param, false, keys);
    }

    public static void unbindByObj(String modelname, Object... keys) {
        unbindObjs(modelname, null, false, keys);
    }

    public static void unbindByClassWithParam(String modelname,Object param, Object... keys) {
        unbindObjs(modelname, param, true, keys);
    }

    public static void unbindByClass(String modelname, Object... keys) {
        unbindObjs(modelname, null, true, keys);
    }

    public static Object getBind(String modelname, boolean byclass, Object key, int index) {
        Module module = getModule(modelname);
        if (module != null) {
            Instance bean = module.getInstance(key, byclass, index);
            if (bean != null) {
                return bean.getObj();
            }
        }
        return java.lang.Boolean.valueOf(false);
    }

    public static Object getFirstBind(String modelname, Object key, boolean byclass) {
        return getBind(modelname, byclass, key, 0);
    }

    public static List<Object> getBinds(String modelname) {
        List<Object> objs = new ArrayList();
        Module module = getModule(modelname);
        if (module != null) {
            List<Instance> instances = module.getInstances();
            if (instances != null) {
                for (Instance instance : instances) {
                    if (!(instance == null || instance.getObj() == null)) {
                        objs.add(instance.getObj());
                    }
                }
            }
        }
        return objs;
    }

    /**
     * 添加监听
     * @param createModelIfNotExist
     * @param subClass
     * @param modelname
     * @param param 附加的参数, 将会传递给所有监听, 可以标识绑定的源
     * @param listener
     * @param canStrClassObjs
     * @return
     */
    public static boolean addBindListener(boolean createModelIfNotExist, boolean subClass, String modelname, Object param, BindListener listener, Object ...canStrClassObjs) {
        Module module = getModule(createModelIfNotExist, modelname);
        if (module != null) {
            return module.addBindListener(listener, param, subClass, canStrClassObjs);
        }
        return false;
    }

    public static boolean addBindListener(boolean createModelIfNotExist, boolean subClass, String modelname, BindListener listener, Object ...canStrClassObjs) {
        return addBindListener(createModelIfNotExist, subClass, modelname, null, listener, canStrClassObjs);
    }


    /**
     * 移除监听
     * @param modelname
     * @param listener
     * @param param 附加的参数, 将会传递给所有监听, 可以标识绑定的源
     * @return
     */
    public static boolean removeBindListener(String modelname, BindListener listener, Object param) {
        Module module = getModule(modelname);
        if (module != null) {
            return module.removeBindListener(listener, param);
        }
        return false;
    }

    public static boolean removeBindListener(String modelname, BindListener listener) {
        return removeBindListener(modelname, listener, null);
    }

    /**
     * 对象注入: 当为String的时候从owner中查找入口并绑定, 当为Object时,从obj中查找入口并绑定,
     * 所有与View相关的操作都会在Hanle(用户传入或者默认创建)中Post
     * @param owner 对象的拥有者
     * @param object 对象
     * @param args 参数类型,
     *             当第一次参数为Handle时
     *                  所有与View相关的操作将在此handler中操作,如果没有传入此对象(不能在没有Looper的Thread中使用),则会new一个默认的Handler对象
     *             当第一个参数为String类型时:
     *                  表示为绑定的函数名集合"methodname1,methodname2, methodname3",而args对应的为参数实例
     *             当第一个参数非String类型(即为Object)时:
     *                  会根据Object获取类型, 然后匹配所有"add"或"set"开头的且派生于此类的函数,并调用(注:此种方式可能会出错,即会设置其他未知的函数)
     * @param <T> 返回的类型，injecto 末尾的"o"表示Object
     * @return
     */
    public static <T> T injecto(final Object owner, final T object, Object... args) {
        if (null != object && null != args && 0 < args.length) {
            final Map<String, Object> methodparams = new HashMap<>();
            //判断是否为私有变量
            final Object[] arguments;
            final Handler handler;
            if (args[0] instanceof Handler) {
                handler = (Handler)args[0];
                args = Arrays.copyOfRange(args, 1, args.length);
            } else {
                handler = new Handler();
            }

            if (args[0] instanceof String){
                String[] types = null;
                String type = (String)args[0];
                if (type.matches("^( *\\w+ *)(, *\\w+ *)*$")) {
                    type = type.replaceAll(" ", "");
                    types = type.split(",");
                }

                arguments = Arrays.copyOfRange(args, 1, args.length);
                if (null != types) {
                    for (int i = 0; i < types.length && i < arguments.length; i++) {
                        if (null != types[i] && null != arguments[i]) {
                            methodparams.put(types[i], arguments[i]);
                        }
                    }
                }
            } else {
                arguments = args;
            }

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (methodparams.size() > 0) {
                        Method[] methods = object.getClass().getMethods();
                        for (Method mth : methods) {
                            if (null != mth) {
                                final Method method = mth;
                                String name = method.getName();
                                Class<?>[] params = method.getParameterTypes();
                                if (null != params && 1 == params.length && methodparams.containsKey(name)) {
                                    final Object obj = methodparams.get(name);
                                    if (null != obj && params[0].isAssignableFrom(obj.getClass())) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    method.invoke(object, obj);
                                                } catch (IllegalAccessException e) {
                                                } catch (InvocationTargetException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    } else {
                        Method[] methods = object.getClass().getMethods();
                        for (Method mth : methods) {
                            final Method method = mth;
                            if (null != method && method.getName().matches("^(set|add)\\w+$")) {
                                Class<?>[] types = method.getParameterTypes();
                                //参数的个数为1, 参数不为基本类型, 不为java的基本类型, 不为java.xxx下面的类
                                if (1 == types.length && !types[0].isPrimitive() && !types[0].getName().matches("^java(\\.\\w+)+$")) {
                                    for (Object arg : arguments) {
                                        final Object obj = (arg instanceof String) ? owner : arg;
                                        if (null != obj && types[0].isAssignableFrom(obj.getClass())) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        method.invoke(object, obj);
                                                    } catch (IllegalAccessException e) {
                                                        e.printStackTrace();
                                                    } catch (InvocationTargetException e) {
                                                        System.out.print(e.getTargetException());
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
            thread.start();
        }
        return object;
    }

    /**
     * View注入: 当为String的时候从owner中查找入口并绑定, 当为Object时,从obj中查找入口并绑定
     * @param owner view的拥有者
     * @param id view的ID
     * @param args 参数类型,
     *             当为String的时候从owner中查找入口并绑定
     *             当为Object时,从obj中查找入口并绑定
     *             当第一个参数为Boolean类型时,表示指示此绑定的函数为私有函数
     * @param <T> 返回的类型, injectv 末尾的"v"表示View
     * @return
     */
    public static <T extends View> T injectv(Object owner, Integer id, Object... args) {
        T view = null;
        if (null != owner && null != id) {
            Method findViewById;
            try {
                findViewById = owner.getClass().getMethod("findViewById", int.class);
                view = (T)findViewById.invoke(owner, id);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                System.out.print(e.getTargetException());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (null != view) {
                injecto(owner, view, args);
            }
        }
        return view;
    }
}