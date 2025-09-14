package com.fastcode.bpmnModeler.domain.modelhistory;
import javax.persistence.*;
import java.time.*;
import java.util.Date;

import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.domain.abstractentity.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.querydsl.core.annotations.Config;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TypeDefs;


@Entity
@Config(defaultVariableName = "modelHistoryEntity")
@Table(name = "model_history")
@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@TypeDefs({
})
public class ModelHistory extends AbstractEntity {

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
    @Column(name = "removaldate", nullable = true)
    private Date removaldate;

    @Basic
    @Column(name = "tenantid", nullable = true)
    private String tenantid;

    @Basic
    @Column(name = "version", nullable = true)
    private Integer version;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "modelid")
    private Model model;


}


