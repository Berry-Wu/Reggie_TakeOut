package com.wzy.common;

/**基于TreadLocal封装工具类，用户保存和获取当前登录用户的id
 * @author wzy
 * @creat 2023-06-19-20:39
 */
public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
