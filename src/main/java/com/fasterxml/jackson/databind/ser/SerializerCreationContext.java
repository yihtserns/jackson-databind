package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializationConfig;

/**
 * @since 3.0
 */
public interface SerializerCreationContext {
    public SerializationConfig getConfif();

    public BeanDescription getBeanDescription() throws JsonMappingException;

    public static class Simple implements SerializerCreationContext {
        private final SerializationConfig _config;
        private final BeanDescription _desc;

        public Simple(SerializationConfig config, BeanDescription desc) {
            _config = config;
            _desc = desc;
        }

        @Override
        public SerializationConfig getConfif() { return _config; }

        @Override
        public BeanDescription getBeanDescription() { return _desc; }
    }
}
