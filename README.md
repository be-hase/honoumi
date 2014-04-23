# Honoumi
Honoumi is a High-performance RESTful Micro-framework (for JSON).

## Feature

* Network / Socket
	* Netty. So support async response.
* Dependency injection
	* Google Guice
* Logging
	* logback
* JSON
	* Jackson (faster ver)
* Utility Library
	* Guava
	* joda-time
	* apache commons

## Install

Write your pom.xml.

```xml

  <repositories>
    <repository>
      <id>honoumi-repo</id>
      <url>https://github.com/be-hase/maven-repo/tree/honoumi-0.0.3</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>com.be-hase</groupId>
      <artifactId>honoumi</artifactId>
      <version>0.0.3</version>
    </dependency>
  </dependencies>

```

## Basic Usage

### Routing

Create a Router instance, write a set of routing. 
Please pass parameters when you start the server this Router. (See below)

```java
//create router
Router mainRouter = new Router();
mainRouter.GET().route("/v1/user/{userId}").with(UserController.class, "show");
mainRouter.POST().route("/v1/user").with(UserController.class, "new");
mainRouter.POST().route("/v1/user/{userId}/edit").with(UserController.class, "edit");

mainRouter.GET().route("/healthcheck").with(HelthCheckController.class, "index");

```

### Controller

#### Simple Controller

If you want to response OK.

```java
public class HelthCheckController {
	
	public void index(
			MessageEvent evt
			) {		
		Response.execute(evt, HttpResponseStatus.OK, null, "OK");
	}
}
```

#### Getting Basic parameters into your controllers

By using the annotations, you can get the basic parameters.

```java
public class HogeController {
	private static Logger logger = LoggerFactory.getLogger(HogeController.class);

	public void formPost(
			MessageEvent evt,
			@Body String body,
			@FormParam("formParam") String formParam,
			@FormParams Map<String, String> formParams,
			@Header("X-Real-IP") String header,
			@Headers Map<String, String> headers,
			@PathParam("hoge") String pathParam,
			@PathParams Map<String, String> pathParams,
			@QueryParam("bar") String queryParam,
			@QueryParams Map<String, String> queryParams
			) {
			
		Response.execute(evt, HttpResponseStatus.OK, null, "OK"); //async response.
			
		logger.debug("body : {}", body);
		logger.debug("formParam : {}", formParam);
		logger.debug("formParams : {}", formParams);
		logger.debug("header : {}", header);
		logger.debug("headers : {}", headers);
		logger.debug("pathParam : {}", pathParam);
		logger.debug("pathParams : {}", pathParams);
		logger.debug("queryParam : {}", queryParam);
		logger.debug("queryParams : {}", queryParams);
	}
}

```

#### Use Argument Resolver

It also allows that if you create an Argument Resolver, to get the controller to any parameter. 

First, I want to create a Resolve.

```java
@Singleton
public class LogginedUserResolver implements ArgumentResolver<LogginedUser>{
	
	
	public LogginedUser resolveArgument(ChannelHandlerContext ctx, MessageEvent evt) {
		LogginedUser user = new LogginedUser();
		
		//user.setName...etc;
		
		return user;
	}

	public boolean supportedType(Class<?> klass) {
		return klass.isAssignableFrom(LogginedUser.class);
	}
}
```

To create annotation of Resolver that you created.

```java
@WithArgumentResolver(LogginedUserResolver.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface LogginedUserAnnotation {

}

```

After you create the file of the above two, written controller as follows.


```java
public class TestController {

    public void index(
            @LoggedInUser LogginedUser loggedInUser
            ) {

        //do something with the parameters...
    }

}
```

#### Use Filter

If you want to write a common processed by the controller, using the Filter. 

First of all, create a class that implement the Filter. 
by the filer method returns true, continue processing of the controller or the subsequent filter. 
On the other hand, end with this method in the case of false. 
So, to response properly in this case.

```java
@Singleton
public class CheckHttpHeaderFilter implements Filter {

	public boolean filter(ChannelHandlerContext ctx, MessageEvent evt) {
		HttpRequest request = (HttpRequest)evt.getMessage();
		
		final HttpHeaders httpHeaders = request.headers();
		
		if (!httpHeaders.contains("X-HOGE")) {
			Response.execute(evt, HttpResponseStatus.BAD_REQUEST, null, "INVALID HEADER");
			return false;
		}
		
		return true;
	}
}

```

Please to grant annotation to the controller. 
Either is fine in the method in the class. 
You can also be more than one grant. 
In that case, filter is called in the order in which they were granted. 
(class filter → method filter → controller method)

```java
public class TestController {

    @WithFilter({CheckHttpHeaderFilter.class})
    public void index(MessageEvent evt) {		
		    Response.execute(evt, HttpResponseStatus.OK, null, "OK");
    }
}
```

### Dependency injection

You can use the Google Guice.
Google guice document is here.

https://code.google.com/p/google-guice/wiki/Motivation?tm=6

```java
public class MyModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(UserService.class).to(UserServiceImpl.class).in(Singleton.class);

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		bind(Validator.class).toInstance(validator);;
	}
}

```

Please pass this Module as parameters when you start the server. (See below)


### Configuration Properties

You might have properties which writed as follows.

```
bar.int=1234
bar.string=string
bar.boolean=true

hoge=hoge
%local.hoge=localHoge
%release.hoge=releaseHoge

```

If you want to get the property, do the following:

```java
//string
String barString = ApplicationProperties.get("bar.string", "");

//int
Integer barInt = ApplicationProperties.getInt("bar.int", 0);

//boolean
Boolean barBoolean = ApplicationProperties.getBoolean("bar.boolean", false);

```

In addition, if you put a "%" in the prefix, the value can be retrieved in response to the activation mode will change. 
If there is no key for the mode corresponding, that nothing sticks will be selected.

```java
//when local mode, return "localHoge"
String localHoge = ApplicationProperties.get("hoge", "");

//when release mode, return "releaseHoge"
String releaseHoge = ApplicationProperties.get("hoge", "");

//when dev mode, return "local"
String devHoge = ApplicationProperties.get("hoge", "");

```

You can also get the value of a different mode if you specify the full name.

```java
String releaseHoge = ApplicationProperties.get("%release.hoge", "");
```

### Start Server

Please, start the server method which is the end point. 
This is the main method of the Main class in general.

```java
public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		//create router
    Router mainRouter = new Router();
    mainRouter.GET().route("/v1/user/{userId}").with(UserController.class, "show");
    mainRouter.POST().route("/v1/user").with(UserController.class, "new");
    mainRouter.POST().route("/v1/user/{userId}/edit").with(UserController.class, "edit");
    
    mainRouter.GET().route("/healthcheck").with(HelthCheckController.class, "index");
		
		//create guice module for DI (option)
		List<AbstractModule> modules = Lists.newArrayList();
		modules.add(new MyModule());
		
		//create server and start
		Server mainServer = Server.create("mainServer", mainRouter, modules);
		mainServer.start();
	}
}

```

First argument of Server.create is the name of the server. 

You can also take advantage of this name, to set and port of server, the option of netty to properties.

```

mainServer.bind.port=21014
mainServer.http.encoding=UTF-16
mainServer.http.keepAlive=false
mainServer.http.chunkAggregate=true
mainServer.http.chunkAggregate.maxContentLength=65535
mainServer.http.contentCompress=true
mainServer.netty.options.reuseAddress=true
mainServer.netty.options.child.reuseAddress=true
mainServer.netty.options.child.keepAlive=true
mainServer.netty.options.child.tcpNoDelay=true

```

In addition, you can also pass a ServerSocketChannelFactory of netty to create method. 
We will use the create method of this place if you want to change the thread pool.

### Start JAR

When you start server, please specify application.environment (required) application.properties (Optional).

```
ex: 
-Dapplication.environment=local -Dapplication.properties=application.properties
```

※ application.properties allows you to specify multiple comma-separated.

### What 'Honoumi' means?

From the name of the Characters of Love Live (Japanese anime).
