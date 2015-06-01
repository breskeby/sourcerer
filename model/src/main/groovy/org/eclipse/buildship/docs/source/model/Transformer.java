package org.eclipse.buildship.docs.source.model;

public interface Transformer<OUT, IN> {
    /**
     * Transforms the given object, and returns the transformed value.
     *
     * @param in The object to transform.
     * @return The transformed object.
     */
    OUT transform(IN in);
}