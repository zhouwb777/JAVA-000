package com.springbean.test;

import com.springbean.test.config.StudentConfig;
import com.springbean.test.dto.Student;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author zhouwb
 * create 2020-11-17 22:14
 */
public class AnnotationConfigTest {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(StudentConfig.class);
        Student student = ctx.getBean(Student.class);
        student.setName("test");
        student.setAge(25);
        System.out.println(student);
    }
}
