package space.provided.di;

import space.provided.rs.result.Result;

@FunctionalInterface
public interface ServiceLocator {

    <Service> Result<Service, String> get(Class<Service> identifier);
}
