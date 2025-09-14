package com.fastcode.bpmnModeler.domain.modelrelation;
import javax.persistence.*;
import java.time.*;
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
@Config(defaultVariableName = "modelRelationEntity")
@Table(name = "model_relation")
@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@TypeDefs({
}) 
public class ModelRelation extends AbstractEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "VARCHAR(512)", nullable = false)
    private String id;

    @Basic
    @Column(name = "type", nullable = true)
    private String type;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "parent_model_id")
    private Model parentModel;

    public ModelRelation(Model parentModel, Model model, String type) {
        this.type = type;
        this.model = model;
        this.parentModel = parentModel;
    }

}



