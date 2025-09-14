package com.fastcode.bpmnModeler.application.decisiontable.dto;

import com.fastcode.bpmnModeler.domain.model.Model;

import java.util.Date;

public class DecisionTableRepresentation {
    protected String id;
    protected String name;
    protected String key;
    protected String description;
    protected Integer version;
    protected Date lastUpdated;
    protected DecisionTableDefinitionRepresentation decisionTableDefinition;

    public DecisionTableRepresentation(Model model) {
        this.id = model.getId();
        this.name = model.getName();
        this.key = model.getKey();
        this.description = model.getDescription();
        this.version = model.getVersion();
        this.lastUpdated = model.getLastupdated();
    }

    public DecisionTableRepresentation() {
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public DecisionTableDefinitionRepresentation getDecisionTableDefinition() {
        return decisionTableDefinition;
    }

    public void setDecisionTableDefinition(DecisionTableDefinitionRepresentation decisionTableDefinition) {
        this.decisionTableDefinition = decisionTableDefinition;
    }
}
