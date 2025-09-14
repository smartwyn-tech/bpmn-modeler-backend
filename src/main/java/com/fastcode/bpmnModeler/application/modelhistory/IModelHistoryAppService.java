package com.fastcode.bpmnModeler.application.modelhistory;


import com.fastcode.bpmnModeler.application.model.dto.ResultListDataRepresentation;

public interface IModelHistoryAppService {
    ResultListDataRepresentation getModelHistoryCollection(String modelId, Boolean includeLatestVersion);
}

