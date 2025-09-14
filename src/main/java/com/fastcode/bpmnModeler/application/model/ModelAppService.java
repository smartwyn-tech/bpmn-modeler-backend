package com.fastcode.bpmnModeler.application.model;

import com.fastcode.bpmnModeler.application.decisiontable.dto.DecisionTableDefinitionRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ModelKeyRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ModelRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ResultListDataRepresentation;
import com.fastcode.bpmnModeler.application.modelimage.ModelImageService;
import com.fastcode.bpmnModeler.commons.bpmn.BpmnAutoLayout;
import com.fastcode.bpmnModeler.commons.error.*;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import com.fastcode.bpmnModeler.commons.utils.XmlUtil;
import com.fastcode.bpmnModeler.domain.model.IModelRepository;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.domain.model.ModelSort;
import com.fastcode.bpmnModeler.domain.modelhistory.IModelHistoryRepository;
import com.fastcode.bpmnModeler.domain.modelhistory.ModelHistory;
import com.fastcode.bpmnModeler.domain.modelrelation.IModelRelationRepository;
import com.fastcode.bpmnModeler.domain.modelrelation.ModelRelation;
import com.fastcode.bpmnModeler.domain.modelrelation.ModelRelationTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.editor.language.json.converter.util.CollectionUtils;
import org.flowable.editor.language.json.converter.util.JsonConverterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;


@Service("modelAppService")
@RequiredArgsConstructor
public class ModelAppService implements IModelAppService {

    protected static final String PROCESS_NOT_FOUND_MESSAGE_KEY = "PROCESS.ERROR.NOT-FOUND";
    protected static final int MIN_FILTER_LENGTH = 1;
    private static final String RESOLVE_ACTION_OVERWRITE = "overwrite";
    private static final String RESOLVE_ACTION_SAVE_AS = "saveAs";
    private static final String RESOLVE_ACTION_NEW_VERSION = "newVersion";
    @Qualifier("modelRepository")
    @NonNull
    protected final IModelRepository _modelRepository;

    @Qualifier("IModelMapperImpl")
    @NonNull
    protected final IModelMapper mapper;

    @NonNull
    protected final LoggingHelper logHelper;

    @Qualifier("modelRelationRepository")
    @NonNull
    protected final IModelRelationRepository _modelRelationRepository;

    @Qualifier("modelHistoryRepository")
    @NonNull
    protected final IModelHistoryRepository _modelHistoryRepository;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ModelImageService modelImageService;

    protected BpmnXMLConverter bpmnXmlConverter = new BpmnXMLConverter();

    protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();


    @Override
    public ModelRepresentation getModelRepresentation(String modelId) {
        Model model = getModel(modelId);
        return new ModelRepresentation(model);
    }

    public Model getModel(String modelId) {
        Optional<Model> optionalModel = _modelRepository.findById(modelId);

        Model model = optionalModel.orElseThrow(() -> {
            NotFoundException modelNotFound = new NotFoundException("No model found with the given id: " + modelId);
            modelNotFound.setMessageKey(PROCESS_NOT_FOUND_MESSAGE_KEY);
            return modelNotFound;
        });

        return model;
    }

    @Override
    public ModelRepresentation updateModel(String modelId, ModelRepresentation updatedModel) {
        // Get model, write-permission required if not a favorite-update
        Model model = getModel(modelId);
        ModelKeyRepresentation modelKeyInfo = validateModelKey(model, model.getModeltype(), updatedModel.getKey());
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Model with provided key already exists " + updatedModel.getKey());
        }
        try {
            updatedModel.updateModel(model);

            if (model.getModeltype() != null) {
                ObjectNode modelNode = (ObjectNode) objectMapper.readTree(model.getModeleditorjson());
                modelNode.put("name", model.getName());
                modelNode.put("key", model.getKey());

                if (Model.MODEL_TYPE_BPMN == model.getModeltype()) {
                    ObjectNode propertiesNode = (ObjectNode) modelNode.get("properties");
                    propertiesNode.put("process_id", model.getKey());
                    propertiesNode.put("name", model.getName());
                    if (StringUtils.isNotEmpty(model.getDescription())) {
                        propertiesNode.put("documentation", model.getDescription());
                    }
                    modelNode.set("properties", propertiesNode);
                }
                model.setModeleditorjson(modelNode.toString());
            }
            _modelRepository.save(model);
            ModelRepresentation result = new ModelRepresentation(model);
            return result;

        } catch (Exception e) {
            throw new BadRequestException("Model cannot be updated: " + modelId);
        }
    }

    @Override
    public ModelRepresentation duplicateModel(String modelId, ModelRepresentation modelRepresentation) {
        String json = null;
        Model model = null;
        if (modelId != null) {
            model = getModel(modelId);
            json = model.getModeleditorjson();
        }

        if (model == null) {
            throw new InternalServerErrorException("Error duplicating model : Unknown original model");
        }

        modelRepresentation.setKey(modelRepresentation.getKey().replaceAll(" ", ""));
        checkForDuplicateKey(modelRepresentation);

        if (modelRepresentation.getModelType() == null || modelRepresentation.getModelType().equals(Model.MODEL_TYPE_BPMN)) {
            // BPMN model
            ObjectNode editorNode = null;
            try {
                editorNode = (ObjectNode) objectMapper.readTree(json);

                ObjectNode propertiesNode = (ObjectNode) editorNode.get("properties");
                String processId = modelRepresentation.getKey().replaceAll(" ", "");
                propertiesNode.put("process_id", processId);
                propertiesNode.put("name", modelRepresentation.getName());
                if (StringUtils.isNotEmpty(modelRepresentation.getDescription())) {
                    propertiesNode.put("documentation", modelRepresentation.getDescription());
                }
                editorNode.set("properties", propertiesNode);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (editorNode != null) {
                json = editorNode.toString();
            }
        }

        // create the new model
        Model newModel = createModel(modelRepresentation, json);

        // copy also the thumbnail
        byte[] imageBytes = model.getThumbnail();
        newModel = saveModel(newModel, newModel.getModeleditorjson(), imageBytes, false, newModel.getComment());

        return new ModelRepresentation(newModel);
    }

    @Override
    public ModelRepresentation importProcessModel(HttpServletRequest request, MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".bpmn") || fileName.endsWith(".bpmn20.xml"))) {
            try {
                XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
                InputStreamReader xmlIn = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);
                BpmnModel bpmnModel = bpmnXmlConverter.convertToBpmnModel(xtr);
                if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
                    throw new BadRequestException("No process found in definition " + fileName);
                }

                if (bpmnModel.getLocationMap().size() == 0) {
                    BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel);
                    bpmnLayout.execute();
                }

                ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);

                Process process = bpmnModel.getMainProcess();
                String name = process.getId();
                if (StringUtils.isNotEmpty(process.getName())) {
                    name = process.getName();
                }
                String description = process.getDocumentation();

                ModelRepresentation model = new ModelRepresentation();
                model.setKey(process.getId());
                model.setName(name);
                model.setDescription(description);
                model.setModelType(Model.MODEL_TYPE_BPMN);
                Model newModel = createModel(model, modelNode.toString());
                return new ModelRepresentation(newModel);

            } catch (BadRequestException e) {
                throw e;

            } catch (Exception e) {
                logHelper.getLogger().error("Import failed for {}", fileName, e);
                throw new BadRequestException("Import failed for " + fileName + ", error message " + e.getMessage());
            }
        } else {
            throw new BadRequestException("Invalid file name, only .bpmn and .bpmn20.xml files are supported not " + fileName);
        }
    }

    public ResultListDataRepresentation getModels(String filter, String sort, Integer modelType, HttpServletRequest request) {

        // need to parse the filterText parameter ourselves, due to encoding issues with the default parsing.
        String filterText = null;
        List<NameValuePair> params = URLEncodedUtils.parse(request.getQueryString(), StandardCharsets.UTF_8);
        if (params != null) {
            for (NameValuePair nameValuePair : params) {
                if ("filterText".equalsIgnoreCase(nameValuePair.getName())) {
                    filterText = nameValuePair.getValue();
                }
            }
        }

        List<ModelRepresentation> resultList = new ArrayList<>();
        List<Model> models = null;

        String validFilter = makeValidFilterText(filterText);

        if (validFilter != null) {
            if (Objects.equals(sort, ModelSort.MODIFIED_DESC)) {
                models = _modelRepository.findByModeltypeAndFilter(modelType, validFilter, Sort.by(Sort.Direction.DESC, "created"));
            } else {
                models = _modelRepository.findByModeltypeAndFilter(modelType, validFilter, Sort.by(Sort.Direction.ASC, "created"));
            }

        } else {
            if (Objects.equals(sort, ModelSort.MODIFIED_DESC)) {
                models = _modelRepository.findByModeltype(modelType, Sort.by(Sort.Direction.DESC, "created"));
            } else {
                models = _modelRepository.findByModeltype(modelType, Sort.by(Sort.Direction.ASC, "created"));
            }
        }

        if (CollectionUtils.isNotEmpty(models)) {
            List<String> addedModelIds = new ArrayList<>();
            for (Model model : models) {
                if (!addedModelIds.contains(model.getId())) {
                    addedModelIds.add(model.getId());
                    ModelRepresentation representation = createModelRepresentation(model);
                    resultList.add(representation);
                }
            }
        }

        ResultListDataRepresentation result = new ResultListDataRepresentation(resultList);
        return result;
    }

    public ResultListDataRepresentation getModelsToIncludeInAppDefinition() {

        List<ModelRepresentation> resultList = new ArrayList<>();

        List<String> addedModelIds = new ArrayList<>();
        List<Model> models = _modelRepository.findByModeltype(Model.MODEL_TYPE_BPMN, Sort.by(Sort.Direction.DESC, "created"));

        if (CollectionUtils.isNotEmpty(models)) {
            for (Model model : models) {
                if (!addedModelIds.contains(model.getId())) {
                    addedModelIds.add(model.getId());
                    ModelRepresentation representation = createModelRepresentation(model);
                    resultList.add(representation);
                }
            }
        }

        ResultListDataRepresentation result = new ResultListDataRepresentation(resultList);
        return result;
    }

    @Override
    public ModelRepresentation createModel(ModelRepresentation modelRepresentation) {
        modelRepresentation.setKey(modelRepresentation.getKey().replaceAll(" ", ""));
        checkForDuplicateKey(modelRepresentation);
        String json = createModelJson(modelRepresentation);
        Model newModel = createModel(modelRepresentation, json);
        return new ModelRepresentation(newModel);
    }

    @Override
    public ObjectNode getModelEditorJson(String modelId) {
        Model model = getModel(modelId);
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put("modelId", model.getId());
        modelNode.put("name", model.getName());
        modelNode.put("key", model.getKey());
        modelNode.put("description", model.getDescription());
        modelNode.putPOJO("lastUpdated", model.getLastupdated());
        if (StringUtils.isNotEmpty(model.getModeleditorjson())) {
            try {
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModeleditorjson());
                editorJsonNode.put("modelType", "model");
                modelNode.set("model", editorJsonNode);
            } catch (Exception e) {
                logHelper.getLogger().error("Error reading editor json {}", modelId, e);
                throw new InternalServerErrorException("Error reading editor json " + modelId);
            }

        } else {
            ObjectNode editorJsonNode = objectMapper.createObjectNode();
            editorJsonNode.put("id", "canvas");
            editorJsonNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorJsonNode.put("modelType", "model");
            modelNode.set("model", editorJsonNode);
        }
        return modelNode;
    }

    public String createModelJson(ModelRepresentation model) {
        String json = null;
        if (Integer.valueOf(Model.MODEL_TYPE_DECISION_TABLE).equals(model.getModelType())) {
            try {
                DecisionTableDefinitionRepresentation decisionTableDefinition = new DecisionTableDefinitionRepresentation();

                String decisionTableDefinitionKey = model.getName().replaceAll(" ", "");
                decisionTableDefinition.setKey(decisionTableDefinitionKey);

                json = objectMapper.writeValueAsString(decisionTableDefinition);
            } catch (Exception e) {
                logHelper.getLogger().error("Error creating decision table model", e);
                throw new InternalServerErrorException("Error creating decision table");
            }

        }
//        else if (Integer.valueOf(Model.MODEL_TYPE_APP).equals(model.getModelType())) {
//            try {
//                json = objectMapper.writeValueAsString(new AppDefinition());
//            } catch (Exception e) {
//                LOGGER.error("Error creating app definition", e);
//                throw new InternalServerErrorException("Error creating app definition");
//            }
//
//        }
//        else if (Integer.valueOf(Model.MODEL_TYPE_CMMN).equals(model.getModelType())) {
//            ObjectNode editorNode = objectMapper.createObjectNode();
//            editorNode.put("id", "canvas");
//            editorNode.put("resourceId", "canvas");
//            ObjectNode stencilSetNode = objectMapper.createObjectNode();
//            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/cmmn1.1#");
//            editorNode.set("stencilset", stencilSetNode);
//            ObjectNode propertiesNode = objectMapper.createObjectNode();
//            propertiesNode.put("case_id", model.getKey());
//            propertiesNode.put("name", model.getName());
//            if (StringUtils.isNotEmpty(model.getDescription())) {
//                propertiesNode.put("documentation", model.getDescription());
//            }
//            editorNode.set("properties", propertiesNode);
//
//            ArrayNode childShapeArray = objectMapper.createArrayNode();
//            editorNode.set("childShapes", childShapeArray);
//            ObjectNode childNode = objectMapper.createObjectNode();
//            childShapeArray.add(childNode);
//            ObjectNode boundsNode = objectMapper.createObjectNode();
//            childNode.set("bounds", boundsNode);
//            ObjectNode lowerRightNode = objectMapper.createObjectNode();
//            boundsNode.set("lowerRight", lowerRightNode);
//            lowerRightNode.put("x", 758);
//            lowerRightNode.put("y", 754);
//            ObjectNode upperLeftNode = objectMapper.createObjectNode();
//            boundsNode.set("upperLeft", upperLeftNode);
//            upperLeftNode.put("x", 40);
//            upperLeftNode.put("y", 40);
//            childNode.set("childShapes", objectMapper.createArrayNode());
//            childNode.set("dockers", objectMapper.createArrayNode());
//            childNode.set("outgoing", objectMapper.createArrayNode());
//            childNode.put("resourceId", "casePlanModel");
//            ObjectNode stencilNode = objectMapper.createObjectNode();
//            childNode.set("stencil", stencilNode);
//            stencilNode.put("id", "CasePlanModel");
//            json = editorNode.toString();
//
//        }
        else {
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.set("stencilset", stencilSetNode);
            ObjectNode propertiesNode = objectMapper.createObjectNode();
            propertiesNode.put("process_id", model.getKey());
            propertiesNode.put("name", model.getName());
            if (StringUtils.isNotEmpty(model.getDescription())) {
                propertiesNode.put("documentation", model.getDescription());
            }
            editorNode.set("properties", propertiesNode);

            ArrayNode childShapeArray = objectMapper.createArrayNode();
            editorNode.set("childShapes", childShapeArray);
            ObjectNode childNode = objectMapper.createObjectNode();
            childShapeArray.add(childNode);
            ObjectNode boundsNode = objectMapper.createObjectNode();
            childNode.set("bounds", boundsNode);
            ObjectNode lowerRightNode = objectMapper.createObjectNode();
            boundsNode.set("lowerRight", lowerRightNode);
            lowerRightNode.put("x", 130);
            lowerRightNode.put("y", 193);
            ObjectNode upperLeftNode = objectMapper.createObjectNode();
            boundsNode.set("upperLeft", upperLeftNode);
            upperLeftNode.put("x", 100);
            upperLeftNode.put("y", 163);
            childNode.set("childShapes", objectMapper.createArrayNode());
            childNode.set("dockers", objectMapper.createArrayNode());
            childNode.set("outgoing", objectMapper.createArrayNode());
            childNode.put("resourceId", "startEvent1");
            ObjectNode stencilNode = objectMapper.createObjectNode();
            childNode.set("stencil", stencilNode);
            stencilNode.put("id", "StartNoneEvent");
            json = editorNode.toString();
        }

        return json;
    }

    @Override
    public ModelRepresentation saveModel(String modelId, MultiValueMap<String, String> values) {
        // Validation: see if there was another update in the meantime
        long lastUpdated = -1L;
        String lastUpdatedString = values.getFirst("lastUpdated");
        if (lastUpdatedString == null) {
            throw new BadRequestException("Missing lastUpdated date");
        }
        try {
            Date readValue = objectMapper.getDeserializationConfig().getDateFormat().parse(lastUpdatedString);
            lastUpdated = readValue.getTime();
        } catch (ParseException e) {
            throw new BadRequestException("Invalid lastUpdated date: '" + lastUpdatedString + "'");
        }

        Model model = getModel(modelId);
        String resolveAction = values.getFirst("conflictResolveAction");
        if (model.getLastupdated().getTime() != lastUpdated) {

            if (RESOLVE_ACTION_SAVE_AS.equals(resolveAction)) {
                String saveAs = values.getFirst("saveAs");
                String json = values.getFirst("json_xml");
                return createNewModel(saveAs, model.getDescription(), model.getModeltype(), json);
            } else if (RESOLVE_ACTION_OVERWRITE.equals(resolveAction)) {
                return updateModel(model, values, false);
            } else if (RESOLVE_ACTION_NEW_VERSION.equals(resolveAction)) {
                return updateModel(model, values, true);
            } else {
                // Tried everything, this is really a conflict, return 409
                ConflictingRequestException exception = new ConflictingRequestException("Process model was updated in the meantime");
                throw exception;
            }

        } else {
            // Actual, regular, update
            return updateModel(model, values, false);
        }
    }

    public Model saveModel(Model modelObject, String editorJson, byte[] imageBytes, boolean newVersion, String newVersionComment) {
        return internalSave(modelObject.getName(), modelObject.getKey(), modelObject.getDescription(), editorJson, newVersion,
                newVersionComment, imageBytes, modelObject);
    }

    protected ModelRepresentation createNewModel(String name, String description, Integer modelType, String editorJson) {
        ModelRepresentation model = new ModelRepresentation();
        model.setName(name);
        model.setDescription(description);
        model.setModelType(modelType);
        Model newModel = createModel(model, editorJson);
        return new ModelRepresentation(newModel);
    }

    public Model createModel(ModelRepresentation model, String editorJson) {
        Model newModel = new Model();
        newModel.setVersion(1);
        newModel.setName(model.getName());
        newModel.setKey(model.getKey());
        newModel.setModeltype(model.getModelType());
        newModel.setCreated(Calendar.getInstance().getTime());
        newModel.setDescription(model.getDescription());
        newModel.setModeleditorjson(editorJson);
        newModel.setLastupdated(Calendar.getInstance().getTime());
        newModel.setTenantid(model.getTenantId());

        persistModel(newModel);
        return newModel;
    }

    protected Model persistModel(Model model) {

        if (StringUtils.isNotEmpty(model.getModeleditorjson())) {

            // Parse json to java
            ObjectNode jsonNode = null;
            try {
                jsonNode = (ObjectNode) objectMapper.readTree(model.getModeleditorjson());
            } catch (Exception e) {
                logHelper.getLogger().error("Could not deserialize json model", e);
                throw new InternalServerErrorException("Could not deserialize json model");
            }

            if ((model.getModeltype() == null || model.getModeltype().intValue() == Model.MODEL_TYPE_BPMN)) {

                // Thumbnail
                byte[] thumbnail = modelImageService.generateThumbnailImage(model, jsonNode);
                if (thumbnail != null) {
                    model.setThumbnail(thumbnail);
                }

                _modelRepository.save(model);

                // Relations
                handleBpmnProcessFormModelRelations(model, jsonNode);
//                handleBpmnProcessDecisionTaskModelRelations(model, jsonNode);

            }
//            else if (model.getModeltype().intValue() == Model.MODEL_TYPE_CMMN) {
//
//                // Thumbnail
//                byte[] thumbnail = modelImageService.generateCmmnThumbnailImage(model, jsonNode);
//                if (thumbnail != null) {
//                    model.setThumbnail(thumbnail);
//                }
//
//                _modelRepository.save(model);
//
//                // Relations
////                handleCmmnFormModelRelations(model, jsonNode);
////                handleCmmnDecisionModelRelations(model, jsonNode);
////                handleCmmnCaseModelRelations(model, jsonNode);
////                handleCmmnProcessModelRelations(model, jsonNode);
//
//            }
            else if (model.getModeltype().intValue() == Model.MODEL_TYPE_DECISION_TABLE) {

                jsonNode.put("name", model.getName());
                jsonNode.put("key", model.getKey());
                _modelRepository.save(model);

            }
        }

        return model;
    }

    protected void handleBpmnProcessFormModelRelations(Model bpmnProcessModel, ObjectNode editorJsonNode) {
        List<JsonNode> formReferenceNodes = JsonConverterUtil.filterOutJsonNodes(JsonConverterUtil.getBpmnProcessModelFormReferences(editorJsonNode));
        Set<String> formIds = JsonConverterUtil.gatherStringPropertyFromJsonNodes(formReferenceNodes, "id");

        handleModelRelations(bpmnProcessModel, formIds, ModelRelationTypes.TYPE_FORM_MODEL_CHILD);
    }

    protected void handleAppModelProcessRelations(Model appModel, ObjectNode appModelJsonNode) {
        Set<String> processModelIds = JsonConverterUtil.getAppModelReferencedModelIds(appModelJsonNode);
        handleModelRelations(appModel, processModelIds, ModelRelationTypes.TYPE_PROCESS_MODEL);
    }


    protected void handleModelRelations(Model bpmnProcessModel, Set<String> idsReferencedInJson, String relationshipType) {

        // Find existing persisted relations
        List<ModelRelation> persistedModelRelations = _modelRelationRepository.findByParentModelIdAndType(bpmnProcessModel.getId(), relationshipType);

        // if no ids referenced now, just delete them all
        if (idsReferencedInJson == null || idsReferencedInJson.size() == 0) {
            for (ModelRelation modelRelation : persistedModelRelations) {
                _modelRelationRepository.delete(modelRelation);
            }
            return;
        }

        Set<String> alreadyPersistedModelIds = new HashSet<>(persistedModelRelations.size());
        for (ModelRelation persistedModelRelation : persistedModelRelations) {
            if (!idsReferencedInJson.contains(persistedModelRelation.getModel().getId())) {
                // model used to be referenced, but not anymore. Delete it.
                _modelRelationRepository.delete(persistedModelRelation);
            } else {
                alreadyPersistedModelIds.add(persistedModelRelation.getModel().getId());
            }
        }

        // Loop over all referenced ids and see which one are new
        for (String idReferencedInJson : idsReferencedInJson) {

            // if model is referenced, but it is not yet persisted = create it
            if (!alreadyPersistedModelIds.contains(idReferencedInJson)) {
                // Check if model actually still exists. Don't create the relationship if it doesn't exist. The client UI will have cope with this too.
                Optional<Model> optionalModel = _modelRepository.findById(idReferencedInJson);
                Model model = optionalModel.orElseThrow(() -> new IllegalArgumentException("Model not found for the given ID"));
                _modelRelationRepository.save(new ModelRelation(bpmnProcessModel, model, relationshipType));
            }
        }
    }


    protected ModelRepresentation updateModel(Model model, MultiValueMap<String, String> values, boolean forceNewVersion) {

        String name = values.getFirst("name");
        String key = values.getFirst("key").replaceAll(" ", "");
        String description = values.getFirst("description");
        String isNewVersionString = values.getFirst("newversion");
        String newVersionComment = null;

        ModelKeyRepresentation modelKeyInfo = validateModelKey(model, model.getModeltype(), key);
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Model with provided key already exists " + key);
        }

        boolean newVersion = false;
        if (forceNewVersion) {
            newVersion = true;
            newVersionComment = values.getFirst("comment");
        } else {
            if (isNewVersionString != null) {
                newVersion = "true".equals(isNewVersionString);
                newVersionComment = values.getFirst("comment");
            }
        }

        String json = values.getFirst("json_xml");

        try {
            ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(json);

            ObjectNode propertiesNode = (ObjectNode) editorJsonNode.get("properties");
            String processId = key;
            propertiesNode.put("process_id", processId);
            propertiesNode.put("name", name);
            if (StringUtils.isNotEmpty(description)) {
                propertiesNode.put("documentation", description);
            }
            editorJsonNode.set("properties", propertiesNode);
            model = saveModel(model.getId(), name, key, description, editorJsonNode.toString(), newVersion, newVersionComment);
            return new ModelRepresentation(model);

        } catch (Exception e) {
            logHelper.getLogger().error("Error saving model {}", model.getId(), e);
            throw new BadRequestException("Process model could not be saved " + model.getId());
        }
    }

    public Model saveModel(String modelId, String name, String key, String description, String editorJson, boolean newVersion, String newVersionComment) {

        Optional<Model> modelObject = _modelRepository.findById(modelId);
        Model model = modelObject.orElseThrow(() -> new NotFoundException("Model not found with ID: " + modelId));
        return internalSave(name, key, description, editorJson, newVersion, newVersionComment, null, model);
    }


    protected Model internalSave(String name, String key, String description, String editorJson, boolean newVersion,
                                 String newVersionComment, byte[] imageBytes, Model modelObject) {

        if (!newVersion) {

            modelObject.setLastupdated(new Date());
            modelObject.setName(name);
            modelObject.setKey(key);
            modelObject.setDescription(description);
            modelObject.setModeleditorjson(editorJson);

            if (imageBytes != null) {
                modelObject.setThumbnail(imageBytes);
            }

        } else {

            ModelHistory historyModel = createNewModelhistory(modelObject);
            persistModelHistory(historyModel);

            modelObject.setVersion(modelObject.getVersion() + 1);
            modelObject.setLastupdated(new Date());
            modelObject.setName(name);
            modelObject.setKey(key);
            modelObject.setDescription(description);
            modelObject.setModeleditorjson(editorJson);
            modelObject.setComment(newVersionComment);

            if (imageBytes != null) {
                modelObject.setThumbnail(imageBytes);
            }
        }

        return persistModel(modelObject);
    }

    protected ModelHistory createNewModelhistory(Model model) {
        ModelHistory historyModel = new ModelHistory();
        historyModel.setName(model.getName());
        historyModel.setKey(model.getKey());
        historyModel.setDescription(model.getDescription());
        historyModel.setCreated(model.getCreated());
        historyModel.setLastupdated(model.getLastupdated());
        historyModel.setModeleditorjson(model.getModeleditorjson());
        historyModel.setModeltype(model.getModeltype());
        historyModel.setVersion(model.getVersion());
        historyModel.setModel(model);
        historyModel.setComment(model.getComment());
        historyModel.setTenantid(model.getTenantid());

        return historyModel;
    }

    protected void persistModelHistory(ModelHistory modelHistory) {
        _modelHistoryRepository.save(modelHistory);
    }

    public ModelKeyRepresentation validateModelKey(Model model, Integer modelType, String key) {
        ModelKeyRepresentation modelKeyResponse = new ModelKeyRepresentation();
        modelKeyResponse.setKey(key);

        List<Model> models = _modelRepository.findByKeyAndModeltype(key, modelType);
        for (Model modelInfo : models) {
            if (model == null || !modelInfo.getId().equals(model.getId())) {
                modelKeyResponse.setKeyAlreadyExists(true);
                modelKeyResponse.setId(modelInfo.getId());
                modelKeyResponse.setName(modelInfo.getName());
                break;
            }
        }

        return modelKeyResponse;
    }

    @Override
    public void getProcessModelBpmn20Xml(HttpServletResponse response, String processModelId) {
        if (processModelId == null) {
            throw new BadRequestException("No process model id provided");
        }

        Model model = getModel(processModelId);
        generateBpmn20Xml(response, model);
    }

    protected void generateBpmn20Xml(HttpServletResponse response, Model model) {
        String name = model.getName().replaceAll(" ", "_") + ".bpmn20.xml";
        String encodedName = null;
        try {
            encodedName = "UTF-8''" + URLEncoder.encode(name, "UTF-8");
        } catch (Exception e) {
            logHelper.getLogger().warn("Failed to encode name " + name);
        }

        String contentDispositionValue = "attachment; filename=" + name;
        if (encodedName != null) {
            contentDispositionValue += "; filename*=" + encodedName;
        }

        response.setHeader("Content-Disposition", contentDispositionValue);
        if (model.getModeleditorjson() != null) {
            try {
                ServletOutputStream servletOutputStream = response.getOutputStream();
                response.setContentType("application/xml");

                BpmnModel bpmnModel = getBpmnModel(model);
                byte[] xmlBytes = getBpmnXML(bpmnModel);
                BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(xmlBytes));

                byte[] buffer = new byte[8096];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    servletOutputStream.write(buffer, 0, count);
                }

                // Flush and close stream
                servletOutputStream.flush();
                servletOutputStream.close();

            } catch (BaseModelerRestException e) {
                throw e;

            } catch (Exception e) {
                logHelper.getLogger().error("Could not generate BPMN 2.0 XML", e);
                throw new InternalServerErrorException("Could not generate BPMN 2.0 xml");
            }
        }
    }

    public byte[] getBpmnXML(Model model) {
        BpmnModel bpmnModel = getBpmnModel(model);
        return getBpmnXML(bpmnModel);
    }

    public byte[] getBpmnXML(BpmnModel bpmnModel) {
        for (Process process : bpmnModel.getProcesses()) {
            if (StringUtils.isNotEmpty(process.getId())) {
                char firstCharacter = process.getId().charAt(0);
                // no digit is allowed as first character
                if (Character.isDigit(firstCharacter)) {
                    process.setId("a" + process.getId());
                }
            }
        }
        byte[] xmlBytes = bpmnXmlConverter.convertToXML(bpmnModel);
        return xmlBytes;
    }

    public BpmnModel getBpmnModel(Model model) {
        BpmnModel bpmnModel = null;
        try {
            Map<String, Model> formMap = new HashMap<>();
            Map<String, Model> decisionTableMap = new HashMap<>();
            List<Model> referencedModels = _modelRepository.findAllById(Collections.singleton(model.getId()));
            for (Model childModel : referencedModels) {
                if (Model.MODEL_TYPE_FORM == childModel.getModeltype()) {
                    formMap.put(childModel.getId(), childModel);

                } else if (Model.MODEL_TYPE_DECISION_TABLE == childModel.getModeltype()) {
                    decisionTableMap.put(childModel.getId(), childModel);
                }
            }

            bpmnModel = getBpmnModel(model, formMap, decisionTableMap);

        } catch (Exception e) {
            logHelper.getLogger().error("Could not generate BPMN 2.0 model for {}", model.getId(), e);
            throw new InternalServerErrorException("Could not generate BPMN 2.0 model");
        }

        return bpmnModel;
    }

    public BpmnModel getBpmnModel(Model model, Map<String, Model> formMap, Map<String, Model> decisionTableMap) {
        try {
            ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModeleditorjson());
            Map<String, String> formKeyMap = new HashMap<>();
            for (Model formModel : formMap.values()) {
                formKeyMap.put(formModel.getId(), formModel.getKey());
            }

            Map<String, String> decisionTableKeyMap = new HashMap<>();
            for (Model decisionTableModel : decisionTableMap.values()) {
                decisionTableKeyMap.put(decisionTableModel.getId(), decisionTableModel.getKey());
            }

            return bpmnJsonConverter.convertToBpmnModel(editorJsonNode, formKeyMap, decisionTableKeyMap);

        } catch (Exception e) {
            logHelper.getLogger().error("Could not generate BPMN 2.0 model for {}", model.getId(), e);
            throw new InternalServerErrorException("Could not generate BPMN 2.0 model");
        }
    }



    protected String makeValidFilterText(String filterText) {
        String validFilter = null;

        if (filterText != null) {
            String trimmed = StringUtils.trim(filterText);
            if (trimmed.length() >= MIN_FILTER_LENGTH) {
                validFilter = "%" + trimmed.toLowerCase() + "%";
            }
        }
        return validFilter;
    }

    protected ModelRepresentation createModelRepresentation(Model model) {
        ModelRepresentation representation = null;
        representation = new ModelRepresentation(model);
        return representation;
    }

    public void checkForDuplicateKey(ModelRepresentation modelRepresentation) {
        ModelKeyRepresentation modelKeyInfo = validateModelKey(null, modelRepresentation.getModelType(), modelRepresentation.getKey());
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new ConflictingRequestException("Provided model key already exists: " + modelRepresentation.getKey());
        }
    }

    public void deleteModel(String modelId) {
        // Get model to check if it exists, read-permission required for delete
        Model model = getModel(modelId);
        try {
            deleteModelAndHistory(model);

        } catch (Exception e) {
            logHelper.getLogger().error("Error while deleting: ", e);
            throw new BadRequestException("Model cannot be deleted: " + modelId);
        }
    }

    public void deleteModelAndHistory(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("No model found with id: " + model.getId());
        }
        // Fetch current model history list
        List<ModelHistory> history = _modelHistoryRepository.findByModelId(model.getId());

        // Move model to history and mark removed
        ModelHistory historyModel = createNewModelhistory(model);
        historyModel.setRemovaldate(Calendar.getInstance().getTime());
        persistModelHistory(historyModel);

        deleteModelAndChildren(model);
    }

    protected void deleteModelAndChildren(Model model) {

        // Models have relations with each other, in all kind of wicked and funny ways.
        // Hence, we remove first all relations, comments, etc. while collecting all models.
        // Then, once all foreign key problem makers are removed, we remove the models

        List<Model> allModels;
        allModels = internalDeleteModelAndChildren(model);

        for (Model modelToDelete : allModels) {
            _modelRepository.delete(modelToDelete);
        }
    }

    protected List<Model> internalDeleteModelAndChildren(Model model) {
        // Delete all related data
        List<Model> allModels = new ArrayList<>();
        _modelRelationRepository.deleteByParentModelId(model.getId());
        allModels.add(model);
        return allModels;
    }

}



