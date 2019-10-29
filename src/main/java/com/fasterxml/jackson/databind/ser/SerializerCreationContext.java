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
        private final SerializerProvider _ctxt;
        private final JavaType _type;

        private BeanDescription _desc;

        public Simple(SerializerProvider ctxt, JavaType type) {
            _ctxt = ctxt;
            _type = type;
        }

        public static Simple from(SerializerProvider prov, JavaType forType) {
            return new Simple(prov, forType);
        }
        
        @Override
        public SerializationConfig config() { return _ctxt.getConfig(); }

        @Override
        public AnnotationIntrospector annotationIntrospector() { return _ctxt.getAnnotationIntrospector(); }
        
        @Override
        public BeanDescription beanDescription() {
            if (_desc == null) {
                _desc = _ctxt.introspectBeanDescription(_type);
            }
            return _desc;
        }

        @Override
        public AnnotatedClass classInfo() {
            return beanDescription().getClassInfo();
        }
    }
}
