import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * 自定义一个 Classloader，加载一个 Hello.xlass 文件，执行 hello 方法，此文件内容是一个 Hello.class 文件所有字节（x=255-x）处理后的文件。
 * @author zhouwb
 * create 2020-10-21 21:20
 */
public class CustomClassLoader extends ClassLoader{
    public static void main(String[] args) {
        CustomClassLoader customClassLoader = new CustomClassLoader();
        Class<?> helloClazz = customClassLoader.findClass("Hello");
        try {
            //执行 hello 方法
            Object HelloClazzInstance = helloClazz.newInstance();
            Method helloMethod = helloClazz.getMethod("hello");
            helloMethod.invoke(HelloClazzInstance);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name){
        //加载一个 Hello.xlass 文件
        URL helloXlassURL = CustomClassLoader.class.getResource("Hello.xlass");
        byte[] bytes = new byte[0];

        try {
            bytes = Files.readAllBytes(Paths.get(helloXlassURL.toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        //所有字节（x=255-x）处理
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (255 - bytes[i]);
        }

        return  defineClass(name, bytes, 0, bytes.length);
    }
}
