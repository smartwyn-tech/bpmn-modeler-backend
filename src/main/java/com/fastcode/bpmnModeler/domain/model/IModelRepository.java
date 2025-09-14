package com.fastcode.bpmnModeler.domain.model;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("modelRepository")
public interface IModelRepository extends JpaRepository<Model, String>,QuerydslPredicateExecutor<Model> {
    List<Model> findByKeyAndModeltype(String key, Integer modelType);

    @Query(value = "SELECT m FROM Model m WHERE m.modeltype = ?1 AND m.description LIKE %?2% ORDER BY m.created DESC")
    List<Model> findByModeltypeAndFilter(Integer modelType, String validFilter, Sort sort);

    @Query(value = "SELECT m FROM Model m WHERE m.modeltype = ?1 AND m.description IS NOT NULL ORDER BY m.created DESC")
    List<Model> findByModeltype(Integer modelType, Sort sort);
}

