package com.fastcode.bpmnModeler.application.model.dto;

import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.domain.modelhistory.ModelHistory;

import java.util.Date;

public class ModelRepresentation{
    protected String id;
    protected String name;
    protected String key;
    protected String description;
    protected Date lastUpdated;
    protected boolean latestVersion;
    protected int version;
    protected String comment;
    protected Integer modelType;
    protected String tenantId;

    public ModelRepresentation(Model model) {
        initialize(model);
    }

    public ModelRepresentation(ModelHistory model) {
        initialize(model);
    }

    public ModelRepresentation() {

    }

    public void initialize(Model model) {
        this.id = model.getId();
        this.name = model.getName();
        this.key = model.getKey();
        this.description = model.getDescription();
        this.lastUpdated = model.getLastupdated();
        this.version = model.getVersion();
        this.comment = model.getComment();
        this.modelType = model.getModeltype();
        this.tenantId = model.getTenantid();
        this.setLatestVersion(true);
    }

    public void initialize(ModelHistory model) {
        this.id = model.getId();
        this.name = model.getName();
        this.key = model.getKey();
        this.description = model.getDescription();
        this.lastUpdated = model.getLastupdated();
        this.version = model.getVersion();
        this.comment = model.getComment();
        this.modelType = model.getModeltype();
        this.tenantId = model.getTenantid();
        this.setLatestVersion(false);
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

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setLatestVersion(boolean latestVersion) {
        this.latestVersion = latestVersion;
    }

    public boolean isLatestVersion() {
        return latestVersion;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public Integer getModelType() {
        return modelType;
    }

    public void setModelType(Integer modelType) {
        this.modelType = modelType;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Model toModel() {
        Model model = new Model();
        model.setName(name);
        model.setDescription(description);
        return model;
    }

    /**
     * Update all editable properties of the given {@link Model} based on the values in this instance.
     */
    public void updateModel(Model model) {
        model.setDescription(this.description);
        model.setName(this.name);
        model.setKey(this.key);
    }
}
