> Java library to wire services and their factories

# Dependency injection

- **Service Locator Pattern:** Utilize the `ServiceLocator` and `ServiceManager` classes to implement the Service Locator pattern, allowing for efficient and centralized management of services within your application.
- **Dependency Injection:** Register your own factories using the `FactoryInterface` to create your services or simply use the default factory (`AutowireFactory`) if your service depends on solely other services which are resolvable using the ServiceLocator.
- **Optimized autowiring:** Annotate your services with ``@FactoryGenerated`` if they're applicable for autowiring. This leverages annotation processing to achieve slightly faster service resolving.

## Installation

```groovy
repositories {
    maven { url 'https://registry.provided.space' }
}

dependencies {
    implementation 'space.provided:dependency-injection:2.0.1'
    annotationProcessor 'space.provided:dependency-injection:2.0.1'
}
```

## Example

Given you have a Worker to handle tasks within your application and want to debug how long each execution took. The debug output can be stored anywhere.
```java
@FactoryGenerated
public final class Worker {

    private final Logger logger;

    public Worker(Logger logger) {
        this.logger = logger;
    }

    public void dispatch() {
        // ...
        logger.debug(String.format("Dispatching took %1$s.", end - start));
    }
}

public interface Logger {

    void debug(String message);
}
```

The next step would be to define an implementation for the Logger interface and a service factory to create an instance of the implementation.
```java
public final class ConsoleLogger implements Logger {

    @Override
    public void debug(String message) {
        System.out.println(message);
    }
}

public final class LoggerFactory implements FactoryInterface<Logger> {

    @Override
    public Result<Logger, String> create(Class<? extends Logger> identifier, ServiceLocator locator) {
        return Result.ok(new ConsoleLogger());
    }
}
```

Configuring the `ServiceManager` could look something like this.
```java
final ServiceManager services = new ServiceManager();

// Autowiring
// Option 1: Default factory which will run at runtime
services.register(Worker.class);

// Option 2: Generated factory which will run at compile time
services.register(Worker.class, new WorkerFactory());

// Working with abstractions
// Option 1: Provide the factory instance yourself
services.register(Logger.class, new LoggerFactory());

// Option 2: Setup an alias for the specific implementation
services.alias(Logger.class, ConsoleLogger.class)
        .register(ConsoleLogger.class); // Pass a factory if required

services.get(Worker.class)
        .andThenContinue(Worker::dispatch)
        .orElseContinue(error -> { /* handle error which happened during service creation */ });
```