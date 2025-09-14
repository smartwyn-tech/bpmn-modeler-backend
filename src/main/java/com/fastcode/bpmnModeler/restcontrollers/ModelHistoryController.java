package com.fastcode.bpmnModeler.restcontrollers;

import com.fastcode.bpmnModeler.application.model.dto.ResultListDataRepresentation;
import com.fastcode.bpmnModeler.application.modelhistory.IModelHistoryAppService;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelHistoryController {

	@Qualifier("modelHistoryAppService")
	@NonNull protected final IModelHistoryAppService _modelHistoryAppService;

	@NonNull protected final LoggingHelper logHelper;

	@NonNull protected final Environment env;

	@RequestMapping(value = "{modelId}/history", method = RequestMethod.GET, produces = "application/json")
	public ResultListDataRepresentation getModelHistoryCollection(@PathVariable String modelId, @RequestParam(value = "includeLatestVersion", required = false) Boolean includeLatestVersion) {
		return _modelHistoryAppService.getModelHistoryCollection(modelId, includeLatestVersion);
	}

}


