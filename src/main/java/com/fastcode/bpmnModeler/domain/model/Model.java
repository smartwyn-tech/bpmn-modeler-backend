package com.fastcode.bpmnModeler.domain.model;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.fastcode.bpmnModeler.domain.modelhistory.ModelHistory;
import com.fastcode.bpmnModeler.domain.modelrelation.ModelRelation;
import com.fastcode.bpmnModeler.domain.abstractentity.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.querydsl.core.annotations.Config;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TypeDefs;


@Entity
@Config(defaultVariableName = "modelEntity")
@Table(name = "model")
@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@TypeDefs({
})
public class Model extends AbstractEntity {

    public static final int MODEL_TYPE_BPMN = 0;
    public static final int MODEL_TYPE_FORM = 2;
    public static final int MODEL_TYPE_APP = 3;
    public static final int MODEL_TYPE_DECISION_TABLE = 4;
    public static final int MODEL_TYPE_CMMN = 5;

    @Basic
    @Column(name = "comment", columnDefinition = "TEXT", nullable = true)
    private String comment;

    @Basic
    @Column(name = "created", nullable = true)
    private Date created;

    @Basic
    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    private String description;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "VARCHAR(512)", nullable = false)
    private String id;

    @Basic
    @Column(name = "key", nullable = true)
    private String key;

    @Basic
    @Column(name = "lastupdated", nullable = true)
    private Date lastupdated;

    @Basic
    @Column(name = "modeleditorjson", columnDefinition = "TEXT", nullable = true)
    private String modeleditorjson;

    @Basic
    @Column(name = "modeltype", nullable = true)
    private Integer modeltype;

    @Basic
    @Column(name = "name", nullable = true)
    private String name;

    @Basic
    @Column(name = "tenantid", nullable = true)
    private String tenantid;

    @Basic
    @Column(name = "version", nullable = true)
    private Integer version;

    @Basic
    @Column(name="thumbnail", nullable = true)
    private byte[] thumbnail;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ModelHistory> modelHistorysSet = new HashSet<ModelHistory>();

    public void addModelHistorys(ModelHistory modelHistorys) {
        modelHistorysSet.add(modelHistorys);

        modelHistorys.setModel(this);
    }
    public void removeModelHistorys(ModelHistory modelHistorys) {
        modelHistorysSet.remove(modelHistorys);

        modelHistorys.setModel(null);
    }

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ModelRelation> modelRelationsSet = new HashSet<ModelRelation>();

    public void addModelRelations(ModelRelation modelRelations) {
        modelRelationsSet.add(modelRelations);

        modelRelations.setModel(this);
    }
    public void removeModelRelations(ModelRelation modelRelations) {
        modelRelationsSet.remove(modelRelations);

        modelRelations.setModel(null);
    }

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ModelRelation> modelRelations2Set = new HashSet<ModelRelation>();

    public void addModelRelations2(ModelRelation modelRelations2) {
        modelRelations2Set.add(modelRelations2);

        modelRelations2.setModel(this);
    }
    public void removeModelRelations2(ModelRelation modelRelations2) {
        modelRelations2Set.remove(modelRelations2);

        modelRelations2.setModel(null);
    }


}


