package space.provided.di.factory;

import space.provided.rs.result.Result;
import space.provided.di.ServiceLocator;

@FunctionalInterface
public interface FactoryInterface<Service> {

    Result<Service, String> create(Class<?> identifier, ServiceLocator locator);
}
