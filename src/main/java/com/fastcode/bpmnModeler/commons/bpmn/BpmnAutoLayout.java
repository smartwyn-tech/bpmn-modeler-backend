
package com.fastcode.bpmnModeler.commons.bpmn;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flowable.bpmn.model.Artifact;
import org.flowable.bpmn.model.Association;
import org.flowable.bpmn.model.BaseElement;
import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.CallActivity;
import org.flowable.bpmn.model.DataObject;
import org.flowable.bpmn.model.Event;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowElementsContainer;
import org.flowable.bpmn.model.Gateway;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.SubProcess;
import org.flowable.bpmn.model.Task;
import org.flowable.bpmn.model.TextAnnotation;

public class BpmnAutoLayout {
    private static final String STYLE_EVENT = "styleEvent";
    private static final String STYLE_GATEWAY = "styleGateway";
    private static final String STYLE_SEQUENCEFLOW = "styleSequenceFlow";
    private static final String STYLE_BOUNDARY_SEQUENCEFLOW = "styleBoundarySequenceFlow";
    protected BpmnModel bpmnModel;
    protected int eventSize = 30;
    protected int gatewaySize = 40;
    protected int taskWidth = 100;
    protected int taskHeight = 60;
    protected int subProcessMargin = 20;
    protected mxGraph graph;
    protected Object cellParent;
    protected Map<String, Association> associations;
    protected Map<String, TextAnnotation> textAnnotations;
    protected Map<String, SequenceFlow> sequenceFlows;
    protected List<BoundaryEvent> boundaryEvents;
    protected Map<String, FlowElement> handledFlowElements;
    protected Map<String, Artifact> handledArtifacts;
    protected Map<String, Object> generatedVertices;
    protected Map<String, Object> generatedSequenceFlowEdges;
    protected Map<String, Object> generatedAssociationEdges;

    public BpmnAutoLayout(BpmnModel bpmnModel) {
        this.bpmnModel = bpmnModel;
    }

    public void execute() {
        this.bpmnModel.getLocationMap().clear();
        this.bpmnModel.getFlowLocationMap().clear();
        Iterator var1 = this.bpmnModel.getProcesses().iterator();

        while(var1.hasNext()) {
            Process process = (Process)var1.next();
            this.layout(process);
            this.translateNestedSubprocesses(process);
        }

    }

    protected void layout(FlowElementsContainer flowElementsContainer) {
        this.graph = new mxGraph();
        this.cellParent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();
        this.handledFlowElements = new HashMap();
        this.handledArtifacts = new HashMap();
        this.generatedVertices = new HashMap();
        this.generatedSequenceFlowEdges = new HashMap();
        this.generatedAssociationEdges = new HashMap();
        this.associations = new HashMap();
        this.textAnnotations = new HashMap();
        this.sequenceFlows = new HashMap();
        this.boundaryEvents = new ArrayList();

        Iterator var2;
        FlowElement flowElement;
        for(var2 = flowElementsContainer.getFlowElements().iterator(); var2.hasNext(); this.handledFlowElements.put(flowElement.getId(), flowElement)) {
            flowElement = (FlowElement)var2.next();
            if (flowElement instanceof SequenceFlow) {
                this.handleSequenceFlow((SequenceFlow)flowElement);
            } else if (flowElement instanceof Event) {
                this.handleEvent(flowElement);
            } else if (flowElement instanceof Gateway) {
                this.createGatewayVertex(flowElement);
            } else if (!(flowElement instanceof Task) && !(flowElement instanceof CallActivity)) {
                if (flowElement instanceof SubProcess) {
                    this.handleSubProcess(flowElement);
                }
            } else {
                this.handleActivity(flowElement);
            }
        }

        Artifact artifact;
        for(var2 = flowElementsContainer.getArtifacts().iterator(); var2.hasNext(); this.handledArtifacts.put(artifact.getId(), artifact)) {
            artifact = (Artifact)var2.next();
            if (artifact instanceof Association) {
                this.handleAssociation((Association)artifact);
            } else if (artifact instanceof TextAnnotation) {
                this.handleTextAnnotation((TextAnnotation)artifact);
            }
        }

        this.handleBoundaryEvents();
        this.handleSequenceFlow();
        this.handleAssociations();
        CustomLayout layout = new CustomLayout(this.graph, 7);
        layout.setIntraCellSpacing(100.0);
        layout.setResizeParent(true);
        layout.setFineTuning(true);
        layout.setParentBorder(20);
        layout.setMoveParent(true);
        layout.setDisableEdgeStyle(false);
        layout.setUseBoundingBox(true);
        layout.execute(this.graph.getDefaultParent());
        this.graph.getModel().endUpdate();
        this.generateDiagramInterchangeElements();
    }

    private void handleTextAnnotation(TextAnnotation artifact) {
        this.ensureArtifactIdSet(artifact);
        this.textAnnotations.put(artifact.getId(), artifact);
    }

    protected void ensureSequenceFlowIdSet(SequenceFlow sequenceFlow) {
        if (sequenceFlow.getId() == null) {
            sequenceFlow.setId("sequenceFlow-" + UUID.randomUUID().toString());
        }

    }

    protected void ensureArtifactIdSet(Artifact artifact) {
        if (artifact.getId() == null) {
            artifact.setId("artifact-" + UUID.randomUUID().toString());
        }

    }

    protected void handleAssociation(Association association) {
        this.ensureArtifactIdSet(association);
        this.associations.put(association.getId(), association);
    }

    protected void handleSequenceFlow(SequenceFlow sequenceFlow) {
        this.ensureSequenceFlowIdSet(sequenceFlow);
        this.sequenceFlows.put(sequenceFlow.getId(), sequenceFlow);
    }

    protected void handleEvent(FlowElement flowElement) {
        if (flowElement instanceof BoundaryEvent) {
            this.boundaryEvents.add((BoundaryEvent)flowElement);
        } else {
            this.createEventVertex(flowElement);
        }

    }

    protected void handleActivity(FlowElement flowElement) {
        Object activityVertex = this.graph.insertVertex(this.cellParent, flowElement.getId(), "", 0.0, 0.0, (double)this.taskWidth, (double)this.taskHeight);
        this.generatedVertices.put(flowElement.getId(), activityVertex);
    }

    protected void handleSubProcess(FlowElement flowElement) {
        BpmnAutoLayout bpmnAutoLayout = new BpmnAutoLayout(this.bpmnModel);
        bpmnAutoLayout.layout((SubProcess)flowElement);
        double subProcessWidth = bpmnAutoLayout.getGraph().getView().getGraphBounds().getWidth();
        double subProcessHeight = bpmnAutoLayout.getGraph().getView().getGraphBounds().getHeight();
        Object subProcessVertex = this.graph.insertVertex(this.cellParent, flowElement.getId(), "", 0.0, 0.0, subProcessWidth + (double)(2 * this.subProcessMargin), subProcessHeight + (double)(2 * this.subProcessMargin));
        this.generatedVertices.put(flowElement.getId(), subProcessVertex);
    }

    protected void handleBoundaryEvents() {
        Iterator var1 = this.boundaryEvents.iterator();

        while(var1.hasNext()) {
            BoundaryEvent boundaryEvent = (BoundaryEvent)var1.next();
            mxGeometry geometry = new mxGeometry(0.8, 1.0, (double)this.eventSize, (double)this.eventSize);
            geometry.setOffset(new mxPoint((double)(-(this.eventSize / 2)), (double)(-(this.eventSize / 2))));
            geometry.setRelative(true);
            mxCell boundaryPort = new mxCell((Object)null, geometry, "shape=ellipse;perimeter=ellipsePerimeter");
            boundaryPort.setId("boundary-event-" + boundaryEvent.getId());
            boundaryPort.setVertex(true);
            Object portParent = null;
            if (boundaryEvent.getAttachedToRefId() != null) {
                portParent = this.generatedVertices.get(boundaryEvent.getAttachedToRefId());
            } else {
                if (boundaryEvent.getAttachedToRef() == null) {
                    throw new RuntimeException("Could not generate DI: boundaryEvent '" + boundaryEvent.getId() + "' has no attachedToRef");
                }

                portParent = this.generatedVertices.get(boundaryEvent.getAttachedToRef().getId());
            }

            this.graph.addCell(boundaryPort, portParent);
            this.generatedVertices.put(boundaryEvent.getId(), boundaryPort);
        }

    }

    protected void handleSequenceFlow() {
        Hashtable<String, Object> edgeStyle = new Hashtable();
        edgeStyle.put(mxConstants.STYLE_ORTHOGONAL, true);
        edgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.ElbowConnector);
        edgeStyle.put(mxConstants.STYLE_ENTRY_X, 0.0);
        edgeStyle.put(mxConstants.STYLE_ENTRY_Y, 0.5);
        this.graph.getStylesheet().putCellStyle("styleSequenceFlow", edgeStyle);
        Hashtable<String, Object> boundaryEdgeStyle = new Hashtable();
        boundaryEdgeStyle.put(mxConstants.STYLE_EXIT_X, 0.5);
        boundaryEdgeStyle.put(mxConstants.STYLE_EXIT_Y, 1.0);
        boundaryEdgeStyle.put(mxConstants.STYLE_ENTRY_X, 0.5);
        boundaryEdgeStyle.put(mxConstants.STYLE_ENTRY_Y, 1.0);
        boundaryEdgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.orthConnector);
        this.graph.getStylesheet().putCellStyle("styleBoundarySequenceFlow", boundaryEdgeStyle);
        Iterator var3 = this.sequenceFlows.values().iterator();

        while(var3.hasNext()) {
            SequenceFlow sequenceFlow = (SequenceFlow)var3.next();
            Object sourceVertex = this.generatedVertices.get(sequenceFlow.getSourceRef());
            Object targetVertex = this.generatedVertices.get(sequenceFlow.getTargetRef());
            String style = null;
            if (this.handledFlowElements.get(sequenceFlow.getSourceRef()) instanceof BoundaryEvent) {
                style = "styleBoundarySequenceFlow";
            } else {
                style = "styleSequenceFlow";
            }

            Object sequenceFlowEdge = this.graph.insertEdge(this.cellParent, sequenceFlow.getId(), "", sourceVertex, targetVertex, style);
            this.generatedSequenceFlowEdges.put(sequenceFlow.getId(), sequenceFlowEdge);
        }

    }

    protected void handleAssociations() {
        Hashtable<String, Object> edgeStyle = new Hashtable();
        edgeStyle.put(mxConstants.STYLE_ORTHOGONAL, true);
        edgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.ElbowConnector);
        edgeStyle.put(mxConstants.STYLE_ENTRY_X, 0.0);
        edgeStyle.put(mxConstants.STYLE_ENTRY_Y, 0.5);
        this.graph.getStylesheet().putCellStyle("styleSequenceFlow", edgeStyle);
        Hashtable<String, Object> boundaryEdgeStyle = new Hashtable();
        boundaryEdgeStyle.put(mxConstants.STYLE_EXIT_X, 0.5);
        boundaryEdgeStyle.put(mxConstants.STYLE_EXIT_Y, 1.0);
        boundaryEdgeStyle.put(mxConstants.STYLE_ENTRY_X, 0.5);
        boundaryEdgeStyle.put(mxConstants.STYLE_ENTRY_Y, 1.0);
        boundaryEdgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.orthConnector);
        this.graph.getStylesheet().putCellStyle("styleBoundarySequenceFlow", boundaryEdgeStyle);
        Iterator var3 = this.associations.values().iterator();

        while(var3.hasNext()) {
            Association association = (Association)var3.next();
            Object sourceVertex = this.generatedVertices.get(association.getSourceRef());
            Object targetVertex = this.generatedVertices.get(association.getTargetRef());
            String style = null;
            if (this.handledFlowElements.get(association.getSourceRef()) instanceof BoundaryEvent) {
                style = "styleBoundarySequenceFlow";
            } else {
                style = "styleSequenceFlow";
            }

            Object associationEdge = this.graph.insertEdge(this.cellParent, association.getId(), "", sourceVertex, targetVertex, style);
            this.generatedAssociationEdges.put(association.getId(), associationEdge);
        }

    }

    protected void createEventVertex(FlowElement flowElement) {
        if (!this.graph.getStylesheet().getStyles().containsKey("styleEvent")) {
            Hashtable<String, Object> eventStyle = new Hashtable();
            eventStyle.put(mxConstants.STYLE_SHAPE, "ellipse");
            this.graph.getStylesheet().putCellStyle("styleEvent", eventStyle);
        }

        Object eventVertex = this.graph.insertVertex(this.cellParent, flowElement.getId(), "", 0.0, 0.0, (double)this.eventSize, (double)this.eventSize, "styleEvent");
        this.generatedVertices.put(flowElement.getId(), eventVertex);
    }

    protected void createGatewayVertex(FlowElement flowElement) {
        if (this.graph.getStylesheet().getStyles().containsKey("styleGateway")) {
            Hashtable<String, Object> style = new Hashtable();
            style.put(mxConstants.STYLE_SHAPE, "rhombus");
            this.graph.getStylesheet().putCellStyle("styleGateway", style);
        }

        Object gatewayVertex = this.graph.insertVertex(this.cellParent, flowElement.getId(), "", 0.0, 0.0, (double)this.gatewaySize, (double)this.gatewaySize, "styleGateway");
        this.generatedVertices.put(flowElement.getId(), gatewayVertex);
    }

    protected void generateDiagramInterchangeElements() {
        this.generateActivityDiagramInterchangeElements();
        this.generateSequenceFlowDiagramInterchangeElements();
        this.generateAssociationDiagramInterchangeElements();
    }

    protected void generateActivityDiagramInterchangeElements() {
        Iterator var1 = this.generatedVertices.keySet().iterator();

        while(var1.hasNext()) {
            String flowElementId = (String)var1.next();
            Object vertex = this.generatedVertices.get(flowElementId);
            mxCellState cellState = this.graph.getView().getState(vertex);
            GraphicInfo subProcessGraphicInfo = this.createDiagramInterchangeInformation((FlowElement)this.handledFlowElements.get(flowElementId), (int)cellState.getX(), (int)cellState.getY(), (int)cellState.getWidth(), (int)cellState.getHeight());
            if (this.handledFlowElements.get(flowElementId) instanceof SubProcess) {
                subProcessGraphicInfo.setExpanded(true);
            }
        }

    }

    protected void generateSequenceFlowDiagramInterchangeElements() {
        String sequenceFlowId;
        List points;
        for(Iterator var1 = this.generatedSequenceFlowEdges.keySet().iterator(); var1.hasNext(); this.createDiagramInterchangeInformation((BaseElement)this.handledFlowElements.get(sequenceFlowId), this.optimizeEdgePoints(points))) {
            sequenceFlowId = (String)var1.next();
            Object edge = this.generatedSequenceFlowEdges.get(sequenceFlowId);
            points = this.graph.getView().getState(edge).getAbsolutePoints();
            FlowElement sourceElement = (FlowElement)this.handledFlowElements.get(((SequenceFlow)this.sequenceFlows.get(sequenceFlowId)).getSourceRef());
            if (sourceElement instanceof Gateway && ((Gateway)sourceElement).getOutgoingFlows().size() > 1) {
                mxPoint startPoint = (mxPoint)points.get(0);
                Object gatewayVertex = this.generatedVertices.get(sourceElement.getId());
                mxCellState gatewayState = this.graph.getView().getState(gatewayVertex);
                mxPoint northPoint = new mxPoint(gatewayState.getX() + gatewayState.getWidth() / 2.0, gatewayState.getY());
                mxPoint southPoint = new mxPoint(gatewayState.getX() + gatewayState.getWidth() / 2.0, gatewayState.getY() + gatewayState.getHeight());
                mxPoint eastPoint = new mxPoint(gatewayState.getX() + gatewayState.getWidth(), gatewayState.getY() + gatewayState.getHeight() / 2.0);
                mxPoint westPoint = new mxPoint(gatewayState.getX(), gatewayState.getY() + gatewayState.getHeight() / 2.0);
                double closestDistance = Double.MAX_VALUE;
                mxPoint closestPoint = null;
                Iterator var16 = Arrays.asList(northPoint, southPoint, eastPoint, westPoint).iterator();

                while(var16.hasNext()) {
                    mxPoint rhombusPoint = (mxPoint)var16.next();
                    double distance = this.euclidianDistance(startPoint, rhombusPoint);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestPoint = rhombusPoint;
                    }
                }

                startPoint.setX(closestPoint.getX());
                startPoint.setY(closestPoint.getY());
                if (points.size() > 1) {
                    mxPoint nextPoint = (mxPoint)points.get(1);
                    nextPoint.setY(closestPoint.getY());
                }
            }
        }

    }

    protected void generateAssociationDiagramInterchangeElements() {
        Iterator var1 = this.generatedAssociationEdges.keySet().iterator();

        while(var1.hasNext()) {
            String associationId = (String)var1.next();
            Object edge = this.generatedAssociationEdges.get(associationId);
            List<mxPoint> points = this.graph.getView().getState(edge).getAbsolutePoints();
            this.createDiagramInterchangeInformation((BaseElement)this.handledArtifacts.get(associationId), this.optimizeEdgePoints(points));
        }

    }

    protected double euclidianDistance(mxPoint point1, mxPoint point2) {
        return Math.sqrt((point2.getX() - point1.getX()) * (point2.getX() - point1.getX()) + (point2.getY() - point1.getY()) * (point2.getY() - point1.getY()));
    }

    protected List<mxPoint> optimizeEdgePoints(List<mxPoint> unoptimizedPointsList) {
        List<mxPoint> optimizedPointsList = new ArrayList();

        for(int i = 0; i < unoptimizedPointsList.size(); ++i) {
            boolean keepPoint = true;
            mxPoint currentPoint = (mxPoint)unoptimizedPointsList.get(i);
            if (i > 0 && i != unoptimizedPointsList.size() - 1) {
                mxPoint previousPoint = (mxPoint)unoptimizedPointsList.get(i - 1);
                mxPoint nextPoint = (mxPoint)unoptimizedPointsList.get(i + 1);
                if (currentPoint.getX() >= previousPoint.getX() && currentPoint.getX() <= nextPoint.getX() && currentPoint.getY() == previousPoint.getY() && currentPoint.getY() == nextPoint.getY()) {
                    keepPoint = false;
                } else if (currentPoint.getY() >= previousPoint.getY() && currentPoint.getY() <= nextPoint.getY() && currentPoint.getX() == previousPoint.getX() && currentPoint.getX() == nextPoint.getX()) {
                    keepPoint = false;
                }
            }

            if (keepPoint) {
                optimizedPointsList.add(currentPoint);
            }
        }

        return optimizedPointsList;
    }

    protected GraphicInfo createDiagramInterchangeInformation(FlowElement flowElement, int x, int y, int width, int height) {
        GraphicInfo graphicInfo = new GraphicInfo();
        graphicInfo.setX((double)x);
        graphicInfo.setY((double)y);
        graphicInfo.setWidth((double)width);
        graphicInfo.setHeight((double)height);
        graphicInfo.setElement(flowElement);
        this.bpmnModel.addGraphicInfo(flowElement.getId(), graphicInfo);
        return graphicInfo;
    }

    protected void createDiagramInterchangeInformation(BaseElement element, List<mxPoint> waypoints) {
        List<GraphicInfo> graphicInfoForWaypoints = new ArrayList();
        Iterator var4 = waypoints.iterator();

        while(var4.hasNext()) {
            mxPoint waypoint = (mxPoint)var4.next();
            GraphicInfo graphicInfo = new GraphicInfo();
            graphicInfo.setElement(element);
            graphicInfo.setX(waypoint.getX());
            graphicInfo.setY(waypoint.getY());
            graphicInfoForWaypoints.add(graphicInfo);
        }

        this.bpmnModel.addFlowGraphicInfoList(element.getId(), graphicInfoForWaypoints);
    }

    protected void translateNestedSubprocesses(Process process) {
        Iterator var2 = process.getFlowElements().iterator();

        while(var2.hasNext()) {
            FlowElement flowElement = (FlowElement)var2.next();
            if (flowElement instanceof SubProcess) {
                this.translateNestedSubprocessElements((SubProcess)flowElement);
            }
        }

    }

    protected void translateNestedSubprocessElements(SubProcess subProcess) {
        GraphicInfo subProcessGraphicInfo = (GraphicInfo)this.bpmnModel.getLocationMap().get(subProcess.getId());
        double subProcessX = subProcessGraphicInfo.getX();
        double subProcessY = subProcessGraphicInfo.getY();
        List<SubProcess> nestedSubProcesses = new ArrayList();
        Iterator var8 = subProcess.getFlowElements().iterator();

        while(var8.hasNext()) {
            FlowElement flowElement = (FlowElement)var8.next();
            if (flowElement instanceof SequenceFlow) {
                List<GraphicInfo> graphicInfos = (List)this.bpmnModel.getFlowLocationMap().get(flowElement.getId());
                Iterator var11 = graphicInfos.iterator();

                while(var11.hasNext()) {
                    GraphicInfo graphicInfo = (GraphicInfo)var11.next();
                    graphicInfo.setX(graphicInfo.getX() + subProcessX + (double)this.subProcessMargin);
                    graphicInfo.setY(graphicInfo.getY() + subProcessY + (double)this.subProcessMargin);
                }
            } else if (!(flowElement instanceof DataObject)) {
                GraphicInfo graphicInfo = (GraphicInfo)this.bpmnModel.getLocationMap().get(flowElement.getId());
                graphicInfo.setX(graphicInfo.getX() + subProcessX + (double)this.subProcessMargin);
                graphicInfo.setY(graphicInfo.getY() + subProcessY + (double)this.subProcessMargin);
            }

            if (flowElement instanceof SubProcess) {
                nestedSubProcesses.add((SubProcess)flowElement);
            }
        }

        var8 = nestedSubProcesses.iterator();

        while(var8.hasNext()) {
            SubProcess nestedSubProcess = (SubProcess)var8.next();
            this.translateNestedSubprocessElements(nestedSubProcess);
        }

    }

    public mxGraph getGraph() {
        return this.graph;
    }

    public void setGraph(mxGraph graph) {
        this.graph = graph;
    }

    public int getEventSize() {
        return this.eventSize;
    }

    public void setEventSize(int eventSize) {
        this.eventSize = eventSize;
    }

    public int getGatewaySize() {
        return this.gatewaySize;
    }

    public void setGatewaySize(int gatewaySize) {
        this.gatewaySize = gatewaySize;
    }

    public int getTaskWidth() {
        return this.taskWidth;
    }

    public void setTaskWidth(int taskWidth) {
        this.taskWidth = taskWidth;
    }

    public int getTaskHeight() {
        return this.taskHeight;
    }

    public void setTaskHeight(int taskHeight) {
        this.taskHeight = taskHeight;
    }

    public int getSubProcessMargin() {
        return this.subProcessMargin;
    }

    public void setSubProcessMargin(int subProcessMargin) {
        this.subProcessMargin = subProcessMargin;
    }

    static class CustomLayout extends mxHierarchicalLayout {
        public CustomLayout(mxGraph graph, int orientation) {
            super(graph, orientation);
            this.traverseAncestors = false;
        }
    }
}
