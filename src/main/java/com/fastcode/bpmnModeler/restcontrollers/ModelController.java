package com.fastcode.bpmnModeler.restcontrollers;

import com.fastcode.bpmnModeler.application.model.IModelAppService;
import com.fastcode.bpmnModeler.application.model.dto.ModelRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ResultListDataRepresentation;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javassist.NotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelController {

    @NonNull
    protected final LoggingHelper logHelper;

    @Qualifier("modelAppService")
    @NonNull
    protected final IModelAppService _modelAppService;

    @RequestMapping(value = "/{modelId}", method = RequestMethod.GET, produces = "application/json")
    public ModelRepresentation getModel(@PathVariable String modelId) throws NotFoundException {
        return _modelAppService.getModelRepresentation(modelId);
    }

    /**
     * POST /{modelId}/editor/json -> save the JSON model
     */
    @RequestMapping(value = "/{modelId}/editor/json", method = RequestMethod.POST)
    public ModelRepresentation saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {
        return _modelAppService.saveModel(modelId, values);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    public ResultListDataRepresentation getModels(@RequestParam(required = false) String filter, @RequestParam(required = false) String sort, @RequestParam(required = false) Integer modelType,
                                                  HttpServletRequest request) {

        return _modelAppService.getModels(filter, sort, modelType, request);
    }

    @RequestMapping(value = "/models-for-app-definition", method = RequestMethod.GET, produces = "application/json")
    public ResultListDataRepresentation getModelsToIncludeInAppDefinition() {
        return _modelAppService.getModelsToIncludeInAppDefinition();
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    public ModelRepresentation createModel(@RequestBody ModelRepresentation modelRepresentation) {
        return _modelAppService.createModel(modelRepresentation);
    }

    /**
     * GET /{modelId}/editor/json -> get the JSON model
     */
    @RequestMapping(value = "/{modelId}/editor/json", method = RequestMethod.GET, produces = "application/json")
    public ObjectNode getModelJSON(@PathVariable String modelId) {
        return _modelAppService.getModelEditorJson(modelId);
    }

    /**
     * DELETE /{modelId} -> delete process model or, as a non-owner, remove the share info link for that user specifically
     */
    @ResponseStatus(value = HttpStatus.OK)
    @RequestMapping(value = "/{modelId}", method = RequestMethod.DELETE)
    public void deleteModel(@PathVariable String modelId) {
        _modelAppService.deleteModel(modelId);
    }

    /**
     * PUT /{modelId} -> update process model properties
     */
    @RequestMapping(value = "/{modelId}", method = RequestMethod.PUT)
    public ModelRepresentation updateModel(@PathVariable String modelId, @RequestBody ModelRepresentation updatedModel) {
       return _modelAppService.updateModel(modelId, updatedModel);
    }

    @RequestMapping(value = "/{modelId}/clone", method = RequestMethod.POST, produces = "application/json")
    public ModelRepresentation duplicateModel(@PathVariable String modelId, @RequestBody ModelRepresentation modelRepresentation) {
        return _modelAppService.duplicateModel(modelId, modelRepresentation);
    }

    @RequestMapping(value = "/import-process-model", method = RequestMethod.POST, produces = "application/json")
    public ModelRepresentation importProcessModel(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        return _modelAppService.importProcessModel(request, file);
    }

    /**
     * GET /{modelId}/thumbnail -> Get process model thumbnail
     */
    @RequestMapping(value = "/{modelId}/thumbnail", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getModelThumbnail(@PathVariable String modelId) {
        Model model = _modelAppService.getModel(modelId);
        return model.getThumbnail();
    }

    @RequestMapping(value = "/{modelId}/bpmn20", method = RequestMethod.GET)
    public void getProcessModelBpmn20Xml(HttpServletResponse response, @PathVariable String modelId) throws IOException {
        _modelAppService.getProcessModelBpmn20Xml(response, modelId);
    }

}
