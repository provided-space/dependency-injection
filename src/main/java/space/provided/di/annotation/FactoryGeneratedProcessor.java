package space.provided.di.annotation;

import com.google.auto.service.AutoService;
import space.provided.di.ServiceLocator;
import space.provided.di.factory.FactoryInterface;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public final class FactoryGeneratedProcessor extends AbstractProcessor {

    private static final Predicate<Element> IS_EXECUTABLE = element -> element instanceof ExecutableElement;
    private static final Predicate<Element> IS_CONSTRUCTOR = element -> element.getSimpleName().contentEquals("<init>");
    private static final Predicate<Element> IS_PUBLIC = element -> element.getModifiers().contains(Modifier.PUBLIC);

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {{
            add(FactoryGenerated.class.getCanonicalName());
        }};
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(FactoryGenerated.class)) {
            if (!(element instanceof TypeElement)) {
                continue;
            }

            final TypeElement type = (TypeElement) element;
            if (type.getModifiers().contains(Modifier.ABSTRACT)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Class %1$s is abstract and therefore cannot be instantiated.", type.getQualifiedName()));
                continue;
            }

            final List<ExecutableElement> constructors = getConstructors(type);
            if (constructors.size() != 1) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Class %1$s needs exactly one public constructor.", type.getQualifiedName()));
                continue;
            }

            try {
                createDefaultFactory(type, constructors.get(0));
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }

        return true;
    }

    private void createDefaultFactory(TypeElement type, ExecutableElement constructor) throws IOException {
        final StringBuilder errorHandling = new StringBuilder();
        final List<String> parameters = new ArrayList<>();
        constructor.getParameters().forEach(parameter -> {
            errorHandling.append(String.format("\t\tfinal Result<? extends %1$s, String> %2$s = locator.get(%1$s.class);\n", parameter.asType(), parameter.getSimpleName()));
            errorHandling.append(String.format("\t\tif (%1$s.isError()) return Result.error(\"Could not resolve parameter \\\"%1$s\\\" for %2$s due to: \" + %1$s.unwrapError());\n", parameter.getSimpleName(), type.getQualifiedName()));
            parameters.add(parameter.getSimpleName() + ".unwrap()");
        });

        final JavaFileObject file = processingEnv.getFiler().createSourceFile(type.getQualifiedName() + "Factory");
        final PrintWriter writer = new PrintWriter(file.openWriter());
        writer.println("package " + type.getQualifiedName().subSequence(0, type.getQualifiedName().toString().lastIndexOf(".")) + ";");
        writer.println();
        writer.println(String.format("import %1$s;", ServiceLocator.class.getCanonicalName()));
        writer.println(String.format("import %1$s;", FactoryInterface.class.getCanonicalName()));
        writer.println("import space.provided.rs.result.Result;");
        writer.println();
        writer.println(String.format("public final class %1$s implements FactoryInterface<%2$s> {", type.getSimpleName() + "Factory",type.getQualifiedName()));
        writer.println("\t@Override");
        writer.println(String.format("\tpublic Result<%1$s, String> create(Class<? extends %1$s> identifier, ServiceLocator locator) {", type.getQualifiedName()));
        writer.println(errorHandling);
        writer.println(String.format("\t\treturn Result.ok(new %1$s(%2$s));", type.getQualifiedName(), String.join(", ", parameters)));
        writer.println("\t}");
        writer.println("}");

        writer.close();
    }

    private List<ExecutableElement> getConstructors(TypeElement type) {
        return type.getEnclosedElements().stream()
                .filter(IS_EXECUTABLE.and(IS_CONSTRUCTOR).and(IS_PUBLIC))
                .map(element -> (ExecutableElement) element)
                .collect(Collectors.toList());
    }
}
