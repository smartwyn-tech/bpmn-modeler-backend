package com.fastcode.bpmnModeler.restcontrollers;

import com.fastcode.bpmnModeler.application.decisiontable.IDecisionTableAppService;
import com.fastcode.bpmnModeler.application.decisiontable.dto.DecisionTableRepresentation;
import com.fastcode.bpmnModeler.application.decisiontable.dto.DecisionTableSaveRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ModelRepresentation;
import com.fastcode.bpmnModeler.application.model.dto.ResultListDataRepresentation;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/decision-table-models")
public class DecisionTableController {

    @Autowired()
    protected IDecisionTableAppService _decisionTableAppService;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResultListDataRepresentation getDecisionTables(HttpServletRequest request) {
        // need to parse the filterText parameter ourselves, due to encoding issues with the default parsing.
        String filter = null;
        List<NameValuePair> params = URLEncodedUtils.parse(request.getQueryString(), StandardCharsets.UTF_8);
        if (params != null) {
            for (NameValuePair nameValuePair : params) {
                if ("filter".equalsIgnoreCase(nameValuePair.getName())) {
                    filter = nameValuePair.getValue();
                }
            }
        }
        return _decisionTableAppService.getDecisionTables(filter);
    }

    @RequestMapping(value = "/{decisionTableId}", method = RequestMethod.GET, produces = "application/json")
    public DecisionTableRepresentation getDecisionTable(@PathVariable String decisionTableId) {
        return _decisionTableAppService.getDecisionTable(decisionTableId);
    }

    @RequestMapping(value = "/{decisionTableId}", method = RequestMethod.PUT, produces = "application/json")
    public DecisionTableRepresentation saveDecisionTable(@PathVariable String decisionTableId, @RequestBody DecisionTableSaveRepresentation saveRepresentation) {
        return _decisionTableAppService.saveDecisionTable(decisionTableId, saveRepresentation);
    }

    @RequestMapping(value = "/import-decision-table", method = RequestMethod.POST, produces = "application/json")
    public ModelRepresentation importDecisionTable(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        return _decisionTableAppService.importDecisionTable(request, file);
    }

}