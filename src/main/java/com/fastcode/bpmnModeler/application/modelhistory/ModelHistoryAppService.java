package com.fastcode.bpmnModeler.application.modelhistory;

import com.fastcode.bpmnModeler.application.model.ModelAppService;
import com.fastcode.bpmnModeler.application.model.dto.ModelRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ResultListDataRepresentation;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.domain.modelhistory.IModelHistoryRepository;
import com.fastcode.bpmnModeler.domain.modelhistory.ModelHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("modelHistoryAppService")
@RequiredArgsConstructor
public class ModelHistoryAppService implements IModelHistoryAppService {

    @Autowired()
    protected ModelAppService _modelService;

    @Autowired()
    private IModelHistoryRepository _modelHistoryRepository;

    @Override()
    public ResultListDataRepresentation getModelHistoryCollection(String modelId, Boolean includeLatestVersion) {

        Model model = _modelService.getModel(modelId);
        List<ModelHistory> history = _modelHistoryRepository.findByModelId(model.getId());
        ResultListDataRepresentation result = new ResultListDataRepresentation();

        List<ModelRepresentation> representations = new ArrayList<>();

        // Also include the latest version of the model
        if (Boolean.TRUE.equals(includeLatestVersion)) {
            representations.add(new ModelRepresentation(model));
        }
        if (history.size() > 0) {
            for (ModelHistory modelHistory : history) {
                representations.add(new ModelRepresentation(modelHistory));
            }
            result.setData(representations);
        }

        // Set size and total
        result.setSize(representations.size());
        result.setTotal(Long.valueOf(representations.size()));
        result.setStart(0);
        return result;
    }

}



