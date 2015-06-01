package org.eclipse.buildship.docs.export.dto;

public class TaskProperty {

    private String name;
    private String description;
    private String propertyTypeName; // todo fix this

    public TaskProperty(String name, String description, String propertyTypeName){
        this.name = name;
        this.description = description;
        this.propertyTypeName = propertyTypeName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPropertyTypeName() {
        return propertyTypeName;
    }
}
