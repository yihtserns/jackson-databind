package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;

/**
 * @since 3.0
 */
public interface SerializerCreationContext {
    public SerializationConfig config();
    public AnnotationIntrospector annotationIntrospector();
    public BeanDescription beanDescription() throws JsonMappingException;
    public AnnotatedClass classInfo();

    public static class Simple implements SerializerCreationContext {
        private final SerializationConfig _config;
        private final BeanDescription _desc;

        public Simple(SerializationConfig config, BeanDescription desc) {
            _config = config;
            _desc = desc;
        }

        public static Simple from(SerializerProvider prov, JavaType forType) {
            return new Simple(prov.getConfig(),
                    prov.introspectBeanDescription(forType));
        }
        
        @Override
        public SerializationConfig config() { return _config; }

        @Override
        public AnnotationIntrospector annotationIntrospector() { return _config.getAnnotationIntrospector(); }
        
        @Override
        public BeanDescription beanDescription() { return _desc; }

        @Override
        public AnnotatedClass classInfo() { return _desc.getClassInfo(); }
    }
}
