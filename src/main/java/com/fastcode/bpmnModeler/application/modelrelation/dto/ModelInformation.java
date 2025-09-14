package com.fastcode.bpmnModeler.application.modelrelation.dto;

public class ModelInformation {

    private String id;
    private String name;
    private Integer type;

    public ModelInformation() {

    }

    public ModelInformation(String id, String name, Integer type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}
