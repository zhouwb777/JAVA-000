package com.springbean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class XmlBeanTest {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        System.out.println(context.getBean("student1"));
        System.out.println(context.getBean("student2"));

        // Output
        // User(name=Tom, password=123)
        // User(name=Lucy, password=456)
    }
}
