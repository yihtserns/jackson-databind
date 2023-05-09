package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.jdk14.JDK14Util;

import java.util.ArrayList;
import java.util.List;

public interface SpecialTypeHandler {

    boolean supports(JavaType type);

    AnnotatedConstructor findSpecialConstructor(AnnotatedClass classDef,
                                                AnnotationIntrospector introspector,
                                                MapperConfig<?> config,
                                                List<String> specialPropertyNames);

    boolean canUseFieldAsMutator();

    AccessorNamingStrategy getAccessorNamingStrategy(MapperConfig<?> config, AnnotatedClass classDef);

    enum OutOfTheBox implements SpecialTypeHandler {

        POJO() {
            @Override
            public boolean supports(JavaType type) {
                return true;
            }

            @Override
            public AnnotatedConstructor findSpecialConstructor(AnnotatedClass classDef, AnnotationIntrospector introspector, MapperConfig<?> config, List<String> specialPropertyNames) {
                return null;
            }

            @Override
            public boolean canUseFieldAsMutator() {
                return true;
            }

            @Override
            public AccessorNamingStrategy getAccessorNamingStrategy(MapperConfig<?> config, AnnotatedClass classDef) {
                return config.getAccessorNaming().forPOJO(config, classDef);
            }
        },
        RECORDS() {
            @Override
            public boolean supports(JavaType type) {
                return type.isRecordType();
            }

            @Override
            public AnnotatedConstructor findSpecialConstructor(AnnotatedClass classDef, AnnotationIntrospector introspector, MapperConfig<?> config, List<String> specialPropertyNames) {
                return JDK14Util.findRecordConstructor(classDef, introspector, config, specialPropertyNames);
            }

            @Override
            public boolean canUseFieldAsMutator() {
                return false;
            }

            @Override
            public AccessorNamingStrategy getAccessorNamingStrategy(MapperConfig<?> config, AnnotatedClass classDef) {
                return config.getAccessorNaming().forRecord(config, classDef);
            }
        }
    }

    class Config {

        private List<SpecialTypeHandler> handlers = new ArrayList<>();

        public SpecialTypeHandler findHandler(JavaType type) {
            for (SpecialTypeHandler resolver : handlers) {
                if (resolver.supports(type)) {
                    return resolver;
                }
            }

            return OutOfTheBox.POJO;
        }

        public Config add(SpecialTypeHandler handler) {
            handlers.add(handler);

            return this;
        }
    }
}
