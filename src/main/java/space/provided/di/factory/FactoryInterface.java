package space.provided.di.factory;

import space.provided.rs.result.Result;
import space.provided.di.ServiceLocator;

/**
 * The {@code FactoryInterface} interface defines a mechanism for creating instances of a service based on its identifier.
 * This interface is designed to be implemented by factories responsible for instantiating services within a
 * dependency injection (DI) context.
 *
 * @param <Service> The type parameter representing the service to be created.
 */
@FunctionalInterface
public interface FactoryInterface<Service> {

    /**
     * Creates an instance of the specified service identified by its class, resolving dependencies using the provided
     * {@code ServiceLocator}.
     *
     * @param identifier The class representing the identifier of the service to be registered.
     * @param locator The {@code ServiceLocator} used to locate and resolve dependencies required for service creation.
     * @return A {@code Result} containing either the successfully created service or an error message.
     */
    Result<Service, String> create(Class<?> identifier, ServiceLocator locator);
}
