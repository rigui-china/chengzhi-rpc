## RPC 框架

RPC 专业上来说就是远程过程调用，是一种计算机通信协议，它允许程序在不同的计算机之间进行通信和交互，就像本地调用一样。

RPC 框架有了之后，一个程序（服务消费者）就能调用另一个程序（服务提供者）的接口，而不需要了解具体数据的传输处理过程、底层网络通信的细节等。这些都会由 RPC 框架帮你完成，使得开发者可以轻松调用远程服务，快速开发分布式系统。

以前没有 RPC 框架的时候，我们主机 A 想调用主机 B 上的一个服务，首先需要 B 提供服务接口，并且还需要知道主机 B 的 ip 地址和端口，主机 A 通过 http 或 tcp 等通信协议封装好请求然后编码发送给对应的地址和端口上的服务接口，然后主机 B 拿到请求之后，先解码然后再发送给对应的服务然后处理完请求之后再将结果编码返回给主机 A ，主机 A 收到之后再进行相应的操作或者结果不返回给主机 A 做相应处理。 



![1742548897602](C:\Users\29631\AppData\Roaming\Typora\typora-user-images\1742548897602.png)

没有 RPC 框架之前，消费者想要调用提供者，就需要提供者启动一个 web 服务，然后通过请求客户端发送 HTTP 或者其他协议的请求来调用。

比如请求 yupi.icu/order 地址后，提供者会调用 orderService 的 order 方法。

但是如果提供者提供了多个服务和方法，难道每个接口和方法都要单独写一个接口？消费者要针对每一个接口写一段 HTTP 调用的逻辑吗？

其实可以提供一个统一的服务调用接口，通过请求处理器根据客户端的请求参数来做不同的处理、调用不同的服务和方法。

可以在服务提供者处维护一个本地服务注册器，记录服务和对应实现类的映射。

![1742549314625](C:\Users\29631\AppData\Roaming\Typora\typora-user-images\1742549314625.png)

举个例子，消费者要调用 orderService 服务的 order 方法，可以发送请求，参数为 service = orderService，method = order，然后请求处理器会根据 service 从服务注册器里面找到对应的实现类，并且通过 Java 的反射机制调用 method 指定的方法。

需要注意的是，由于 Java 对象无法直接在网络中传输，所以要对传输的参数进行序列化和反序列化。

并且为了简化消费者发请求的代码，实现类似本地调用的体验。可以基于代理模式，为消费者要调用的接口生成一个代理对象，由代理对象完成请求和响应的过程。



### 简易版 RPC 框架

#### web 服务器

我们创建了服务消费者以及服务提供者的模板后，接下来，我们要先让服务提供者提供可远程访问的服务。那么，就需要一个 web 服务器，能够接受处理请求、并返回响应。

web 服务器的选择有很多，比如 Spring Boot 内嵌的 Tomcat、NIO 框架 Netty 和 Vert.x 等等。

#### 为什么使用 Vert.x 服务器呢？

因为 Vert.x 是基于 netty 构建的，采用异步非阻塞的 I/O 模型，能在少量线程下并发处理大量请求，避免线程的上下文切换浪费大量资源，tomcat 是基于线程池的阻塞 I/O 模型，每个请求分配一个线程处理。并且它十分轻量级以及容易扩展，占用内存少。它支持多种编程语言，天生支持响应式编程，能够非常方便的实现异步数据流处理和事件驱动模型。

此处我们使用高性能的 NIO 框架 Vert.x 来作为 RPC 框架的 web 服务器。

1. 首先先编写一个 web 服务器的接口 HttpServer，定义统一的启动服务器方法，便于后续的扩展，比如实现多种不同的 web 服务器。

   ```java
   package com.chengzhi.chengrpc.server;
   
   /**
    * HTTP 服务器接口
    */
   public interface HttpServer {
   
       /**
        * 启动服务器
        *
        * @param port
        */
       void doStart(int port);
   }
   ```

2. 然后编写基于 Vert.x 实现的 web 服务器 VertxHttpServer，能够监听指定端口并处理请求。

   首先创建一个 vertx 实例，然后实例创建 vertx 服务器然后先写一个请求处理器，接收到 HTTP 请求后做什么操作以及响应返回什么，最后就是启动 HTTP 服务器然后监听指定的端口。

   ```java
   package com.yupi.yurpc.server;
   
   import io.vertx.core.Vertx;
   
   public class VertxHttpServer implements HttpServer {
   
       public void doStart(int port) {
           // 创建 Vert.x 实例
           Vertx vertx = Vertx.vertx();
   
           // 创建 HTTP 服务器
           io.vertx.core.http.HttpServer server = vertx.createHttpServer();
   
           // 监听端口并处理请求
           server.requestHandler(request -> {
               // 处理 HTTP 请求
               System.out.println("Received request: " + request.method() + " " + request.uri());
   
               // 发送 HTTP 响应
               request.response()
                       .putHeader("content-type", "text/plain")
                       .end("Hello from Vert.x HTTP server!");
           });
   
           // 启动 HTTP 服务器并监听指定端口
           server.listen(port, result -> {
               if (result.succeeded()) {
                   System.out.println("Server is now listening on port " + port);
               } else {
                   System.err.println("Failed to start server: " + result.cause());
               }
           });
       }
   }
   
   ```

3. 验证 web 服务器能否启动成功并接受请求。

   这里就是启动 vertx 服务然后监听端口 8080 ，我们直接在浏览器输入 localhost：8080 看是否有正常响应即可验证 web 服务器是否启动成功并能接受请求。

   ```java
   package com.yupi.example.provider;
   
   import com.yupi.example.common.service.UserService;
   import com.yupi.yurpc.registry.LocalRegistry;
   import com.yupi.yurpc.server.HttpServer;
   import com.yupi.yurpc.server.VertxHttpServer;
   
   /**
    * 简易服务提供者示例
    */
   public class EasyProviderExample {
   
       public static void main(String[] args) {
           // 启动 web 服务
           HttpServer httpServer = new VertxHttpServer();
           httpServer.doStart(8080);
       }
   }
   ```

   

#### 本地服务注册器

我们现在做的简易 RPC 框架主要是跑通流程，所以暂时不用第三方注册中心，直接把服务注册到服务提供者本地即可。

我们创建一个本地服务注册器 LocalRegistry ，使用线程安全的 ConcurrentHashMap 存储服务注册信息，key 为服务名称、value 为服务的实现类。之后就可以根据要调用的服务名称获取到对应的实现类，然后通过反射进行方法调用了。

```java
package com.yupi.yurpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地注册中心
 */
public class LocalRegistry {

    /**
     * 注册信息存储
     */
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     *
     * @param serviceName
     * @param implClass
     */
    public static void register(String serviceName, Class<?> implClass) {
        map.put(serviceName, implClass);
    }

    /**
     * 获取服务
     *
     * @param serviceName
     * @return
     */
    public static Class<?> get(String serviceName) {
        return map.get(serviceName);
    }

    /**
     * 删除服务
     *
     * @param serviceName
     */
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }
}
```

注意，本地服务注册器和注册中心的作用是有去别的。注册中心的作用偏向于管理注册的服务、提供服务消息给消费者；而本地服务注册器的作用是根据服务名获取到对应的实现类，是完成调用必不可少的模块。

服务提供者启动时，需要注册服务到注册器中。

```java
package com.yupi.example.provider;

import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.registry.LocalRegistry;
import com.yupi.yurpc.server.HttpServer;
import com.yupi.yurpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {

    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
```



#### 序列化器

服务在本地注册后，我们就可以根据请求信息取出实现类并调用方法了。

但是在编写处理请求的逻辑前，我们要先实现序列化器模块。因为无论是请求或者响应，都会涉及参数的传输。而 Java 对象是存活在 JVM 虚拟机中的，如果想在其他位置存储并访问、或者在网络中进行传输，就需要进行序列化和反序列化。

序列化就是将 Java 对象转为可传输的字节数组。

反序列化就是将字节数组转换为 Java 对象。

有很多种不同的序列化方式，比如 Java 原生序列化、JSON、Hessian、Kryo、protobuf 等。

为了实现方便，此处选择 Java 原生的序列化器。

1. 我们先在 RPC 模块种编写序列化和反序列化的接口 Serializer，便于后续扩展更多的序列化器。

   ```java
   package com.yupi.yurpc.serializer;
   
   import java.io.IOException;
   
   /**
    * 序列化器接口
    */
   public interface Serializer {
   
       /**
        * 序列化
        *
        * @param object
        * @param <T>
        * @return
        * @throws IOException
        */
       <T> byte[] serialize(T object) throws IOException;
   
       /**
        * 反序列化
        *
        * @param bytes
        * @param type
        * @param <T>
        * @return
        * @throws IOException
        */
       <T> T deserialize(byte[] bytes, Class<T> type) throws IOException;
   }
   ```

2. 基于 Java 自带的序列化器实现 JdkSerializer

   ```java
   package com.yupi.yurpc.serializer;
   
   import java.io.*;
   
   /**
    * JDK 序列化器
    */
   public class JdkSerializer implements Serializer {
   
       /**
        * 序列化
        *
        * @param object
        * @param <T>
        * @return
        * @throws IOException
        */
       @Override
       public <T> byte[] serialize(T object) throws IOException {
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
           objectOutputStream.writeObject(object);
           objectOutputStream.close();
           return outputStream.toByteArray();
       }
   
       /**
        * 反序列化
        *
        * @param bytes
        * @param type
        * @param <T>
        * @return
        * @throws IOException
        */
       @Override
       public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
           ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
           ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
           try {
               return (T) objectInputStream.readObject();
           } catch (ClassNotFoundException e) {
               throw new RuntimeException(e);
           } finally {
               objectInputStream.close();
           }
       }
   }
   ```



#### 提供者处理调用 - 请求处理器

请求处理器是 RPC 框架的实现关键，它的作用是：处理接受到的请求，并根据请求参数找到对应的服务和方法，通过反射实现调用，最后封装返回结果并响应请求。

1. 在 RPC 模块中编写请求和响应封装类。

   ```java
   package com.yupi.yurpc.model;
   
   import lombok.AllArgsConstructor;
   import lombok.Builder;
   import lombok.Data;
   import lombok.NoArgsConstructor;
   
   import java.io.Serializable;
   
   /**
    * RPC 请求
    */
   @Data
   @Builder
   @AllArgsConstructor
   @NoArgsConstructor
   public class RpcRequest implements Serializable {
   
       /**
        * 服务名称
        */
       private String serviceName;
   
       /**
        * 方法名称
        */
       private String methodName;
   
       /**
        * 参数类型列表
        */
       private Class<?>[] parameterTypes;
   
       /**
        * 参数列表
        */
       private Object[] args;
   
   }
   
   package com.yupi.yurpc.model;
   
   import lombok.AllArgsConstructor;
   import lombok.Builder;
   import lombok.Data;
   import lombok.NoArgsConstructor;
   
   import java.io.Serializable;
   
   /**
    * RPC 响应
    */
   @Data
   @Builder
   @AllArgsConstructor
   @NoArgsConstructor
   public class RpcResponse implements Serializable {
   
       /**
        * 响应数据
        */
       private Object data;
   
       /**
        * 响应数据类型（预留）
        */
       private Class<?> dataType;
   
       /**
        * 响应信息
        */
       private String message;
   
       /**
        * 异常信息
        */
       private Exception exception;
   
   }
   
   ```

2. 编写请求处理器 HttpServerHandler

   业务流程如下：

   从请求中反序列化得到对象，然后从请求对象中获得请求参数和请求方法

   根据服务名称和方法名称从本地服务注册中获取到对应的服务实现类

   然后根据反射机制调用方法，得到返回结果

   将返回结果封装和序列化然后写到响应中

   ```java
   package com.yupi.yurpc.server;
   
   import com.yupi.yurpc.model.RpcRequest;
   import com.yupi.yurpc.model.RpcResponse;
   import com.yupi.yurpc.registry.LocalRegistry;
   import com.yupi.yurpc.serializer.JdkSerializer;
   import com.yupi.yurpc.serializer.Serializer;
   import io.vertx.core.Handler;
   import io.vertx.core.buffer.Buffer;
   import io.vertx.core.http.HttpServerRequest;
   import io.vertx.core.http.HttpServerResponse;
   
   import java.io.IOException;
   import java.lang.reflect.Method;
   
   /**
    * HTTP 请求处理
    */
   public class HttpServerHandler implements Handler<HttpServerRequest> {
   
       @Override
       public void handle(HttpServerRequest request) {
           // 指定序列化器
           final Serializer serializer = new JdkSerializer();
   
           // 记录日志
           System.out.println("Received request: " + request.method() + " " + request.uri());
   
           // 异步处理 HTTP 请求
           request.bodyHandler(body -> {
               byte[] bytes = body.getBytes();
               RpcRequest rpcRequest = null;
               try {
                   rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
               } catch (Exception e) {
                   e.printStackTrace();
               }
   
               // 构造响应结果对象
               RpcResponse rpcResponse = new RpcResponse();
               // 如果请求为 null，直接返回
               if (rpcRequest == null) {
                   rpcResponse.setMessage("rpcRequest is null");
                   doResponse(request, rpcResponse, serializer);
                   return;
               }
   
               try {
                   // 获取要调用的服务实现类，通过反射调用
                   Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                   Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                   Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                   // 封装返回结果
                   rpcResponse.setData(result);
                   rpcResponse.setDataType(method.getReturnType());
                   rpcResponse.setMessage("ok");
               } catch (Exception e) {
                   e.printStackTrace();
                   rpcResponse.setMessage(e.getMessage());
                   rpcResponse.setException(e);
               }
               // 响应
               doResponse(request, rpcResponse, serializer);
           });
       }
   
       /**
        * 响应
        *
        * @param request
        * @param rpcResponse
        * @param serializer
        */
       void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
           HttpServerResponse httpServerResponse = request.response()
                   .putHeader("content-type", "application/json");
           try {
               // 序列化
               byte[] serialized = serializer.serialize(rpcResponse);
               httpServerResponse.end(Buffer.buffer(serialized));
           } catch (IOException e) {
               e.printStackTrace();
               httpServerResponse.end(Buffer.buffer());
           }
       }
   }
   
   ```

   需要注意，不同的 web 服务器对应的请求处理器实现方式也不同，比如 Vert.x 中是通过实现 Handler<HttpServerRequeset> 接口来自定义请求处理器的。并且可以通过 request.bodyHandler 异步处理请求。

3. 给 HttpServer 绑定请求处理器。

   ```java
   package com.yupi.yurpc.server;
   
   import io.vertx.core.Vertx;
   
   /**
    * Vertx HTTP 服务器
    */
   public class VertxHttpServer implements HttpServer {
   
       /**
        * 启动服务器
        *
        * @param port
        */
       public void doStart(int port) {
           // 创建 Vert.x 实例
           Vertx vertx = Vertx.vertx();
   
           // 创建 HTTP 服务器
           io.vertx.core.http.HttpServer server = vertx.createHttpServer();
   
           // 监听端口并处理请求
           server.requestHandler(new HttpServerHandler());
   
           // 启动 HTTP 服务器并监听指定端口
           server.listen(port, result -> {
               if (result.succeeded()) {
                   System.out.println("Server is now listening on port " + port);
               } else {
                   System.err.println("Failed to start server: " + result.cause());
               }
           });
       }
   }
   ```

   至此，引入了 RPC 框架的服务提供者模块，已经能够接受请求并完成服务调用了。



#### 消费方发起调用 - 代理

我们在消费者的代码中，预留了一段获得 UserService 对象（实现类）的代码，只要填补上这段代码，我们就能跑通整个流程。

但是 UserService 的实现类哪里来呢？

我们可以通过静态代理和动态代理两种方式来生成代理对象简化消费方的调用。

**静态代理：**

我们创建一个 UserServiceProxy ，实现 UserService 接口和 getUser 方法。

```java
package com.yupi.example.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.yupi.example.common.model.User;
import com.yupi.example.common.service.UserService;
import com.yupi.yurpc.model.RpcRequest;
import com.yupi.yurpc.model.RpcResponse;
import com.yupi.yurpc.serializer.JdkSerializer;
import com.yupi.yurpc.serializer.Serializer;

import java.io.IOException;

/**
 * 静态代理
 */
public class UserServiceProxy implements UserService {

    public User getUser(User user) {
        // 指定序列化器
        Serializer serializer = new JdkSerializer();

        // 发请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try {
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            byte[] result;
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute()) {
                result = httpResponse.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
```

其实就是自己封装了要发送的请求参数，自己编码发送给对应的端口，然后接收到响应之后，将其解码后得到其中的返回对象。但是这样太麻烦了，难不成我们以后每次每个方法都要自己写一个方法来实现对应的接口吗？



**动态代理：**

动态代理的作用是，根据要生成的对象的类型，自动生成一个代理对象。

常用的动态代理实现方式有 JDK 动态代理和基于字节码生成的动态代理（比如 CGLIB）。前者简单易用、无需引入额外的库，但缺点是只能对接口进行代理；后者更灵活、可以对任何类进行代理，但性能略低于 JDK 动态代理。

此处我们使用 JDK 动态代理。

1. 在 RPC 模块中编写动态代理类 ServiceProxy，需要实现 InvocationHandler 的 invoke 方法。

   ```java
   package com.yupi.yurpc.proxy;
   
   import cn.hutool.http.HttpRequest;
   import cn.hutool.http.HttpResponse;
   import com.yupi.yurpc.model.RpcRequest;
   import com.yupi.yurpc.model.RpcResponse;
   import com.yupi.yurpc.serializer.JdkSerializer;
   import com.yupi.yurpc.serializer.Serializer;
   
   import java.io.IOException;
   import java.lang.reflect.InvocationHandler;
   import java.lang.reflect.Method;
   
   /**
    * 服务代理（JDK 动态代理）
    */
   public class ServiceProxy implements InvocationHandler {
   
       /**
        * 调用代理
        *
        * @return
        * @throws Throwable
        */
       @Override
       public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
           // 指定序列化器
           Serializer serializer = new JdkSerializer();
   
           // 构造请求
           RpcRequest rpcRequest = RpcRequest.builder()
                   .serviceName(method.getDeclaringClass().getName())
                   .methodName(method.getName())
                   .parameterTypes(method.getParameterTypes())
                   .args(args)
                   .build();
           try {
               // 序列化
               byte[] bodyBytes = serializer.serialize(rpcRequest);
               // 发送请求
               // todo 注意，这里地址被硬编码了（需要使用注册中心和服务发现机制解决）
               try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                       .body(bodyBytes)
                       .execute()) {
                   byte[] result = httpResponse.bodyBytes();
                   // 反序列化
                   RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                   return rpcResponse.getData();
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
   
           return null;
       }
   }
   ```

   你会发现，其实逻辑跟静态代理的逻辑一样，只不过我们这样传入的方法和参数是形参而不是实参，所以可以根据传入的方法跟参数做出对应的操作。

2. 创建动态代理工厂 ServiceProxyFactory，作用是根据指定类创建动态代理对象。

   ```java
   package com.yupi.yurpc.proxy;
   
   import java.lang.reflect.Proxy;
   
   /**
    * 服务代理工厂（用于创建代理对象）
    */
   public class ServiceProxyFactory {
   
       /**
        * 根据服务类获取代理对象
        *
        * @param serviceClass
        * @param <T>
        * @return
        */
       public static <T> T getProxy(Class<T> serviceClass) {
           return (T) Proxy.newProxyInstance(
                   serviceClass.getClassLoader(),
                   new Class[]{serviceClass},
                   new ServiceProxy());
       }
   }
   ```

   这里的 new ServiceProxy() 就是我们刚才写的动态代理的处理逻辑，这样子我们生成的代理对象调用相应的方法的时候就会根据这个 动态代理的逻辑去发送请求和接受相应。



服务调用方使用 rpc 框架提供的动态代理工厂，动态代理工厂根据传进来的服务类进行一个代理对象的生成，代理对象调用方法的时候会去走到这个代理对象的 invoke 方法，根据传进来的方法构造一个 request 请求带上方法和请求参数发送到对应的地址和端口然后接收响应并返回。

服务提供者提供的请求处理器接收到请求后，先解析出相应参数，比如要调用的方法和实现类，然后利用反射调用相关类的方法去实现对应的逻辑然后将结果包装成 response 经过编码发送给服务调用方，然后服务调用方的 invoke 会把收到的响应进行解码然后取出其中对应的数据然后返回。



### 全局配置加载

在 RPC 框架运行的过程中，会涉及很多的配置信息，比如注册中心的地址、序列化方式、网络服务器端口号等等。

之前的简易版 RPC 项目中，我们是在程序里面写死了这些配置，不利于维护。

我们应当允许引入框架的项目通过编写配置文件来**自定义配置**。并且一般情况下，服务提供者和服务消费者需要编写相同的 RPC 配置。

因此，我们需要一套全局配置加载功能。

首先我们梳理需要的配置项，刚开始就一切从简，只提供以下几个配置项即可：

- name 名称
- version 版本号
- serverHost 服务器主机名
- serverPort 服务器端口号

#### 读取配置文件

如何读取配置文件呢？这里可以使用 Java 的 Properties 类自行编写，但是更推荐使用一些第三方工具库，比如 Hutool 的 Setting 模块，可以直接读取指定名称的配置文件中的部分配置信息，并且转换成 Java 对象，非常方便。

#### 代码编写

1. 在 config 包下新建配置类 RpcConfig，用于保存配置信息。

   ```java
   package com.yupi.yurpc.config;
   
   import lombok.Data;
   
   /**
    * RPC 框架配置
    */
   @Data
   public class RpcConfig {
   
       /**
        * 名称
        */
       private String name = "yu-rpc";
   
       /**
        * 版本号
        */
       private String version = "1.0";
   
       /**
        * 服务器主机名
        */
       private String serverHost = "localhost";
       
       /**
        * 服务器端口号
        */
       private Integer serverPort = 8080;
   
   }
   ```

2. 在 utils 包下新建工具类 ConfigUtils，作用是读取配置文件并返回配置对象，可以简化调用。

   ```java
   package com.yupi.yurpc.utils;
   
   import cn.hutool.core.util.StrUtil;
   import cn.hutool.setting.dialect.Props;
   
   /**
    * 配置工具类
    */
   public class ConfigUtils {
   
       /**
        * 加载配置对象
        *
        * @param tClass
        * @param prefix
        * @param <T>
        * @return
        */
       public static <T> T loadConfig(Class<T> tClass, String prefix) {
           return loadConfig(tClass, prefix, "");
       }
   
       /**
        * 加载配置对象，支持区分环境
        *
        * @param tClass
        * @param prefix
        * @param environment
        * @param <T>
        * @return
        */
       public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
           StringBuilder configFileBuilder = new StringBuilder("application");
           if (StrUtil.isNotBlank(environment)) {
               configFileBuilder.append("-").append(environment);
           }
           configFileBuilder.append(".properties");
           Props props = new Props(configFileBuilder.toString());
           return props.toBean(tClass, prefix);
       }
   }
   ```

3. 在 constant 包中新建 RpcConstant 接口，用于存储 RPC 框架相关的常量。

   ```java
   package com.yupi.yurpc.constant;
   
   /**
    * RPC 相关常量
    *
    * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
    * @learn <a href="https://codefather.cn">鱼皮的编程宝典</a>
    * @from <a href="https://yupi.icu">编程导航学习圈</a>
    */
   public interface RpcConstant {
   
       /**
        * 默认配置文件加载前缀
        */
       String DEFAULT_CONFIG_PREFIX = "rpc";
   }
   ```

4. 维护一个全局的配置对象。在引入 RPC 框架的项目启动时，从配置文件中读取配置并创建对象实例，之后就可以集中地从这个对象中获取配置信息，而不用每次加载配置时再重新读取配置、并创建新的对象，减少了性能开销。

   使用设计模式中的**单例模式**，就饿能够很轻松地实现这个需求了。

   ```java
   package com.yupi.yurpc;
   
   import com.yupi.yurpc.config.RpcConfig;
   import com.yupi.yurpc.constant.RpcConstant;
   import com.yupi.yurpc.utils.ConfigUtils;
   import lombok.extern.slf4j.Slf4j;
   
   /**
    * RPC 框架应用
    * 相当于 holder，存放了项目全局用到的变量。双检锁单例模式实现
    */
   @Slf4j
   public class RpcApplication {
   
       private static volatile RpcConfig rpcConfig;
   
       /**
        * 框架初始化，支持传入自定义配置
        *
        * @param newRpcConfig
        */
       public static void init(RpcConfig newRpcConfig) {
           rpcConfig = newRpcConfig;
           log.info("rpc init, config = {}", newRpcConfig.toString());
       }
   
       /**
        * 初始化
        */
       public static void init() {
           RpcConfig newRpcConfig;
           try {
               newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
           } catch (Exception e) {
               // 配置加载失败，使用默认值
               newRpcConfig = new RpcConfig();
           }
           init(newRpcConfig);
       }
   
       /**
        * 获取配置
        *
        * @return
        */
       public static RpcConfig getRpcConfig() {
           if (rpcConfig == null) {
               synchronized (RpcApplication.class) {
                   if (rpcConfig == null) {
                       init();
                   }
               }
           }
           return rpcConfig;
       }
   }
   ```

   上述代码其实就是双检锁单例模式的经典实现，支持在获取配置时才调用 init 方法实现懒加载。

   单例模式就是一个类只有一个实例能够获取和访问，这个类提供一个全局的访问点，只有这个类的实例第一次不存在的时候才进行创建，以后访问这个类的时候，只要这个实例存在就去访问这个实例而无需再重新创建实例。



### 接口 Mock

1. 我们可以支持开发者通过修改配置文件的方式开启 mock，那么首先给全局配置类 RpcConfig 新增 mock 字段，默认值为 false。

   ```java
   @Data
   public class RpcConfig {
       ...
       
       /**
        * 模拟调用
        */
       private boolean mock = false;
   }
   ```

2. 在 Proxy 包下新增 MockServiceProxy 类，用于生成 mock 代理服务。

   ```java
   package com.yupi.yurpc.proxy;
   
   import lombok.extern.slf4j.Slf4j;
   
   import java.lang.reflect.InvocationHandler;
   import java.lang.reflect.Method;
   
   /**
    * Mock 服务代理（JDK 动态代理）
    *
    * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
    * @learn <a href="https://codefather.cn">编程宝典</a>
    * @from <a href="https://yupi.icu">编程导航知识星球</a>
    */
   @Slf4j
   public class MockServiceProxy implements InvocationHandler {
   
       /**
        * 调用代理
        *
        * @return
        * @throws Throwable
        */
       @Override
       public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
           // 根据方法的返回值类型，生成特定的默认值对象
           Class<?> methodReturnType = method.getReturnType();
           log.info("mock invoke {}", method.getName());
           return getDefaultObject(methodReturnType);
       }
   
       /**
        * 生成指定类型的默认值对象（可自行完善默认值逻辑）
        *
        * @param type
        * @return
        */
       private Object getDefaultObject(Class<?> type) {
           // 基本类型
           if (type.isPrimitive()) {
               if (type == boolean.class) {
                   return false;
               } else if (type == short.class) {
                   return (short) 0;
               } else if (type == int.class) {
                   return 0;
               } else if (type == long.class) {
                   return 0L;
               }
           }
           // 对象类型
           return null;
       }
   }
   ```

   简单来说就是根据传入的方法的返回类型然后返回该类型的默认值。

3. 给 ServiceProxyFactory 服务代理工厂新增获取 mock 代理对象的方法 getMockProxy。可以通过读取已定义的全局配置 mock 来区分创建哪种代理对象。

   ```java
   package com.yupi.yurpc.proxy;
   
   import com.yupi.yurpc.RpcApplication;
   
   import java.lang.reflect.Proxy;
   
   /**
    * 服务代理工厂（用于创建代理对象）
    *
    * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
    * @learn <a href="https://codefather.cn">编程宝典</a>
    * @from <a href="https://yupi.icu">编程导航知识星球</a>
    */
   public class ServiceProxyFactory {
   
       /**
        * 根据服务类获取代理对象
        *
        * @param serviceClass
        * @param <T>
        * @return
        */
       public static <T> T getProxy(Class<T> serviceClass) {
           if (RpcApplication.getRpcConfig().isMock()) {
               return getMockProxy(serviceClass);
           }
   
           return (T) Proxy.newProxyInstance(
                   serviceClass.getClassLoader(),
                   new Class[]{serviceClass},
                   new ServiceProxy());
       }
   
       /**
        * 根据服务类获取 Mock 代理对象
        *
        * @param serviceClass
        * @param <T>
        * @return
        */
       public static <T> T getMockProxy(Class<T> serviceClass) {
           return (T) Proxy.newProxyInstance(
                   serviceClass.getClassLoader(),
                   new Class[]{serviceClass},
                   new MockServiceProxy());
       }
   }
   
   ```



### 序列化器与 SPI 机制

我们还编写了通用的序列化器接口，并且已经实现了基于 Java 原生序列化的序列化器。但是对于一个完善的 RPC 框架，我们还要思考以下三个问题：

1. 有没有更好的序列化器实现方式
2. 如何让使用框架的开发者指定使用的序列化器
3. 如何让使用框架的开发者自己定制序列化器

#### 动态使用序列化器

理想情况下，应该可以通过配置文件来指定使用的序列化器。在使用序列化器时，根据配置来获取不同的序列化器实例即可。

这个操作并不难，我们只需定义一个 序列化器名称 =》序列化器实现类对象 的 Map，然后根据名称从 Map 中获取对象即可。

#### 自定义序列化器

如果开发者不想使用我们框架内置的序列化器，想要自己定义一个新的序列化器实现，但不能修改我们写好的框架代码，应该怎么办呢？

思路很简单：只要我们的 RPC 框架能够读取到用户自定义的类路径，然后加载这个类，作为 Serializer 序列化器接口的实现即可。

但是如何实现这个操作呢？

这就需要我们学习一个新的概念，也是 Java 中的重要特性 —— SPI 机制。

**什么是 SPI？**

SPI 服务提供接口是 Java 的机制，主要用于实现模块化开发和插件化扩展。

SPI 机制允许服务提供者通过特定的配置文件将自己的实现注册到系统中，然后系统通过反射机制加载这些实现，而不需要修改原始框架的代码，从而实现了系统的解耦、提高了可扩展性。

一个典型的 SPI 应用场景就是 JDBC（Java 数据库链接库），不同的数据库驱动程序开发者可以使用 JDBC 库，然后定制自己的数据库驱动程序。

#### 系统实现 SPI 

Java 提供了 SPI 机制相关的 API 接口，可以直接使用。

1. 首先在 resources 资源目录下创建 META-INF/services 目录，并且创建一个名称为要实现的接口的空文件。

   ![1742612443254](C:\Users\29631\AppData\Roaming\Typora\typora-user-images\1742612443254.png)

2. 在文件中填写自己定制的接口实现类的完整类路径，

   ![1742612497356](C:\Users\29631\AppData\Roaming\Typora\typora-user-images\1742612497356.png)

3. 直接使用系统内置的 ServiceLoader 动态加载指定接口的实现类

   ```java
   // 指定序列化器
   Serializer serializer = null;
   ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
   for (Serializer service : serviceLoader) {
       serializer = service;
   }
   ```

   上述代码能够获取所有文件中编写的实现类对象，选择一个使用即可。



#### 自定义 SPI 实现

首先编写 JSON 、Kryo、Hessian 序列化器的实现。

然后具体的实现如下：

1. 首先定义序列化器名称的常量，使用接口实现

   ```java
   package com.yupi.yurpc.serializer;
   
   /**
    * 序列化器键名
    */
   public interface SerializerKeys {
   
       String JDK = "jdk";
       String JSON = "json";
       String KRYO = "kryo";
       String HESSIAN = "hessian";
   
   }
   ```

2. 定义序列化器工厂

   序列化器对象是可以复用的，没必要每次执行序列化操作前都创建一个新的对象。所以我们可以使用设计模式中的工厂模式 + 单例模式 来简化创建和获取序列化器对象的操作。

   ```java
    package com.yupi.yurpc.serializer;
   
   import java.util.HashMap;
   import java.util.Map;
   
   /**
    * 序列化器工厂（用于获取序列化器对象）
    *
    * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
    * @learn <a href="https://codefather.cn">编程宝典</a>
    * @from <a href="https://yupi.icu">编程导航知识星球</a>
    */
   public class SerializerFactory {
   
       /**
        * 序列化映射（用于实现单例）
        */
       private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<String, Serializer>() {{
           put(SerializerKeys.JDK, new JdkSerializer());
           put(SerializerKeys.JSON, new JsonSerializer());
           put(SerializerKeys.KRYO, new KryoSerializer());
           put(SerializerKeys.HESSIAN, new HessianSerializer());
       }};
   
       /**
        * 默认序列化器
        */
       private static final Serializer DEFAULT_SERIALIZER = KEY_SERIALIZER_MAP.get("jdk");
   
       /**
        * 获取实例
        *
        * @param key
        * @return
        */
       public static Serializer getInstance(String key) {
           return KEY_SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
       }
   
   }
   ```

3. 在全局配置类 RpcConfig 中补充序列化器的配置

   ```java
   public class RpcConfig {
       ...
       
       /**
        * 序列化器
        */
       private String serializer = SerializerKeys.JDK;
   }
   ```

4. 动态获取序列化器

   需要将之前代码中所有用到序列化器的位置更改为 “使用工厂 + 读取配置” 来获取实现类。

   ```java
   // 指定序列化器
   final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
   ```



![1742613163085](C:\Users\29631\AppData\Roaming\Typora\typora-user-images\1742613163085.png)

编写 SpiLoader 加载器。相当于一个工具类，提供了读取配置并加载实现类的方法。

关键实现如下：

1. 用 Map 来存储已加载的配置信息 键名 =》 实现类
2. 扫描指定路径，读取每个配置文件，获取到 键名 =》实现类 信息并存储在 Map 中
3. 定义获取实例方法，根据用户传入的接口和键名，从 Map 中找到对应的实现类，然后通过反射获取到实现类对象。可以维护一个对象实例缓存，创建过一次的对象从缓存中读取即可。

```java
package com.yupi.yurpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.yupi.yurpc.serializer.Serializer;
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
 * SPI 加载器（支持键值对映射）
 */
@Slf4j
public class SpiLoader {

    /**
     * 存储已加载的类：接口名 =>（key => 实现类）
     */
    private static Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存（避免重复 new），类路径 => 对象实例，单例模式
     */
    private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统 SPI 目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 用户自定义 SPI 目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll() {
        log.info("加载所有 SPI");
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }

    /**
     * 获取某个接口的实例
     *
     * @param tClass
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
        if (keyClassMap == null) {
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", tClassName));
        }
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key=%s 的类型", tClassName, key));
        }
        // 获取到要加载的实现类型
        Class<?> implClass = keyClassMap.get(key);
        // 从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMsg = String.format("%s 类实例化失败", implClassName);
                throw new RuntimeException(errorMsg, e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }

    /**
     * 加载某个类型
     *
     * @param loadClass
     * @throws IOException
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为 {} 的 SPI", loadClass.getName());
        // 扫描路径，用户自定义的 SPI 优先级高于系统 SPI
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for (String scanDir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            // 读取每个资源文件
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");
                        if (strArray.length > 1) {
                            String key = strArray[0];
                            String className = strArray[1];
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("spi resource load error", e);
                }
            }
        }
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }
}

```



之前，我们是通过在工厂中硬编码 HashMap 来存储序列化器和实现类的，有了 SPI 后，就可以改为从 SPI 加载指定的序列化器对象。

```java
package com.yupi.yurpc.serializer;

import com.yupi.yurpc.spi.SpiLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 序列化器工厂（用于获取序列化器对象）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class SerializerFactory {

    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
```



### 注册中心

RPC 框架的一个核心模块是注册中心，目的是帮助服务消费者获取到服务提供者的调用地址，而不用将调用地址硬编码到项目中。

我们需要一个能够集中存储和读取数据的中间件。此外，它还需要有数据过期、数据监听的能力，便于我们移除失效节点、更新节点列表等。

主流的注册中心中间件有 ZooKeeper、Redis 等。我们使用一种更新颖的、更适合存储元信息（注册信息）的云原生中间件 Etcd，来实现注册中心。

Etcd 使用 Raft 一致性算法来保证数据的一致性。Raft 是一种分布式一致性算法，它确保了分布式系统中的所有节点在任何时间点都能达成一致的数据视图。

具体来说，Raft 算法通过选举机制选举出一个领导者节点，领导者负责接收客户端的写请求，并将写操作复制到其他节点上。当客户端发送写请求时，领导者首先将写操作写入自己的日志中，并将写操作的日志发送给其他节点，其他节点收到后也将其写入自己的日志中。一旦大多数节点（即半数以上的节点）都成功写入到自己的日志中，该日志就被视为已提交，领导者会向客户端发送成功响应。在领导者发送成功响应后，该写操作就被视为已提交，从而保证了数据的一致性。

如果领导者节点宕机或失去联系，Raft 算法会在剩余的节点里面再选举出一个新的领导者，从而保证系统的可用性和一致性。

#### 注册中心开发

1. 注册信息定义

   在 model 包下新建 ServiceMetaInfo 类，封装服务的注册信息，包括服务名称、服务版本号、服务地址（域名和端口号）、服务分组等。

   ```java
   package com.yupi.yurpc.model;
   
   /**
    * 服务元信息（注册信息）
    */
   public class ServiceMetaInfo {
   
   
       /**
        * 服务名称
        */
       private String serviceName;
   
       /**
        * 服务版本号
        */
       private String serviceVersion = "1.0";
   
       /**
        * 服务域名
        */
       private String serviceHost;
   
       /**
        * 服务端口号
        */
       private Integer servicePort;
   
       /**
        * 服务分组（暂未实现）
        */
       private String serviceGroup = "default";
   
   }
   ```

   需要给 ServiceMetaInfo 增加一些工具方法，用于获取服务注册键名、获取服务注册节点键名等。

   可以把版本号和分组都放到服务键名中，就可以在查询时根据这些参数获取对应版本和分组的服务了。

   ```java
   /**
    * 获取服务键名
    *
    * @return
    */
   public String getServiceKey() {
       // 后续可扩展服务分组
       // return String.format("%s:%s:%s", serviceName, serviceVersion, serviceGroup);
       return String.format("%s:%s", serviceName, serviceVersion);
   }
   
   /**
    * 获取服务注册节点键名
    *
    * @return
    */
   public String getServiceNodeKey() {
       return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
   }
   ```

   给 RpcRequest 和 RpcConstant 都增加相应字段。

2. 注册中心配置

   在 config 包下编写注册中心配置类 RegistryConfig，让用户配置连接注册中心所需的信息，比如注册中心类别、注册中心地址、用户名、密码、连接超时时间等。

   ```java
   package com.yupi.yurpc.config;
   
   import lombok.Data;
   
   /**
    * RPC 框架注册中心配置
    */
   @Data
   public class RegistryConfig {
   
       /**
        * 注册中心类别
        */
       private String registry = "etcd";
   
       /**
        * 注册中心地址
        */
       private String address = "http://localhost:2380";
   
       /**
        * 用户名
        */
       private String username;
   
       /**
        * 密码
        */
       private String password;
   
       /**
        * 超时时间（单位毫秒）
        */
       private Long timeout = 10000L;
   }
   ```

   还要为 RpcConfig 全局配置补充注册中心配置

   ```java
   @Data
   public class RpcConfig {
       ...
       
       /**
        * 注册中心配置
        */
       private RegistryConfig registryConfig = new RegistryConfig();
   }
   ```

3. 注册中心接口。

   遵循可扩展设计，我们先写一个注册中心接口，后续可以实现多种不同的注册中心，并且和序列化器一样，可以使用 SPI 机制动态加载。

   ```java
   package com.yupi.yurpc.registry;
   
   import com.yupi.yurpc.config.RegistryConfig;
   import com.yupi.yurpc.model.ServiceMetaInfo;
   
   import java.util.List;
   
   /**
    * 注册中心
    *
    * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
    * @learn <a href="https://codefather.cn">编程宝典</a>
    * @from <a href="https://yupi.icu">编程导航知识星球</a>
    */
   public interface Registry {
   
       /**
        * 初始化
        *
        * @param registryConfig
        */
       void init(RegistryConfig registryConfig);
   
       /**
        * 注册服务（服务端）
        *
        * @param serviceMetaInfo
        */
       void register(ServiceMetaInfo serviceMetaInfo) throws Exception;
   
       /**
        * 注销服务（服务端）
        *
        * @param serviceMetaInfo
        */
       void unRegister(ServiceMetaInfo serviceMetaInfo);
   
       /**
        * 服务发现（获取某服务的所有节点，消费端）
        *
        * @param serviceKey 服务键名
        * @return
        */
       List<ServiceMetaInfo> serviceDiscovery(String serviceKey);
   
       /**
        * 服务销毁
        */
       void destroy();
   }
   ```

4. Etcd 注册中心实现

   在 registry 目录下新建 EtcdRegistry 类，实现注册中心接口，先完成初始化方法，读取注册中心配置并初始化客户端对象。

   ```java
   public class EtcdRegistry implements Registry {
   
       private Client client;
   
       private KV kvClient;
       
       /**
        * 根节点
        */
       private static final String ETCD_ROOT_PATH = "/rpc/";
   
       @Override
       public void init(RegistryConfig registryConfig) {
           client = Client.builder().endpoints(registryConfig.getAddress()).connectTimeout(Duration.ofMillis(registryConfig.getTimeout())).build();
           kvClient = client.getKVClient();
       }
   }
   
   @Override
   public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
       // 创建 Lease 和 KV 客户端
       Lease leaseClient = client.getLeaseClient();
   
       // 创建一个 30 秒的租约
       long leaseId = leaseClient.grant(30).get().getID();
   
       // 设置要存储的键值对
       String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
       ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
       ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
   
       // 将键值对与租约关联起来，并设置过期时间
       PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
       kvClient.put(key, value, putOption).get();
   }
   
   public void unRegister(ServiceMetaInfo serviceMetaInfo) {
       kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
   }
   
   public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
       // 前缀搜索，结尾一定要加 '/'
       String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";
   
       try {
           // 前缀查询
           GetOption getOption = GetOption.builder().isPrefix(true).build();
           List<KeyValue> keyValues = kvClient.get(
                           ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                           getOption)
                   .get()
                   .getKvs();
           // 解析服务信息
           return keyValues.stream()
                   .map(keyValue -> {
                       String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                       return JSONUtil.toBean(value, ServiceMetaInfo.class);
                   })
                   .collect(Collectors.toList());
       } catch (Exception e) {
           throw new RuntimeException("获取服务列表失败", e);
       }
   }
   
   public void destroy() {
       System.out.println("当前节点下线");
       // 释放资源
       if (kvClient != null) {
           kvClient.close();
       }
       if (client != null) {
           client.close();
       }
   }
   
   ```



#### 支持配置和扩展注册中心

开发方式和序列化器也是一样的，都可以使用工厂创建对象、使用 SPI 动态加载自定义的注册中心。

```java
package com.yupi.yurpc.registry;

import com.yupi.yurpc.spi.SpiLoader;

/**
 * 注册中心工厂（用于获取注册中心对象）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class RegistryFactory {

    static {
        SpiLoader.load(Registry.class);
    }

    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Registry getInstance(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }

}
```

![1742626864171](C:\Users\29631\AppData\Roaming\Typora\typora-user-images\1742626864171.png)

![1742626875920](C:\Users\29631\AppData\Roaming\Typora\typora-user-images\1742626875920.png)



### 注册中心优化

#### 心跳检测相应服务为其自动续期

在 Etcd 中，我们要实现心跳检测和续期机制，可以遵循以下步骤：

1. 服务提供者向 Etcd 注册自己的服务信息，并在注册时设置 TTL（生存时间）。
2. Etcd 在接收到服务提供者的注册信息后，会自动维护服务信息的 TTL，并在 TTL 过期时删除该服务信息
3. 服务提供者定期请求 Etcd 续签自己的注册信息，重写 TTL。

在注册服务和删除服务的时候将相应节点保存或从一个集合中添加和删除。

然后是心跳检测的具体代码，用 Hutool 的 CronUtil 实现定时任务，对所有集合中的节点执行重新注册操作。

```java
@Override
public void heartBeat() {
    // 10 秒续签一次
    CronUtil.schedule("*/10 * * * * *", new Task() {
        @Override
        public void execute() {
            // 遍历本节点所有的 key
            for (String key : localRegisterNodeKeySet) {
                try {
                    List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                            .get()
                            .getKvs();
                    // 该节点已过期（需要重启节点才能重新注册）
                    if (CollUtil.isEmpty(keyValues)) {
                        continue;
                    }
                    // 节点未过期，重新注册（相当于续签）
                    KeyValue keyValue = keyValues.get(0);
                    String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                    ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                    register(serviceMetaInfo);
                } catch (Exception e) {
                    throw new RuntimeException(key + "续签失败", e);
                }
            }
        }
    });

    // 支持秒级别定时任务
    CronUtil.setMatchSecond(true);
    CronUtil.start();
}
```

然后在注册中心初始化的 init 方法中，调用 heartBeat 方法即可。

```java
@Override
public void init(RegistryConfig registryConfig) {
    client = Client.builder()
            .endpoints(registryConfig.getAddress())
            .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
            .build();
    kvClient = client.getKVClient();
    heartBeat();
}
```



#### 服务节点下线机制

被动下线：服务提供者项目异常退出时，利用 Etcd 的 key 过期机制自动移除。

主动下线：服务提供者项目正常退出时，主动从注册中心移除注册信息。

被动下线可以利用 Etcd 的机制实现。

主动下线我们利用 JVM 的 ShutdownHook 实现，它允许开发者在 JVM 即将关闭前执行一些清理工作或其他必要的操作，例如关闭数据库连接、释放资源、保存临时数据等。

1. 节点下线逻辑

   ```java
   public void destroy() {
       System.out.println("当前节点下线");
       // 下线节点
       // 遍历本节点所有的 key
       for (String key : localRegisterNodeKeySet) {
           try {
               kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
           } catch (Exception e) {
               throw new RuntimeException(key + "节点下线失败");
           }
       }
   
       // 释放资源
       if (kvClient != null) {
           kvClient.close();
       }
       if (client != null) {
           client.close();
       }
   }
   ```

2. 在 RpcApplication 的 init 方法中，注册 Shutdown Hook，当程序正常退出时会执行注册中心的 destroy 方法。

   ```java
   public static void init(RpcConfig newRpcConfig) {
       rpcConfig = newRpcConfig;
       log.info("rpc init, config = {}", newRpcConfig.toString());
       // 注册中心初始化
       RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
       Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
       registry.init(registryConfig);
       log.info("registry init, config = {}", registryConfig);
       
       // 创建并注册 Shutdown Hook，JVM 退出时执行操作
       Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
   }
   ```

#### 消费端服务缓存

服务发现的时候首先从缓存中获取，获取不到再从注册中心获取服务。

还有消费端需要在服务发现方法中添加一个监听节点注册的服务的方法，当相应节点下线的时候，将节点的服务从缓存中删除。

```java
/**
 * 监听（消费端）
 *
 * @param serviceNodeKey
 */
@Override
public void watch(String serviceNodeKey) {
    Watch watchClient = client.getWatchClient();
    // 之前未被监听，开启监听
    boolean newWatch = watchingKeySet.add(serviceNodeKey);
    if (newWatch) {
        watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
            for (WatchEvent event : response.getEvents()) {
                switch (event.getEventType()) {
                    // key 删除时触发
                    case DELETE:
                        // 清理注册服务缓存
                        registryServiceCache.clearCache();
                        break;
                    case PUT:
                    default:
                        break;
                }
            }
        });
    }
}
```
