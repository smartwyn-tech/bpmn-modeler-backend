package com.fastcode.bpmnModeler.domain.modelrelation;

import com.fastcode.bpmnModeler.application.modelrelation.dto.ModelInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.time.*;

@Repository("modelRelationRepository")
public interface IModelRelationRepository extends JpaRepository<ModelRelation, String>,QuerydslPredicateExecutor<ModelRelation> {
    List<ModelRelation> findByParentModelIdAndType(String parentModelId, String type);

    void deleteByParentModelId(String parentModelId);

    List<ModelInformation> findModelInformationByModelId(String modelId);
}

