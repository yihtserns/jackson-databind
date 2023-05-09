package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class SpecialTypeHandlerTest extends BaseMapTest {

    private final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new CustomSpecialTypeModule());

    // ===
    // Without @JsonCreator
    // ===

    public static class ClassWithPrimaryConstructor {

        private final String id;
        private final String name;
        private final String email;

        public ClassWithPrimaryConstructor(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        @PrimaryConstructor({"id", "name"})
        public ClassWithPrimaryConstructor(String id, String name) {
            this.id = id;
            this.name = name;
            this.email = "bob@example.com";
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public static ClassWithPrimaryConstructor valueOf(String id) {
            return new ClassWithPrimaryConstructor(id, id, id);
        }
    }

    public void testSerialize_ClassWithPrimaryConstructor_WithoutJsonCreator() throws Exception {
        String json = MAPPER.writeValueAsString(new ClassWithPrimaryConstructor("123", "Bob", "bob@email.com"));

        assertEquals("{\"id\":\"123\",\"name\":\"Bob\",\"email\":\"bob@email.com\"}", json);
    }

    public void testDeserialize_ClassWithPrimaryConstructor_WithoutJsonCreator() throws Exception {
        ClassWithPrimaryConstructor value = MAPPER.readValue("{\"id\":\"123\",\"name\":\"Bob\"}", ClassWithPrimaryConstructor.class);

        assertEquals("123", value.id);
        assertEquals("Bob", value.name);
        assertEquals("bob@example.com", value.email);
    }

    public void testDeserialize_ClassWithPrimaryConstructor_WithoutJsonCreator_UsingImplicitDelegatingCreator() throws Exception {
        ClassWithPrimaryConstructor value = MAPPER.readValue("\"123\"", ClassWithPrimaryConstructor.class);

        assertEquals("123", value.id);
        assertEquals("123", value.name);
        assertEquals("123", value.email);
    }

    // ===
    // With @JsonCreator on constructor
    // ===

    public static class ClassWithPrimaryConstructorWithJsonCreatorConstructor {

        private final String id;
        private final String name;
        private final String email;

        @JsonCreator
        public ClassWithPrimaryConstructorWithJsonCreatorConstructor(@JsonProperty("i") String id,
                                                                     @JsonProperty("n") String name,
                                                                     @JsonProperty("e") String email) {

            this.id = id;
            this.name = name;
            this.email = email;
        }

        @PrimaryConstructor({"id", "name"})
        public ClassWithPrimaryConstructorWithJsonCreatorConstructor(String id, String name) {
            this.id = id;
            this.name = name;
            this.email = "bob@example.com";
        }
    }

    public void testDeserialize_ClassWithPrimaryConstructor_WithJsonCreatorConstructor() throws Exception {
        ClassWithPrimaryConstructorWithJsonCreatorConstructor value = MAPPER.readValue(
                "{\"i\":\"123\",\"n\":\"Bob\",\"e\":\"bob@email.com\"}",
                ClassWithPrimaryConstructorWithJsonCreatorConstructor.class);

        assertEquals("123", value.id);
        assertEquals("Bob", value.name);
        assertEquals("bob@email.com", value.email);
    }

    // ===
    // With @JsonProperty on constructor parameters
    // ===

    public static class ClassWithPrimaryConstructorWithJsonPropertyConstructor {

        private final String id;
        private final String name;
        private final String email;

        public ClassWithPrimaryConstructorWithJsonPropertyConstructor(@JsonProperty("i") String id,
                                                                      @JsonProperty("n") String name,
                                                                      @JsonProperty("e") String email) {

            this.id = id;
            this.name = name;
            this.email = email;
        }

        @PrimaryConstructor({"id", "name"})
        public ClassWithPrimaryConstructorWithJsonPropertyConstructor(String id, String name) {
            this.id = id;
            this.name = name;
            this.email = "bob@example.com";
        }
    }

    public void testDeserialize_ClassWithPrimaryConstructor_WithJsonPropertyConstructor() throws Exception {
        ClassWithPrimaryConstructorWithJsonPropertyConstructor value = MAPPER.readValue(
                "{\"i\":\"123\",\"n\":\"Bob\",\"e\":\"bob@email.com\"}",
                ClassWithPrimaryConstructorWithJsonPropertyConstructor.class);

        assertEquals("123", value.id);
        assertEquals("Bob", value.name);
        assertEquals("bob@email.com", value.email);
    }

    // ===
    // With @JsonCreator on factory method
    // ===

    public static class ClassWithPrimaryConstructorWithJsonCreatorFactoryMethod {

        private final String id;
        private final String name;
        private final String email;

        public ClassWithPrimaryConstructorWithJsonCreatorFactoryMethod(String id, String name, String email) {

            this.id = id;
            this.name = name;
            this.email = email;
        }

        @PrimaryConstructor({"id", "name"})
        public ClassWithPrimaryConstructorWithJsonCreatorFactoryMethod(String id, String name) {
            this.id = id;
            this.name = name;
            this.email = "bob@example.com";
        }

        @JsonCreator
        public static ClassWithPrimaryConstructorWithJsonCreatorFactoryMethod create(@JsonProperty("i") String id,
                                                                                     @JsonProperty("n") String name,
                                                                                     @JsonProperty("e") String email) {

            return new ClassWithPrimaryConstructorWithJsonCreatorFactoryMethod(id, name, email);
        }
    }

    public void testDeserialize_ClassWithPrimaryConstructor_WithJsonCreatorFactoryMethod() throws Exception {
        ClassWithPrimaryConstructorWithJsonCreatorFactoryMethod value = MAPPER.readValue(
                "{\"i\":\"123\",\"n\":\"Bob\",\"e\":\"bob@email.com\"}",
                ClassWithPrimaryConstructorWithJsonCreatorFactoryMethod.class);

        assertEquals("123", value.id);
        assertEquals("Bob", value.name);
        assertEquals("bob@email.com", value.email);
    }

    // ===
    // With a custom AnnotationIntrospector.findCreatorAnnotation implementation.
    // ===

    public static class ClassWithPrimaryConstructorWithCustomCreatorAnnotation {

        private final String id;
        private final String name;
        private final String email;

        @Inject
        public ClassWithPrimaryConstructorWithCustomCreatorAnnotation(@Named("i") String id,
                                                                      @Named("n") String name,
                                                                      @Named("e") String email) {

            this.id = id;
            this.name = name;
            this.email = email;
        }

        @PrimaryConstructor({"id", "name"})
        public ClassWithPrimaryConstructorWithCustomCreatorAnnotation(String id, String name) {
            this.id = id;
            this.name = name;
            this.email = "bob@example.com";
        }
    }

    public void testDeserialize_ClassWithPrimaryConstructor_WithAppendedCustomCreatorAnnotationIntrospector() throws Exception {
        MAPPER.registerModule(new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.appendAnnotationIntrospector(new Jsr330AnnotationIntrospector());
            }
        });

        ClassWithPrimaryConstructorWithCustomCreatorAnnotation value = MAPPER.readValue(
                "{\"i\":\"123\",\"n\":\"Bob\",\"e\":\"bob@email.com\"}",
                ClassWithPrimaryConstructorWithCustomCreatorAnnotation.class);

        assertEquals("123", value.id);
        assertEquals("Bob", value.name);
        assertEquals("bob@email.com", value.email);
    }

    public void testDeserialize_ClassWithPrimaryConstructor_WithInsertedCustomCreatorAnnotationIntrospector() throws Exception {
        MAPPER.registerModule(new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.insertAnnotationIntrospector(new Jsr330AnnotationIntrospector());
            }
        });

        ClassWithPrimaryConstructorWithCustomCreatorAnnotation value = MAPPER.readValue(
                "{\"i\":\"123\",\"n\":\"Bob\",\"e\":\"bob@email.com\"}",
                ClassWithPrimaryConstructorWithCustomCreatorAnnotation.class);

        assertEquals("123", value.id);
        assertEquals("Bob", value.name);
        assertEquals("bob@email.com", value.email);
    }

    // ===
    // Custom Special Type implementation
    // ===

    private static class CustomSpecialTypeModule extends SimpleModule {

        @Override
        public void setupModule(SetupContext context) {
            super.setupModule(context);

            context.addSpecialTypeHandler(new CustomSpecialTypeHandler());
        }
    }

    private static class CustomSpecialTypeHandler implements SpecialTypeHandler {

        @Override
        public boolean supports(JavaType type) {
            for (Constructor<?> constructor : type.getRawClass().getConstructors()) {
                if (constructor.isAnnotationPresent(PrimaryConstructor.class)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public AnnotatedConstructor findSpecialConstructor(AnnotatedClass classDef, AnnotationIntrospector introspector, MapperConfig<?> config, List<String> specialPropertyNames) {
            for (AnnotatedConstructor constructor : classDef.getConstructors()) {
                PrimaryConstructor ann = constructor.getAnnotation(PrimaryConstructor.class);
                if (ann != null) {
                    specialPropertyNames.addAll(Arrays.asList(ann.value()));

                    return constructor;
                }
            }

            return null;
        }

        @Override
        public boolean canUseFieldAsMutator() {
            return false;
        }

        @Override
        public AccessorNamingStrategy getAccessorNamingStrategy(MapperConfig<?> config, AnnotatedClass classDef) {
            return SpecialTypeHandler.OutOfTheBox.POJO.getAccessorNamingStrategy(config, classDef);
        }
    }

    @Target(ElementType.CONSTRUCTOR)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PrimaryConstructor {

        String[] value();
    }

    // ===
    // Custom AnnotationIntrospector using JSR-330's dependency injection annotations as JSON config annotations
    // ===
    private static class Jsr330AnnotationIntrospector extends NopAnnotationIntrospector {

        @Override
        public String findImplicitPropertyName(AnnotatedMember member) {
            Named named = member.getAnnotation(Named.class);

            return named == null ? null : named.value();
        }

        @Override
        public JsonCreator.Mode findCreatorAnnotation(MapperConfig<?> config, Annotated ann) {
            return ann.hasAnnotation(Inject.class)
                    ? JsonCreator.Mode.PROPERTIES
                    : null;
        }
    }
}