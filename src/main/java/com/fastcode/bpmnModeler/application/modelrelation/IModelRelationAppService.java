package com.fastcode.bpmnModeler.application.modelrelation;


import com.fastcode.bpmnModeler.application.modelrelation.dto.ModelInformation;

import java.util.List;

public interface IModelRelationAppService {

    List<ModelInformation> findParentModels(String modelId);
}

