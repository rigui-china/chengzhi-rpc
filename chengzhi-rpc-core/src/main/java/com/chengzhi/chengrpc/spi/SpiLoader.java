package com.chengzhi.chengrpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.chengzhi.chengrpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 徐晟智
 * @version 1.0
 */

/**
 * SPI 加载器（支持键值对映射）
 */
@Slf4j
public class SpiLoader {
    /**
     * 存储已经加载的类：接口名 =》key =》实现类
     */
    private static Map<String, Map<String,Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存，类路径 =》对象实例，单例模式
     */
    private static Map<String,Object> instanceCache = new ConcurrentHashMap<>();

    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR,RPC_CUSTOM_SPI_DIR};

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll(){
        log.info("加载所有 SPI");
        for(Class<?> clazz : LOAD_CLASS_LIST){
            load(clazz);
        }
    }

    /**
     * 获取某个接口的实例
     * @param tClass
     * @param key
     * @return
     * @param <T>
     */
    public static <T> T getInstance(Class<T> tClass,String key){
        String tClassName = tClass.getName();
        Map<String,Class<?>> classMap = loaderMap.get(tClassName);
        if(classMap == null){
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", tClassName));
        }
        if(!classMap.containsKey(key)){
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key=%s 的类型", tClassName,key));
        }
        //获取到要加载的实现类型
        Class<?> implClass = classMap.get(key);
        //从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        if(!instanceCache.containsKey(implClassName)){
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMsg = String.format("%s 类实例化失败", implClassName);
                throw new RuntimeException(errorMsg,e);
            }
        }
        return (T)instanceCache.get(implClassName);
    }

    public static Map<String,Class<?>> load(Class<?> loadClass){
        log.info("加载类型为 {} 的 SPI", loadClass.getName());
        // 扫描路径，用户自定义的 SPI 优先级高于系统 SPI
        Map<String,Class<?>> keyClassMap = new HashMap<>();
        for(String scanDir : SCAN_DIRS){
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            //读取每个资源文件
            for(URL resource : resources){
                try{
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while((line = bufferedReader.readLine()) != null){
                        String[] strArray = line.split("=");
                        if(strArray.length > 1){
                            String key = strArray[0];
                            String className = strArray[1];
                            keyClassMap.put(key,Class.forName(className));
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    log.error("spi resource load error", e);
                }
            }
        }
        loaderMap.put(loadClass.getName(),keyClassMap);
        return keyClassMap;

    }
//    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        loadAll();
//        System.out.println(loaderMap);
//        Serializer serializer = getInstance(Serializer.class, "jdk");
//        System.out.println(serializer);
//    }
}
