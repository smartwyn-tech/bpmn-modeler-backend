package com.fastcode.bpmnModeler.restcontrollers;

import com.fastcode.bpmnModeler.application.model.IModelAppService;
import com.fastcode.bpmnModeler.application.modelrelation.IModelRelationAppService;
import com.fastcode.bpmnModeler.application.modelrelation.dto.ModelInformation;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelRelationController {

	@Qualifier("modelRelationAppService")
	@NonNull protected final IModelRelationAppService _modelRelationAppService;


	@RequestMapping(value = "/{modelId}/parent-relations", method = RequestMethod.GET, produces = "application/json")
	public List<ModelInformation> getModelRelations(@PathVariable String modelId) {
		return _modelRelationAppService.findParentModels(modelId);
	}

}


