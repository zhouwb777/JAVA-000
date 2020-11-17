package com.springbean.test;

import com.springbean.test.config.ScanPackageBeanConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import test.dto.Student;

/**
 * @author zhouwb
 * create 2020-11-17 22:14
 */
public class ScanPackageConfigTest {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(ScanPackageBeanConfig.class);
        Student student = applicationContext.getBean(Student.class);
        student.setName("test");
        student.setAge(25);
        System.out.println(student);
    }
}
