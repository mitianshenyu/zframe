package com.zeu.frame.log;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zerain on 2016/11/1.
 */
public class Slog {
    String tag = null;
    Level level = null;
    List<Type> types = new ArrayList<>();
    public Slog setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public <T extends Type> Slog addType(T type) {
        if (null != type && !types.contains(type)) {
            this.types.add(type);
        }
        return this;
    }

    public <T extends Type> Slog removeType(T type) {
        this.types.remove(type);
        return this;
    }

    public Slog setLevel(Level level) {
        this.level = level;
        return this;
    }

    public void _out(String ...msgs) {
        out(this, null, null, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public void _out(String tag, int traceDeepth, String...msgs) {
        out(this, null, null, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public void _out(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, null, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _out(String tag, Level level, int calledDeepth, String...msgs) {
        out(this, null, level, tag, getTraceElements(calledDeepth, sTraceDeepth), msgs);
    }

    public void _out(String tag, Level level, int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, level, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _out(String tag, Type type, Level level, int calledDeepth, String...msgs) {
        out(this, type, level, tag, getTraceElements(calledDeepth, sTraceDeepth), msgs);
    }

    public void _out(String tag, Type type, Level level, int calledDeepth, int traceDeepth, String...msgs) {
        out(this, type, level, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _v(String...msgs) {
        out(this, null, Level.VERBOSE, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public void _v(int traceDeepth, String...msgs) {
        out(this, null, Level.VERBOSE, null, getTraceElements(0, traceDeepth), msgs);
    }

    public void _v(int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.VERBOSE, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _v(String tag, int traceDeepth, String...msgs) {
        out(this, null, Level.VERBOSE, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public void _v(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.VERBOSE, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _d(String...msgs) {
        out(this, null, Level.DEBUG, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public void _d(int traceDeepth, String...msgs) {
        out(this, null, Level.DEBUG, null, getTraceElements(0, traceDeepth), msgs);
    }

    public void _d(int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.DEBUG, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _d(String tag, int traceDeepth, String...msgs) {
        out(this, null, Level.DEBUG, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public void _d(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.DEBUG, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _i(String...msgs) {
        out(this, null, Level.INFO, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public void _i(int traceDeepth, String...msgs) {
        out(this, null, Level.INFO, null, getTraceElements(0, traceDeepth), msgs);
    }

    public void _i(int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.INFO, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _i(String tag, int traceDeepth, String...msgs) {
        out(this, null, Level.INFO, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public void _i(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.INFO, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _w(String...msgs) {
        out(this, null, Level.WARN, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public void _w(int traceDeepth, String...msgs) {
        out(this, null, Level.WARN, null, getTraceElements(0, traceDeepth), msgs);
    }

    public void _w(int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.WARN, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _w(String tag, int traceDeepth, String...msgs) {
        out(this, null, Level.WARN, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public void _w(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.WARN, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _e(String...msgs) {
        out(this, null, Level.ERROR, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public void _e(int traceDeepth, String...msgs) {
        out(this, null, Level.ERROR, null, getTraceElements(0, traceDeepth), msgs);
    }

    public void _e(int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.ERROR, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _e(String tag, int traceDeepth, String...msgs) {
        out(this, null, Level.ERROR, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public void _e(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.ERROR, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _wtf(String...msgs) {
        out(this, null, Level.WTF, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public void _wtf(int traceDeepth, String...msgs) {
        out(this, null, Level.WTF, null, getTraceElements(0, traceDeepth), msgs);
    }

    public void _wtf(int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.WTF, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public void _wtf(String tag, int traceDeepth, String...msgs) {
        out(this, null, Level.WTF, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public void _wtf(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(this, null, Level.WTF, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    static int sTraceDeepth = 1;
    static Type sDefaultType = null;
    static Map<String, Slog> sSlogs = new LinkedHashMap<>();
    public static void setDefaultType(Type type) {
        sDefaultType = type;
    }
    public static int setTraceDeepth(int traceDeepth) {
        int deepth = sTraceDeepth;
        sTraceDeepth = traceDeepth;
        return deepth;
    }

    public static int getTraceDeepth() {
        return sTraceDeepth;
    }

    public static Slog get(int classCalledDeepth) {
        Slog log = null;
        StackTraceElement stackTraceElement = getTraceElement(classCalledDeepth);
        if (null != stackTraceElement) {
            String className = stackTraceElement.getClassName();
            if (null != className) {
                synchronized (sSlogs) {
                    log = sSlogs.get(className);
                    if (null == log) {
                        log = new Slog();
                        sSlogs.put(className, log);
                    }
                }
            }
        }
        if (null == log) {
            log = new Slog();
            log.level = Level.VERBOSE;
        }
        return log;
    }

    /**
     * 根据log的名字获取,log
     * @param name log的名字
     * @param createIfNotExit log是否已经存在
     * @return
     */
    public static Slog get(String name, boolean createIfNotExit) {
        Slog log = null;
        if (null != name) {
            synchronized (sSlogs) {
                log = sSlogs.get(name);
            }

            if (createIfNotExit && null == log) {
                log = new Slog();
                log.level = Level.VERBOSE;
                sSlogs.put(name, log);
            }
        }
        return log;
    }

    public static Slog get(String name) {
        return get(name, false);
    }

    public static Slog put(String name, Slog slog) {
        if (null != name && null != slog) {
            sSlogs.put(name, slog);
        }
        return slog;
    }

    /**
     * 获取默认的
     * @return
     */
    public static Slog get() {
        return get("", true);
    }

    /**
     * 打印当前类的信息
     * @param msgs
     */
    public static void out(String...msgs) {
        out(get(), null, null, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public static void out(String tag, int traceDeepth, String...msgs) {
        out(get(), null, null, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public static void out(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(get(), null, null, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void out(String tag, Level level, int calledDeepth, String...msgs) {
        out(get(calledDeepth), null, level, tag, getTraceElements(calledDeepth, sTraceDeepth), msgs);
    }

    public static void out(String tag, Level level, int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, level, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void out(String tag, Type type, Level level, int calledDeepth, String...msgs) {
        out(get(calledDeepth), type, level, tag, getTraceElements(calledDeepth, sTraceDeepth), msgs);
    }

    public static void out(String tag, Type type, Level level, int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), type, level, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void v(String...msgs) {
        out(get(), null, Level.VERBOSE, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public static void v(int traceDeepth, String...msgs) {
        out(get(0), null, Level.VERBOSE, null, getTraceElements(0, traceDeepth), msgs);
    }

    public static void v(int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.VERBOSE, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void v(String tag, int traceDeepth, String...msgs) {
        out(get(0), null, Level.VERBOSE, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public static void v(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.VERBOSE, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void d(String...msgs) {
        out(get(), null, Level.DEBUG, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public static void d(int traceDeepth, String...msgs) {
        out(get(0), null, Level.DEBUG, null, getTraceElements(0, traceDeepth), msgs);
    }

    public static void d(int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.DEBUG, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void d(String tag, int traceDeepth, String...msgs) {
        out(get(0), null, Level.DEBUG, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public static void d(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.DEBUG, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void i(String...msgs) {
        out(get(), null, Level.INFO, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public static void i(int traceDeepth, String...msgs) {
        out(get(0), null, Level.INFO, null, getTraceElements(0, traceDeepth), msgs);
    }

    public static void i(int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.INFO, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void i(String tag, int traceDeepth, String...msgs) {
        out(get(0), null, Level.INFO, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public static void i(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.INFO, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void w(String...msgs) {
        out(get(), null, Level.WARN, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public static void w(int traceDeepth, String...msgs) {
        out(get(0), null, Level.WARN, null, getTraceElements(0, traceDeepth), msgs);
    }

    public static void w(int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.WARN, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void w(String tag, int traceDeepth, String...msgs) {
        out(get(0), null, Level.WARN, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public static void w(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.WARN, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void e(String...msgs) {
        out(get(), null, Level.ERROR, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public static void e(int traceDeepth, String...msgs) {
        out(get(0), null, Level.ERROR, null, getTraceElements(0, traceDeepth), msgs);
    }

    public static void e(int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.ERROR, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void e(String tag, int traceDeepth, String...msgs) {
        out(get(0), null, Level.ERROR, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public static void e(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.ERROR, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void wtf(String...msgs) {
        out(get(), null, Level.WTF, null, getTraceElements(0, sTraceDeepth), msgs);
    }

    public static void wtf(int traceDeepth, String...msgs) {
        out(get(0), null, Level.WTF, null, getTraceElements(0, traceDeepth), msgs);
    }

    public static void wtf(int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.WTF, null, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    public static void wtf(String tag, int traceDeepth, String...msgs) {
        out(get(0), null, Level.WTF, tag, getTraceElements(0, traceDeepth), msgs);
    }

    public static void wtf(String tag, int calledDeepth, int traceDeepth, String...msgs) {
        out(get(calledDeepth), null, Level.WTF, tag, getTraceElements(calledDeepth, traceDeepth), msgs);
    }

    /**
     * 获取函数执行跟踪信息
     * @param throwable
     * @param calledDeepth 函数调用的深度, 被其他函数调用一次, 调用深度加一, 默认为0层,即在SLog类外调用深度, 如
     *      深度为0: 直接在需要打印日志的地方调用: Slog.d(...)
     *      胜读为1: void log(String...msgs) {Slog.d("zlib", 1, msgs);}， 被封装了一层
     * @return
     */
    public static StackTraceElement getTraceElement(Throwable throwable, int calledDeepth) {
        StackTraceElement traceElement = null;
        if (calledDeepth == -1) {
            //默认为0层,即在SLog.class外的第一次调用
            calledDeepth = 0;
        }
        if (calledDeepth >= 0) {
            int index = 0;
            if (null == throwable) {
                throwable = new Throwable();
                StackTraceElement[] traceElements = throwable.getStackTrace();
                if (null != traceElements) {
                    for (StackTraceElement element : traceElements) {
                        //现获取当前的类名, 然后在当前的类名中往上开始计算调用层数
                        if (null != element && !Slog.class.getName().equals(element.getClassName())) {
                            calledDeepth +=index;
                            if (calledDeepth < traceElements.length) {
                                traceElement = traceElements[calledDeepth];
                                break;
                            }
                        }
                        index++;
                    }
                }
            } else {
                StackTraceElement[] traceElements = throwable.getStackTrace();
                if (null != traceElements) {
                    if (calledDeepth < traceElements.length) {
                        traceElement = traceElements[calledDeepth];
                    }
                }
            }
        }
        return traceElement;
    }

    /**
     * 获取函数执行所有跟踪信息
     * @param throwable
     * @param calledDeepth 函数调用的深度, 被其他函数调用一次, 调用深度加一, 默认为0层,即在SLog类外调用深度, 如
     *      深度为0: 直接在需要打印日志的地方调用: Slog.d(...)
     *      胜读为1: void log(String...msgs) {Slog.d("zlib", 1, msgs);}， 被封装了一层
     * @param traceDeepth 跟踪深度,
     *                    traceDeepth < 0 的时候打印sTraceDeepth中的,如果sTraceDeepth<=0打印全部跟踪深度
     *                    traceDeepth == 0 表示默认的1层
     * @return
     */
    public static StackTraceElement[] getTraceElements(Throwable throwable, int calledDeepth, int traceDeepth) {
        List<StackTraceElement> traceElements = new ArrayList<>();
        if (calledDeepth < 0) {//默认为0层,即在SLog.class外的第一次调用
            calledDeepth = 0;
        }

        if (0 == traceDeepth) {
            traceDeepth = 1;
        }

        if (null == throwable) {
            throwable = new Throwable();
            StackTraceElement[] elements = throwable.getStackTrace();
            if (null != elements) {
                for (int i = 0; i < elements.length; i++) {
                    StackTraceElement element = elements[i];
                    //现获取当前的类名, 然后在当前的类名中往上开始计算调用层数
                    if (null != element && !Slog.class.getName().equals(element.getClassName())) {
                        i += calledDeepth;
                        for (int j = 0; i < elements.length && (traceDeepth < 0 || j < traceDeepth); j++, i++) {
                            element = elements[i];
                            if (null != element) {
                                traceElements.add(element);
                            }
                        }
                        break;
                    }
                }
            }
        } else {
            StackTraceElement[] elements = throwable.getStackTrace();
            for (int i = calledDeepth; i < elements.length; i++) {
                StackTraceElement element = elements[i];
                if (null != element) {
                    traceElements.add(element);
                }
            }
        }
        return traceElements.toArray(new StackTraceElement[]{});
    }

    /**
     * 获取函数执行跟踪信息
     * @param calledDeepth 函数调用的深度, 默认为0层,即在SLog类外调用深度, 如
     *      深度为0: 直接在需要打印日志的地方调用: Slog.d(...)
     *      胜读为1: void log(String...msgs) {Slog.d("zlib", 1, msgs);}， 被封装了一层
     * @return
     */
    public static StackTraceElement getTraceElement(int calledDeepth) {
        return getTraceElement(null, calledDeepth);
    }

    public static StackTraceElement[] getTraceElements(int calledDeepth, int traceDeepth) {
        return getTraceElements(null, calledDeepth, traceDeepth);
    }

    /**
     * @param msgs
     */
    private static void out(Slog log, Type type, Level level, CharSequence tag, StackTraceElement[] stackTraceElements, String...msgs) {
        if (null != log) {
            if (null == tag) tag = log.tag;
            if (null == level) level = log.level;
        }

        //如果返回为true, 就不执行默认的Logcat打印
        if (null == sDefaultType || !sDefaultType.out(level, tag, stackTraceElements, msgs)) {
            if (null == log || log.types.isEmpty()) {
                if (null == type) {
                    type = new LogcatType();
                }

                type.out(level, tag, stackTraceElements, msgs);
            } else {
                if (null != type) {
                    type.out(level, tag, stackTraceElements, msgs);
                }

                for (Type tp : log.types) {
                    if (null != tp) {
                        tp.out(level, tag, stackTraceElements, msgs);
                    }
                }
            }
        }
    }

    /**
     * @param type printf type: file, email, port...
     * @param level printf level : error ,warning, debug, wtf....
     * @param tag
     *      tag==null : functionName(filename.java:linenumber)
     *      tag!=null : tag[functionName(filename.java:linenumber)]
     * @param stackTraceElements 堆栈跟踪元素
     * @param msgs
     * @return
     */
    public static void out(Type type, Level level, CharSequence tag, StackTraceElement[] stackTraceElements, String...msgs) {
        out(null, type, level, tag, stackTraceElements, msgs);
    }

    public enum Level {
        VERBOSE("V"),
        DEBUG("D"),
        INFO("I"),
        WARN("W"),
        ERROR("E"),
        WTF("WTF");
        final String simple;
        Level(String simple) {this.simple = simple;}
        public String simple() {return this.simple;}
    }

    public abstract static class Type {
        public static final int LOGCAT = (1<<0);//can 是*txt，或者串口
        public static final int FILE = (1<<1);//can 是*txt，或者串口
        public static final int CONSOLE = (1<<2);
        public static final int MAIL = (1<<3);
        public static final int VIEW = (1<<4);
        public static final int NET = (1<<5);
        public static final int OTHER = (1<<6);
        public static final int ALL = ((1<<7)-1);

        Object param;
        int mRraceDeepth = 0;
        protected Set<String> mFileFilter = new HashSet<>();

        /**
         * 必须附加上文件的扩展名, 或者tag, 如:"test.java", "TAG_TEST"
         * @param fileFilter
         * @return
         */
        public <T extends Type> T addFilter(String ...fileFilter) {
            if (null != fileFilter && fileFilter.length > 0) {
                for (String file : fileFilter) {
                    if (null != file) {
                        mFileFilter.add(file);
                    }
                }
            }
            return (T)this;
        }

        /**
         * 可以过滤,log存在的java文件名字, 或者存在的tag
         * @param elementFileName
         * @return
         */
        public boolean isInFilter(String elementFileName) {
            return (mFileFilter.isEmpty() || mFileFilter.contains(elementFileName));
        }

        public int getTraceDeepth() {
            return mRraceDeepth;
        }

        public void setTraceDeepth(int traceDeepth) {
            this.mRraceDeepth = traceDeepth;
        }

        public final Object getParam() {
            return param;
        }

        public final Type setParam(Object param) {
            this.param = param;
            return this;
        }

        /**
         * 默认输出到logcat上面
         * @param level
         * @param tag
         * @param stackTraceElements
         * @param msgs
         * @return
         */
        public abstract boolean out(Level level, CharSequence tag, StackTraceElement[] stackTraceElements, String... msgs);
    }

    public static class LogcatType extends Type {
        /**
         * 默认输出到logcat上面
         * @param level
         * @param tag
         * @param stackTraceElements
         * @param msgs
         * @return
         */
        public boolean out(Level level, CharSequence tag, StackTraceElement[] stackTraceElements, String... msgs) {
            if (null != msgs && msgs.length > 0 && null != stackTraceElements && stackTraceElements.length > 0) {
                String msgstr = "";
                for (String msg : msgs) {
                    if (!msgstr.equals("")) {
                        msgstr += " ";
                    }
                    msgstr += msg;
                }

                //获取tag字符串
                boolean isinfileter = false;
                String tagstr = null;
                StackTraceElement element = stackTraceElements[0];
                if (null != element) {
                    String name = element.getFileName();
                    tagstr = ("(" + name + ":" + element.getLineNumber() + ")" + element.getMethodName());
                    isinfileter = isInFilter(name);
                }

                if (null != tag) {
                    if (null != tagstr) {
                        tagstr = (tag.toString() + ("[" + tagstr + "]"));
                    } else {
                        tagstr = tag.toString();
                    }

                    if (!isinfileter) {
                        isinfileter = isInFilter(tag.toString());
                    }
                }

                if (null != tagstr && !tagstr.isEmpty() &&  isinfileter) {
                    int deepth = stackTraceElements.length - 1;
                    //打印跟踪堆栈
                    for (; deepth > 0; deepth--) {
                        String info;
                        element = stackTraceElements[deepth];
                        if (null != element) {
                            info = ("(" + element.getFileName() + ":" + element.getLineNumber() + ")" + element.getMethodName());
                            switch (level) {
                                case VERBOSE:
                                    Log.v(tagstr, "<" + deepth + ": " + info + ">");
                                    break;
                                case INFO:
                                    Log.i(tagstr, "<" + deepth + ": " + info + ">");
                                    break;
                                case WARN:
                                    Log.w(tagstr, "<" + deepth + ": " + info + ">");
                                    break;
                                case ERROR:
                                    Log.e(tagstr, "<" + deepth + ": " + info + ">");
                                    break;
                                case WTF:
                                    Log.wtf(tagstr, "<" + deepth + ": " + info + ">");
                                    break;
                                case DEBUG:
                                default:
                                    Log.d(tagstr, "<" + deepth + ": " + info + ">");
                                    break;
                            }
                        }
                    }

                    //打印对应行的log日志
                    switch (level) {
                        case VERBOSE:
                            Log.v(tagstr, msgstr);
                            break;
                        case INFO:
                            Log.i(tagstr, msgstr);
                            break;
                        case WARN:
                            Log.w(tagstr, msgstr);
                            break;
                        case ERROR:
                            Log.e(tagstr, msgstr);
                            break;
                        case WTF:
                            Log.wtf(tagstr, msgstr);
                            break;
                        case DEBUG:
                        default:
                            Log.d(tagstr, msgstr);
                            break;
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public static class FileType extends Type{
        String filePath;
        String fileName;
        long maxSize ;
        String timeFormat;
        Context context;
        File mFile;
        boolean mIsAppend;
        boolean mCover;
        boolean mHasCovered = false;

        private FileType(Context context, String filePath, String fileName, String timeFormat, long maxSize, boolean cover, boolean isAppend) {
            this.mFile = null;
            this.timeFormat = timeFormat;
            this.filePath = filePath;
            this.fileName = fileName;
            if (maxSize > 0) {
                this.maxSize = maxSize;
            } else {
                this.maxSize = 2*1024*1024; //2M
            }
            this.context = context;
            mIsAppend = isAppend;
            mCover = cover;
        }

        public FileType(String filePath, String fileName, String[] fileFilter) {
            this(null, filePath, fileName, null, -1, false, true);
            addFilter(fileFilter);
        }

        public FileType(String filePath, String fileName) {
            this(null, filePath, fileName, null, -1, false, true);
        }

        public FileType(String fileName) {
            this(null, null, fileName, null, -1, false, true);
        }

        public FileType setMaxSize(long maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public FileType setTimeFormat(String timeFormat) {
            this.timeFormat = timeFormat;
            return this;
        }

        public FileType setContext(Context context) {
            this.context = context;
            return this;
        }

        public FileType setFile(File file) {
            this.mFile = file;
            return this;
        }

        public FileType setAppend(boolean isAppend) {
            this.mIsAppend = isAppend;
            return this;
        }

        public FileType setCover(boolean cover) {
            this.mCover = cover;
            return this;
        }

        @Override
        public boolean out(Level level, CharSequence tag, StackTraceElement[] stackTraceElements, String... msgs) {
            boolean ret = false;
            if (null != msgs && msgs.length > 0 && null != stackTraceElements && stackTraceElements.length > 0) {
                String msgstr = "";
                for (String msg : msgs) {
                    if (!msgstr.equals("")) {
                        msgstr += " ";
                    }
                    msgstr += msg;
                }

                //获取tag字符串
                StringBuffer tagstr = new StringBuffer();
                //添加打印的时间, 必须
                tagstr.append(new SimpleDateFormat("yy-MM-dd-hhmmss").format(new Date()));
                if (null != context) {
                    tagstr.append("/");
                    tagstr.append(context.getPackageName());
                }
                tagstr.append(" ");
                if (null != level) {
                    tagstr.append(level.simple);
                }
                tagstr.append("/");

                if (null != tag && !tag.toString().isEmpty()) {
                    tagstr.append(tag.toString());
                }

                String info;
                StackTraceElement element = stackTraceElements[0];
                if (null != element && (isInFilter(element.getFileName()) || (isInFilter(String.valueOf(tag))))) {
                    info = ("(" + element.getFileName() + ":" + element.getLineNumber() + ")" + element.getMethodName());
                    tagstr.append("[" + info + "]");

                    StringBuffer resutl = new StringBuffer();
                    int deepth = stackTraceElements.length - 1;
                    tagstr.append(": ");

                    //打印跟踪堆栈
                    for (; deepth > 0; deepth--) {
                        element = stackTraceElements[deepth];
                        if (null != element) {
                            resutl.append(tagstr);
                            info = ("(" + element.getFileName() + ":" + element.getLineNumber() + ")" + element.getMethodName());
                            resutl.append("<" + deepth + ": " + info + ">");
                            resutl.append("\n");
                        }
                    }

                    //打印日志信息
                    resutl.append(tagstr);
                    resutl.append(msgstr);
                    resutl.append("\n");

                    byte[] bytes = resutl.toString().getBytes();

                    //大小超过最大允许的文件大小, 则从新创建
                    InputStream inputStream = null;
                    try {
                        if (null == mFile) {
                            mFile = new File(makeFilePath(filePath, fileName, timeFormat));
                        }

                        //如果文件存在,则查看是否超过最大值, 是则另外新建文件
                        if (mFile.exists()) {
                            inputStream = new FileInputStream(mFile);

                            if (null != inputStream && (inputStream.available() + bytes.length) > maxSize) {
                                mFile = new File(makeFilePath(filePath, fileName, timeFormat));
                            }
                        }

                        //写入数据, 第一次写入数据的时候, 执行覆盖
                        ret = write(mFile, bytes, 0, bytes.length, true, mHasCovered ? false : mCover, mIsAppend);
                        mHasCovered = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (null != inputStream) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return ret;
        }

        public static String makeFilePath(String filePath, String fileName, String timeFormat) {
            String path = null;
            if (null == filePath) {
                filePath = Environment.getExternalStorageDirectory().getPath();
            }

            if (null != filePath && !filePath.isEmpty()) {
                if (!filePath.endsWith(File.separator)) {
                    filePath += File.separator;
                }
                String datetime;
                if (null != timeFormat) {
                    try {
                        SimpleDateFormat format = new SimpleDateFormat(timeFormat);
                        datetime = format.format(new Date());
                    } catch (Exception e) {
                        e.printStackTrace();
                        datetime = "";
                    }
                } else {
                    datetime = "";
                }
                if (null == fileName) {
                    fileName = datetime;
                } else {
                    int index = fileName.lastIndexOf(".");
                    if (index < 0) {
                        fileName = fileName + (timeFormat.isEmpty() ? "" :  datetime);
                    } else {
                        fileName = fileName.substring(0, index) + datetime + fileName.substring(index, fileName.length());
                    }
                }
            }

            if (!fileName.isEmpty()) {
                if (fileName.startsWith(File.separator)) {
                    fileName = fileName.substring(1, fileName.length());
                }

                path = filePath + fileName;
            }
            return path;
        }

        private static synchronized boolean write(File file, byte[] bytes, int off, int len, boolean createIfNotExist, boolean cover, boolean append) {
            boolean ret = false;
            if (null != file && null != bytes) {
                if (createIfNotExist) {
                    File prent = file.getParentFile();
                    if (!"/".equals(prent.getName()) && !prent.exists()) {
                        prent.mkdirs();
                    }
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        } else if (cover) {
                            file.delete();
                            file.createNewFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file, append);
                    fos.write(bytes, off, len);
                    ret = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return ret;
        }
    }

    public static void main(String[] args) {
        {//调用深度
            class TestNormal{
                public void log(String msg) {
                    Slog.d(1, msg); //被其他函数调用一次, 调用深度加一, 调用深度为1
                }
            }
        }

        {//打印到文件, 如果打印到文件, 必须先获取对应类的slog, 然后全局调用此slog去打印, 否则打印的只是对应类的slog
            Slog slog = Slog.get().setTag("_MainActivity").
                    setLevel(Level.ERROR).
                    addType(new FileType("maiActivit.txt").
                            setTimeFormat("_yyMMddhhmmss").
                            //setContext(MainActivity.this).
                                    setMaxSize(1 * 1024 * 1024).
                                    setAppend(true).
                                    setCover(false).addFilter("test.java", "TAG_TEST"));
            slog._d("");
        }

        {//打印当前类的所有跟踪堆栈信息
            /**
             打印结果,文件目录/sdcard/maiActivit_xxxxxx.txt
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <13: (ZygoteInit.java:777)main>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <12: (ZygoteInit.java:982)run>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <11: (Method.java:372)invoke>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <10: (Method.java:-2)invoke>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <9: (ActivityThread.java:5643)main>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <8: (Looper.java:194)loop>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <7: (Handler.java:111)dispatchMessage>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <6: (ActivityThread.java:1470)handleMessage>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <5: (ActivityThread.java:178)access$800>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <4: (ActivityThread.java:2614)handleLaunchActivity>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <3: (ActivityThread.java:2481)performLaunchActivity>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <2: (Instrumentation.java:1112)callActivityOnCreate>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: <1: (Activity.java:6100)performCreate>
             17-05-02-072759/com.pvt.bluetooth E/_MainActivity[(MainActivity.java:108)onCreate]: test
             */
            Slog.setTraceDeepth(-1);
            Slog.e("test");
            Slog.setTraceDeepth(0);
        }
    }
}
