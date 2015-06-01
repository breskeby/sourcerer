/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.buildship.docs.model;

import groovy.lang.Closure;
import org.eclipse.buildship.docs.source.model.Action;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SimpleClassMetaDataRepository<T extends Attachable<T>> implements ClassMetaDataRepository<T> {
    private final Map<String, T> classes = new HashMap<String, T>();

    @SuppressWarnings("unchecked")
    public void load(File repoFile) {
        try {
            FileInputStream inputStream = new FileInputStream(repoFile);
            try {
                ObjectInputStream objInputStream = new ObjectInputStream(new BufferedInputStream(inputStream));
                classes.clear();
                classes.putAll((Map<String, T>) objInputStream.readObject());
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not load meta-data from %s.", repoFile), e);
        }
    }

    public void store(File repoFile) {
        try {
            FileOutputStream outputStream = new FileOutputStream(repoFile);
            try {
                ObjectOutputStream objOutputStream = new ObjectOutputStream(new BufferedOutputStream(outputStream));
                objOutputStream.writeObject(classes);
                objOutputStream.close();
            } finally {
                outputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not write meta-data to %s.", repoFile), e);
        }
    }

    public T get(String fullyQualifiedClassName) {
        T t = find(fullyQualifiedClassName);
        if (t == null) {
            throw new RuntimeException(String.format("No meta-data is available for class '%s'.", fullyQualifiedClassName));
        }
        return t;
    }

    public T find(String fullyQualifiedClassName) {
        T t = classes.get(fullyQualifiedClassName);
        if (t != null) {
            t.attach(this);
        }
        return t;
    }

    public void put(String fullyQualifiedClassName, T metaData) {
        classes.put(fullyQualifiedClassName, metaData);
    }

    public void each(Closure cl) {
        for (Map.Entry<String, T> entry : classes.entrySet()) {
            cl.call(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    public void each(Action<? super T> action) {
        for (T t : classes.values()) {
            action.execute(t);
        }
    }
}
