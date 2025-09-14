package com.fastcode.bpmnModeler.application.decisiontable.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionTableDefinitionRepresentation {

    protected String id;
    protected String modelVersion;
    protected String name;
    protected String key;
    protected String description;
    protected String hitIndicator;
    protected String collectOperator;
    protected String completenessIndicator;
    protected List<DecisionTableExpressionRepresentation> inputExpressions;
    protected List<DecisionTableExpressionRepresentation> outputExpressions;
    protected List<Map<String, Object>> rules;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
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

    public String getHitIndicator() {
        return hitIndicator;
    }

    public void setHitIndicator(String hitIndicator) {
        this.hitIndicator = hitIndicator;
    }

    public String getCollectOperator() {
        return collectOperator;
    }

    public void setCollectOperator(String collectOperator) {
        this.collectOperator = collectOperator;
    }

    public String getCompletenessIndicator() {
        return completenessIndicator;
    }

    public void setCompletenessIndicator(String completenessIndicator) {
        this.completenessIndicator = completenessIndicator;
    }

    public List<DecisionTableExpressionRepresentation> getInputExpressions() {
        return inputExpressions;
    }

    public void setInputExpressions(List<DecisionTableExpressionRepresentation> inputExpressions) {
        this.inputExpressions = inputExpressions;
    }


    public List<DecisionTableExpressionRepresentation> getOutputExpressions() {
        return outputExpressions;
    }

    public void setOutputExpressions(List<DecisionTableExpressionRepresentation> outputExpressions) {
        this.outputExpressions = outputExpressions;
    }

    public List<Map<String, Object>> getRules() {
        return rules;
    }

    public void setRules(List<Map<String, Object>> rules) {
        this.rules = rules;
    }
}
