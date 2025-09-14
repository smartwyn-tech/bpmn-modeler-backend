package com.fastcode.bpmnModeler.application.decisiontable;

import com.fastcode.bpmnModeler.application.decisiontable.dto.DecisionTableDefinitionRepresentation;
import com.fastcode.bpmnModeler.application.decisiontable.dto.DecisionTableRepresentation;
import com.fastcode.bpmnModeler.application.decisiontable.dto.DecisionTableSaveRepresentation;
import com.fastcode.bpmnModeler.application.model.IModelAppService;
import com.fastcode.bpmnModeler.application.model.dto.ModelKeyRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ModelRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ResultListDataRepresentation;
import com.fastcode.bpmnModeler.commons.error.BadRequestException;
import com.fastcode.bpmnModeler.commons.error.InternalServerErrorException;
import com.fastcode.bpmnModeler.commons.error.NotFoundException;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import com.fastcode.bpmnModeler.commons.utils.XmlUtil;
import com.fastcode.bpmnModeler.domain.model.IModelRepository;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.dmn.model.DmnDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.flowable.dmn.xml.converter.DmnXMLConverter;
import org.flowable.editor.dmn.converter.DmnJsonConverter;

@Service("decisionTableAppService")
@RequiredArgsConstructor
public class DecisionTableAppService implements IDecisionTableAppService {

    protected static final int MIN_FILTER_LENGTH = 1;
    protected static final String PROCESS_NOT_FOUND_MESSAGE_KEY = "PROCESS.ERROR.NOT-FOUND";
    protected DmnXMLConverter dmnXmlConverter = new DmnXMLConverter();
    protected DmnJsonConverter dmnJsonConverter = new DmnJsonConverter();

    @NonNull
    protected final LoggingHelper logHelper;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired()
    protected IModelRepository _modelRepository;

    @Autowired()
    protected IModelAppService _modelAppService;

    @Autowired()
    protected DecisionTableModelConversionUtil _decisionTableModelConversionUtil;


    @Override
    public ResultListDataRepresentation getDecisionTables(String filter) {
        String validFilter = makeValidFilterText(filter);

        List<Model> models = null;

        if (validFilter != null) {
            models = _modelRepository.findByModeltypeAndFilter(Model.MODEL_TYPE_DECISION_TABLE, validFilter, Sort.by(Sort.Direction.ASC, "created"));

        } else {
            models = _modelRepository.findByModeltype(Model.MODEL_TYPE_DECISION_TABLE, Sort.by(Sort.Direction.ASC, "created"));
        }

        List<DecisionTableRepresentation> reps = new ArrayList<>();

        for (Model model : models) {
            reps.add(new DecisionTableRepresentation(model));
        }

        ResultListDataRepresentation result = new ResultListDataRepresentation(reps);
        result.setTotal(Long.valueOf(models.size()));
        return result;
    }

    @Override
    public DecisionTableRepresentation getDecisionTable(String decisionTableId) {
        return createDecisionTableRepresentation(getDecisionTableModel(decisionTableId));
    }

    @Override
    public DecisionTableRepresentation saveDecisionTable(String decisionTableId, DecisionTableSaveRepresentation saveRepresentation) {
        Model model = getModel(decisionTableId, false, false);

        String decisionKey = saveRepresentation.getDecisionTableRepresentation().getKey();
        ModelKeyRepresentation modelKeyInfo = _modelAppService.validateModelKey(model, model.getModeltype(), decisionKey);
        if (modelKeyInfo.isKeyAlreadyExists()) {
            throw new BadRequestException("Provided model key already exists: " + decisionKey);
        }

        model.setName(saveRepresentation.getDecisionTableRepresentation().getName());
        model.setKey(decisionKey);
        model.setDescription(saveRepresentation.getDecisionTableRepresentation().getDescription());

        saveRepresentation.getDecisionTableRepresentation().getDecisionTableDefinition().setKey(decisionKey);

        String editorJson = null;
        try {
            editorJson = objectMapper.writeValueAsString(saveRepresentation.getDecisionTableRepresentation().getDecisionTableDefinition());
        } catch (Exception e) {
            logHelper.getLogger().error("Error while processing decision table json", e);
            throw new InternalServerErrorException("Decision table could not be saved " + decisionTableId);
        }

        String filteredImageString = saveRepresentation.getDecisionTableImageBase64().replace("data:image/png;base64,", "");
        byte[] imageBytes = Base64.getDecoder().decode(filteredImageString);
        model = _modelAppService.saveModel(model, editorJson, imageBytes, saveRepresentation.isNewVersion(), saveRepresentation.getComment());
        DecisionTableRepresentation result = new DecisionTableRepresentation(model);
        result.setDecisionTableDefinition(saveRepresentation.getDecisionTableRepresentation().getDecisionTableDefinition());
        return result;
    }

    @Override
    public ModelRepresentation importDecisionTable(HttpServletRequest request, MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".dmn") || fileName.endsWith(".xml"))) {
            try {

                XMLInputFactory xif = XmlUtil.createSafeXmlInputFactory();
                InputStreamReader xmlIn = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                XMLStreamReader xtr = xif.createXMLStreamReader(xmlIn);

                DmnDefinition dmnDefinition = dmnXmlConverter.convertToDmnModel(xtr);

                if (dmnDefinition.getDecisions().size() == 0) {
                    throw new FlowableException("No decisions found in " + fileName);
                }

                ObjectNode editorJsonNode = dmnJsonConverter.convertToJson(dmnDefinition);

                // remove id to avoid InvalidFormatException when deserializing
                editorJsonNode.remove("id");

                // set to latest model version
                editorJsonNode.put("modelVersion", 2);

                ModelRepresentation modelRepresentation = new ModelRepresentation();
                modelRepresentation.setKey(dmnDefinition.getDecisions().get(0).getId());
                modelRepresentation.setName(dmnDefinition.getName());
                modelRepresentation.setDescription(dmnDefinition.getDescription());
                modelRepresentation.setModelType(Model.MODEL_TYPE_DECISION_TABLE);
                Model model = _modelAppService.createModel(modelRepresentation, editorJsonNode.toString());
                return new ModelRepresentation(model);

            } catch (Exception e) {
                logHelper.getLogger().error("Could not import decision table model", e);
                throw new InternalServerErrorException("Could not import decision table model");
            }
        } else {
            throw new BadRequestException("Invalid file name, only .dmn or .xml files are supported not " + fileName);
        }
    }


    protected DecisionTableRepresentation createDecisionTableRepresentation(Model model) {
        DecisionTableDefinitionRepresentation decisionTableDefinitionRepresentation = null;
        try {
            decisionTableDefinitionRepresentation = objectMapper.readValue(model.getModeleditorjson(), DecisionTableDefinitionRepresentation.class);
        } catch (Exception e) {
            logHelper.getLogger().error("Error deserializing decision table", e);
            throw new InternalServerErrorException("Could not deserialize decision table definition");
        }
        DecisionTableRepresentation result = new DecisionTableRepresentation(model);
        result.setDecisionTableDefinition(decisionTableDefinitionRepresentation);
        return result;
    }

    public Model getDecisionTableModel(String decisionTableId) {

        Model decisionTableModel = getModel(decisionTableId, true, false);

        // convert to new model version
        decisionTableModel = _decisionTableModelConversionUtil.convertModel(decisionTableModel);

        return decisionTableModel;
    }

    protected Model getModel(String modelId, boolean checkRead, boolean checkEdit) {
        Optional<Model> model = _modelRepository.findById(modelId);

        if (!model.isPresent()) {
            NotFoundException processNotFound = new NotFoundException("No model found with the given id: " + modelId);
            processNotFound.setMessageKey(PROCESS_NOT_FOUND_MESSAGE_KEY);
            throw processNotFound;
        }

        return model.get();
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
}
