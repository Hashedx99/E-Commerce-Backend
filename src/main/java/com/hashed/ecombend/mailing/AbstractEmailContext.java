package com.hashed.ecombend.mailing;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all email contexts.
 * Subclasses set the template location, subject, recipient, and template variables.
 * The context map is passed to the Thymeleaf template engine for rendering.
 */
@Data
public abstract class AbstractEmailContext {

    private String from;
    private String to;
    private String subject;
    private String templateLocation;
    private Map<String, Object> context;

    public AbstractEmailContext() {
        this.context = new HashMap<>();
    }

    /**
     * Template hook subclasses call this from their own init() to populate fields.
     */
    public abstract <T> void init(T context);

    /**
     * Adds a variable to the Thymeleaf template context.
     *
     * @param key   Template variable name
     * @param value The value to inject into the template
     */
    public Object put(String key, Object value) {
        return key == null ? null : this.context.put(key.intern(), value);
    }
}
