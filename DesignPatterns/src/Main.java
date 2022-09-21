import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Enumeration;
import java.util.Hashtable;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // Factory Design Pattern
        Shape shape = MyFactory.getInstance("circle");
        shape.draw();
 
        // Singleton Design Pattern
        Shape s = MySingletonDesignPatter.getInstance();
        Shape s2 = MySingletonDesignPatter.getInstance();
        System.out.println(s.hashCode() == s2.hashCode());

//        L cb = new L();
//        L.ClassB vb =  cb.new ClassB();
//        vb.methodA();
//        L.InnerClass in = new L.InnerClass();
//        in.a = 300;
//        L.InnerClass in3 = new L.InnerClass();
//        in3.a = 400;
//        FileOutputStream fileOutputStream = new FileOutputStream("seraliz.ser");
//        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
//        objectOutputStream.writeObject(in);
//        fileOutputStream.close();
//
//        FileInputStream fileInputStream = new FileInputStream("seraliz.ser");
//        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//        L.InnerClass in2 = (L.InnerClass) objectInputStream.readObject();
//        System.out.println("in2 a value "+in2.a);

//        L.InnerClass in2 = new L.InnerClass();
//        in.a = 500;
//        System.out.println("printing the a value "+in2.a);

        // BUilder design pattern
        MyBuilder myBuilder = new MyBuilder.InnerClass("Rishi","kesh").phoneNumber("1234").build();
        System.out.println(myBuilder.toString());

        // ObjectPool Design pattern
        CustomPool customPool = new CustomPool();
        Thread t0 = new Thread(()->{
            Shape shape1 = (Shape) customPool.getInstance();
            shape1.draw();
            System.out.println(shape1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            customPool.TaskCompleted(shape1);
        });
        t0.start();
        Thread t1 = new Thread(()->{
            Shape shape1 = (Shape) customPool.getInstance();
            shape1.draw();
            System.out.println(shape1);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            customPool.TaskCompleted(shape1);
        });
        t1.start();
        Thread t2 = new Thread(()->{
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Shape shape1 = (Shape) customPool.getInstance();
            shape1.draw();
            System.out.println(shape1);
            customPool.TaskCompleted(shape1);
        });
        t2.start();

        while ((t1.isAlive() || t2.isAlive() || t0.isAlive())){}

        // DYnamic Proxy Design Pattern
        Shape shape4 = (Shape) MyProxy.getInstance(new Circle());
        shape4.draw();

    }
}

class L{
    static  L l = new L();
    static class InnerClass implements Serializable{
        static int a = 10;
        InnerClass(){
            System.out.println("Static Inner Class");
        }
        void methodA(){
        }
    }
    class ClassB extends InnerClass{

        @Override
        void methodA() {
            super.methodA();
        }
    }

}

// Factory Design Pattern
interface Shape{

    void draw();
}
class Circle implements Shape{

    public void draw() {
        System.out.println("Draw method in the Circle called...");
    }
}
class Square implements Shape{

    @Override
    public void draw() {
        System.out.println("Draw method in the Square called...");
    }
}

class MyFactory{
    static Shape getInstance(String string){
        if("circle".equals(string)) return new Circle();
        else if("square".equals(string)) return new Square();
        else return null;
    }
}

// Singleton Design Pattern
class MySingletonDesignPatter{

    private static Shape shape = null;

    private MySingletonDesignPatter(){}

    static Shape getInstance(){
        if(shape == null){
            shape= new Circle();
        }
        return shape;
    }
}

// BUilder Design Pattern

class MyBuilder{
    private final String  firstName;
    private final String  lastName;
    private final String  phoneNumber;
    private final String  address;
    MyBuilder(InnerClass innerClass){
        firstName = innerClass.firstName;
        lastName = innerClass.lastName;
        phoneNumber =  innerClass.phoneNumber;
        address = innerClass.address;
    }
    static class InnerClass{
        private final String firstName;
        private final String lastName;
        private String phoneNumber;
        private String address;

        InnerClass(String firstName,String lastName){
            this.firstName = firstName;
            this.lastName = lastName;
        }
        InnerClass phoneNumber(String phoneNumber){
            this.phoneNumber = phoneNumber;
            return this;
        }
        InnerClass address(String address){
            this.address = address;
            return this;
        }
        MyBuilder build(){
            return new MyBuilder(this);
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "MyBuilder{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}

// object pool design pattern

abstract class MyObjectPool{

    public Hashtable<Object,Long> lock,unlock;
    public Long expirationTime;

    MyObjectPool(){
        lock = new Hashtable<>();
        unlock = new Hashtable<>();
        expirationTime = Long.valueOf(5000);
    }

    synchronized Object getInstance(){
        Enumeration<Object> keys = unlock.keys();
        while(keys.hasMoreElements()){
            Object obj = keys.nextElement();
            if(isExpired(unlock.get(obj))){
                unlock.remove(obj);
            }
            else{
                return obj;
            }
        }
        // create new Elements
        return createElement();
    }
    public boolean isExpired(Long expiredTime){
        return System.currentTimeMillis()-expiredTime>=expiredTime?true:false;
    }
    public abstract Object createElement();
    public abstract void TaskCompleted(Object obj);
}
class CustomPool extends MyObjectPool{

    @Override
    public Object createElement() {
        Shape shape = new Circle();
        lock.put(shape,System.currentTimeMillis());
        return shape;
    }

    @Override
    public void TaskCompleted(Object obj) {
        this.lock.remove(obj);
        this.unlock.put(obj,System.currentTimeMillis());
    }
}

class MyProxy implements InvocationHandler{

    public Object target;

     MyProxy(Object obj){
        this.target = obj;
    }

    static Object getInstance(Object obj){
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),new MyProxy(obj));
    }
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        System.out.println("Before the method calls..");
        Object obj = method.invoke(target,objects);
        System.out.println("After the method calls...");
        return obj;
    }
}