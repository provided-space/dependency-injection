package space.provided.di;

import space.provided.rs.result.Result;
import space.provided.di.factory.AutowireFactory;
import space.provided.di.factory.FactoryInterface;

import java.util.HashMap;
import java.util.Map;

public final class ServiceManager implements ServiceLocator {

    private final Map<Class<?>, FactoryInterface<?>> factories;
    private final Map<Class<?>, Object> services;
    private final Map<Class<?>, Class<?>> aliases;
    private final FactoryInterface<?> defaultFactory;

    public ServiceManager() {
        factories = new HashMap<>();
        services = new HashMap<>();
        aliases = new HashMap<>();
        defaultFactory = new AutowireFactory<>();

        services.put(ServiceManager.class, this);
        aliases.put(ServiceLocator.class, ServiceManager.class);
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
    public <Service> ServiceManager register(Class<Service> identifier, FactoryInterface<? extends Service> factory) {
        factories.put(identifier, factory);
        return this;
    }

    /**
     * Register an alias by an identifier which will then be mapped to the replacement.
     *
     * @param identifier The super class of the service.
     * @param replacement A child class that inherits from the super class, which will then contain a more specific implementation.
     * @return Instance of self
     * @param <Service> The type parameter representing the service to be registered.
     */
    public <Service> ServiceManager alias(Class<Service> identifier, Class<? extends Service> replacement) {
        aliases.put(identifier, replacement);
        return this;
    }

    @Override
    public <Service> Result<? extends Service, String> get(Class<Service> identifier) {
        if (services.containsKey(identifier)) {
            return Result.ok((Service) services.get(identifier));
        }
        return resolve(identifier, true).andThenContinue(service -> services.put(identifier, service));
    }

    @Override
    public <Service> Result<? extends Service, String> build(Class<Service> identifier) {
        return resolve(identifier, false);
    }

    private <Service> Result<? extends Service, String> resolve(Class<Service> identifier, boolean allowExisting) {
        if (factories.containsKey(identifier)) {
            final FactoryInterface<Service> factory = (FactoryInterface<Service>) factories.get(identifier);
            return factory.create(identifier, this);
        }
        if (aliases.containsKey(identifier)) {
            if (allowExisting) {
                return (Result<Service, String>) get(aliases.get(identifier));
            }
            return (Result<Service, String>) build(aliases.get(identifier));
        }
        return Result.error(String.format("No factory found for %1$s.", identifier.getName()));
    }
}
