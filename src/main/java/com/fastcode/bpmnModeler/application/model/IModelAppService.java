package com.fastcode.bpmnModeler.application.model;

import com.fastcode.bpmnModeler.application.model.dto.ModelKeyRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ModelRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ResultListDataRepresentation;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javassist.NotFoundException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IModelAppService {
	
	ModelRepresentation getModelRepresentation(String modelId) throws NotFoundException;

  	ModelRepresentation saveModel(String modelId, MultiValueMap<String, String> values);

	Model saveModel(Model modelObject, String editorJson, byte[] imageBytes, boolean newVersion, String newVersionComment);

	ResultListDataRepresentation getModels(String filter, String sort, Integer modelType, HttpServletRequest request);

	ResultListDataRepresentation getModelsToIncludeInAppDefinition();

	ModelRepresentation createModel(ModelRepresentation modelRepresentation);

	Model createModel(ModelRepresentation modelRepresentation, String editorJson);

	ObjectNode getModelEditorJson(String modelId);

	void deleteModel(String modelId);

	Model getModel(String modelId);

	ModelRepresentation updateModel(String modelId, ModelRepresentation updatedModel);

	ModelRepresentation duplicateModel(String modelId, ModelRepresentation modelRepresentation);

	ModelRepresentation importProcessModel(HttpServletRequest request, MultipartFile file);

	ModelKeyRepresentation validateModelKey(Model model, Integer modeltype, String decisionKey);

	void getProcessModelBpmn20Xml(HttpServletResponse response, String processModelId);
}

