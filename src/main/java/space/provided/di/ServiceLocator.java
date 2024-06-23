package space.provided.di;

import space.provided.rs.result.Result;

/**
 * The {@code ServiceLocator} interface defines a mechanism for locating and retrieving instances of services within
 * a dependency injection (DI) context.
 */
@FunctionalInterface
public interface ServiceLocator {

    /**
     * Retrieves an instance of the specified service identified by its class.
     *
     * @param identifier The class representing the identifier of the service to be located.
     * @return A {@code Result} containing either the successfully located service or an error message.
     * @param <Service> The type parameter representing the service to be located.
     */
    <Service> Result<? extends Service, String> get(Class<Service> identifier);
}
