package com.fastcode.bpmnModeler.application.modelrelation;

import com.fastcode.bpmnModeler.application.model.IModelAppService;
import com.fastcode.bpmnModeler.application.modelrelation.dto.ModelInformation;
import com.fastcode.bpmnModeler.commons.error.NotFoundException;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import com.fastcode.bpmnModeler.domain.model.IModelRepository;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.domain.modelrelation.IModelRelationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("modelRelationAppService")
@RequiredArgsConstructor
public class ModelRelationAppService implements IModelRelationAppService {
    
	@Qualifier("modelRelationRepository")
	@NonNull protected final IModelRelationRepository _modelRelationRepository;

	
    @Qualifier("modelRepository")
	@NonNull protected final IModelRepository _modelRepository;

	@Qualifier("IModelRelationMapperImpl")
	@NonNull protected final IModelRelationMapper mapper;

	@NonNull protected final LoggingHelper logHelper;

	@Qualifier("modelAppService")
	@NonNull  protected final IModelAppService _modelAppService;

	@Override
	public List<ModelInformation> findParentModels(String modelId) {
		Model model = _modelAppService.getModel(modelId);
		if (model == null) {
			throw new NotFoundException();
		}
		return _modelRelationRepository.findModelInformationByModelId(modelId);
	}
}



