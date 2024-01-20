package space.provided.di;

import space.provided.rs.result.Result;
import space.provided.di.factory.AutowireFactory;
import space.provided.di.factory.FactoryInterface;

import java.util.HashMap;
import java.util.Map;

public final class ServiceManager implements ServiceLocator {

    private final Map<Class<?>, FactoryInterface<?>> factories;
    private final Map<Class<?>, Object> services;
    private final FactoryInterface<?> defaultFactory;

    public ServiceManager() {
        factories = new HashMap<>();
        services = new HashMap<>();
        defaultFactory = new AutowireFactory<>();

        services.put(ServiceManager.class, this);
    }

    /**
     * Register a service by an identifier using the default factory ({@link AutowireFactory}).
     *
     * @param identifier The class representing the identifier of the service to be registered.
     * @return Instance of self
     * @param <Service> The type parameter representing the service to be registered.
     */
    public <Service> ServiceManager register(Class<Service> identifier) {
        factories.put(identifier, defaultFactory);
        return this;
    }

    /**
     * Register a service by an identifier using the instance of the specified factory.
     *
     * @param identifier The class representing the identifier of the service to be registered.
     * @param factory The instance of the factory which has to create the service. Its generic type has to be assignable to {@code Service}
     * @return Instance of self
     * @param <Service> The type parameter representing the service to be registered.
     */
    public <Service> ServiceManager register(Class<Service> identifier, FactoryInterface<Service> factory) {
        factories.put(identifier, factory);
        return this;
    }

    @Override
    public <Service> Result<Service, String> get(Class<Service> identifier) {
        if (services.containsKey(identifier)) {
            return Result.ok((Service) services.get(identifier));
        }
        return build(identifier).andThenContinue(service -> services.put(identifier, service));
    }

    private <Service> Result<Service, String> build(Class<Service> identifier) {
        if (!factories.containsKey(identifier)) {
            return Result.error(String.format("No factory found for %1$s.", identifier.getName()));
        }
        return (Result<Service, String>) factories.get(identifier).create(identifier, this);
    }
}
