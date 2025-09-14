package com.fastcode.bpmnModeler.application.decisiontable;

import com.fastcode.bpmnModeler.application.decisiontable.dto.DecisionTableRepresentation;
import com.fastcode.bpmnModeler.application.decisiontable.dto.DecisionTableSaveRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ModelRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ResultListDataRepresentation;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface IDecisionTableAppService {

    ResultListDataRepresentation getDecisionTables(String filter);

    DecisionTableRepresentation getDecisionTable(String decisionTableId);

    DecisionTableRepresentation saveDecisionTable(String decisionTableId, DecisionTableSaveRepresentation saveRepresentation);

    ModelRepresentation importDecisionTable(HttpServletRequest request, MultipartFile file);
}
