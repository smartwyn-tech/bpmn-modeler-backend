package com.fastcode.bpmnModeler.application.decisiontable;

import com.fastcode.bpmnModeler.commons.error.InternalServerErrorException;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service("decisionTableModelConversionUtil")
@RequiredArgsConstructor
public class DecisionTableModelConversionUtil {

    @NonNull
    protected final LoggingHelper logHelper;

    public Model convertModel(Model decisionTableModel) {

        if (StringUtils.isNotEmpty(decisionTableModel.getModeleditorjson())) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode decisionTableNode = objectMapper.readTree(decisionTableModel.getModeleditorjson());
                migrateModel(decisionTableNode, objectMapper);

                // replace editor json
                decisionTableModel.setModeleditorjson(decisionTableNode.toString());

            } catch (Exception e) {
                throw new InternalServerErrorException(String.format("Error converting decision table %s to new model version", decisionTableModel.getName()));
            }
        }

        return decisionTableModel;
    }

    public JsonNode migrateModel(JsonNode decisionTableNode, ObjectMapper objectMapper) {
        if ((decisionTableNode.get("modelVersion") == null || decisionTableNode.get("modelVersion").isNull()) && decisionTableNode.has("name")) {
            String modelName = decisionTableNode.get("name").asText();
            logHelper.getLogger().info("Decision table model with name " + modelName + " found with version < v2; migrating to v3");
            ObjectNode decisionTableObjectNode = (ObjectNode)decisionTableNode;
            decisionTableObjectNode.put("modelVersion", "3");
            JsonNode inputExpressionNodes = decisionTableNode.get("inputExpressions");
            Map<String, String> inputExpressionIds = new HashMap();
            if (inputExpressionNodes != null && !inputExpressionNodes.isNull()) {
                Iterator var6 = inputExpressionNodes.iterator();

                while(var6.hasNext()) {
                    JsonNode inputExpressionNode = (JsonNode)var6.next();
                    if (inputExpressionNode.get("id") != null && !inputExpressionNode.get("id").isNull()) {
                        String inputId = inputExpressionNode.get("id").asText();
                        String inputType = null;
                        if (inputExpressionNode.get("type") != null && !inputExpressionNode.get("type").isNull()) {
                            inputType = inputExpressionNode.get("type").asText();
                        }

                        inputExpressionIds.put(inputId, inputType);
                    }
                }
            }

            JsonNode ruleNodes = decisionTableNode.get("rules");
            ArrayNode newRuleNodes = objectMapper.createArrayNode();
            if (ruleNodes != null && !ruleNodes.isNull()) {
                Iterator var21 = ruleNodes.iterator();

                while(true) {
                    JsonNode ruleNode;
                    if (!var21.hasNext()) {
                        if (inputExpressionNodes != null && !inputExpressionNodes.isNull()) {
                            var21 = inputExpressionNodes.iterator();

                            while(var21.hasNext()) {
                                ruleNode = (JsonNode)var21.next();
                                if (ruleNode.get("id") != null && !ruleNode.get("id").isNull()) {
                                    String inputId = ruleNode.get("id").asText();
                                    ((ObjectNode)ruleNode).put("type", (String)inputExpressionIds.get(inputId));
                                }
                            }
                        }

                        decisionTableObjectNode.replace("rules", newRuleNodes);
                        break;
                    }

                    ruleNode = (JsonNode)var21.next();
                    ObjectNode newRuleNode = objectMapper.createObjectNode();
                    Iterator ruleProperty = inputExpressionIds.keySet().iterator();

                    String inputExpressionId;
                    String expressionValue;
                    while(ruleProperty.hasNext()) {
                        inputExpressionId = (String)ruleProperty.next();
                        if (ruleNode.has(inputExpressionId)) {
                            expressionValue = inputExpressionId + "_operator";
                            String expressionId = inputExpressionId + "_expression";
                            String operatorValue = null;
                            expressionValue = null;
                            if (ruleNode.get(inputExpressionId) != null && !ruleNode.get(inputExpressionId).isNull()) {
                                String oldExpression = ruleNode.get(inputExpressionId).asText();
                                if (StringUtils.isNotEmpty(oldExpression)) {
                                    if (oldExpression.indexOf(32) != -1) {
                                        operatorValue = oldExpression.substring(0, oldExpression.indexOf(32));
                                        expressionValue = oldExpression.substring(oldExpression.indexOf(32) + 1);
                                    } else {
                                        expressionValue = oldExpression;
                                    }

                                    if (expressionValue.startsWith("\"") && expressionValue.endsWith("\"")) {
                                        expressionValue = expressionValue.substring(1, expressionValue.length() - 1);
                                    }

                                    if (expressionValue.startsWith("fn_date(")) {
                                        expressionValue = expressionValue.substring(9, expressionValue.lastIndexOf(39));
                                    } else if (expressionValue.startsWith("date:toDate(")) {
                                        expressionValue = expressionValue.substring(13, expressionValue.lastIndexOf(39));
                                    }

                                    if (StringUtils.isEmpty((CharSequence)inputExpressionIds.get(inputExpressionId))) {
                                        String expressionType = determineExpressionType(expressionValue);
                                        inputExpressionIds.put(inputExpressionId, expressionType);
                                    }
                                }
                            }

                            if (StringUtils.isNotEmpty(operatorValue)) {
                                newRuleNode.put(expressionValue, operatorValue);
                            } else {
                                newRuleNode.put(expressionValue, "==");
                            }

                            if (StringUtils.isNotEmpty(expressionValue)) {
                                newRuleNode.put(expressionId, expressionValue);
                            } else {
                                newRuleNode.put(expressionId, "-");
                            }
                        }
                    }

                    ruleProperty = ruleNode.fieldNames();

                    while(ruleProperty.hasNext()) {
                        inputExpressionId = (String)ruleProperty.next();
                        if (!inputExpressionIds.containsKey(inputExpressionId) && ruleNode.hasNonNull(inputExpressionId)) {
                            expressionValue = ruleNode.get(inputExpressionId).asText();
                            if (StringUtils.isNotEmpty(expressionValue) && expressionValue.startsWith("\"") && expressionValue.endsWith("\"")) {
                                expressionValue = expressionValue.substring(1, expressionValue.length() - 1);
                            }

                            if (expressionValue.startsWith("fn_date(")) {
                                expressionValue = expressionValue.substring(9, expressionValue.lastIndexOf(39));
                            } else if (expressionValue.startsWith("date:toDate(")) {
                                expressionValue = expressionValue.substring(13, expressionValue.lastIndexOf(39));
                            }

                            newRuleNode.put(inputExpressionId, expressionValue);
                        }
                    }

                    newRuleNodes.add(newRuleNode);
                }
            }

            logHelper.getLogger().info("Decision table model " + modelName + " migrated to v2");
        }

        return decisionTableNode;
    }

    public static String determineExpressionType(String expressionValue) {
        String expressionType = null;
        if (!"-".equals(expressionValue)) {
            expressionType = "string";
            if (NumberUtils.isCreatable(expressionValue)) {
                expressionType = "number";
            } else {
                try {
                    (new SimpleDateFormat("yyyy-MM-dd")).parse(expressionValue);
                    expressionType = "date";
                } catch (ParseException var3) {
                    if ("true".equalsIgnoreCase(expressionValue) || "false".equalsIgnoreCase(expressionType)) {
                        expressionType = "boolean";
                    }
                }
            }
        }

        return expressionType;
    }


}
