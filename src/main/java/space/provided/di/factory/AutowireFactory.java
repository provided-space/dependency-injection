package space.provided.di.factory;

import space.provided.rs.option.Option;
import space.provided.rs.result.Result;
import space.provided.di.ServiceLocator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class AutowireFactory<Service> implements FactoryInterface<Service> {

    @Override
    public Result<Service, String> create(Class<?> identifier, ServiceLocator locator) {
        final Option<Constructor<Service>> constructorOption = findConstructor(identifier);
        if (constructorOption.isNone()) {
            return Result.error(String.format("No matching constructor was found for %1$s.", identifier.getName()));
        }

        final Constructor<Service> constructor = constructorOption.unwrap();
        final Result<Object[], List<String>> argumentsResult = resolveParameters(constructor.getParameterTypes(), locator);
        if (argumentsResult.isError()) {
            return Result.error(String.join(" ", argumentsResult.unwrapError()));
        }

        return construct(constructor, argumentsResult.unwrap());
    }

    private Option<Constructor<Service>> findConstructor(Class<?> identifier) {
        final Constructor<Service>[] constructors = (Constructor<Service>[]) identifier.getConstructors();
        if (constructors.length != 1) {
            return Option.none();
        }
        return Option.some(constructors[0]);
    }

    private Result<Service, String> construct(Constructor<Service> constructor, Object[] arguments) {
        try {
            return Result.ok(constructor.newInstance(arguments));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return Result.error(e.getMessage());
        }
    }

    private Result<Object[], List<String>> resolveParameters(Class<?>[] parameters, ServiceLocator container) {
        final Object[] arguments = new Object[parameters.length];
        final List<String> errors = new ArrayList<>();

        for (int i = 0; i < arguments.length; i++) {
            final int index = i;
            container.get(parameters[index])
                    .andThenContinue(argument -> arguments[index] = argument)
                    .orElseContinue(errors::add);
        }

        if (!errors.isEmpty()) {
            return Result.error(errors);
        }
        return Result.ok(arguments);
    }
}
