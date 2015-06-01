package org.eclipse.buildship.docs.export.dto;

import java.util.ArrayList;
import java.util.List;

public class TaskType {

    private String description;
    private String className;
    private String name;

    private List<TaskProperty> taskProperties = new ArrayList<TaskProperty>();;

    public TaskType(String className, String name, String description) {
        this.name = name;
        this.className = className;
        this.description = description;
    }


    /**
     +     * Adds a {@link TaskProperty} to the task type.
     +     * @param property {@link TaskProperty}
     +     */
     public void addTaskProperty(TaskProperty property){
         taskProperties.add(property);
     }


    public String getDescription() {
        return description;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public List<TaskProperty> getTaskProperties() {
        return taskProperties;
    }

    public void setTaskProperties(List<TaskProperty> taskProperties) {
        this.taskProperties = taskProperties;
    }
}
