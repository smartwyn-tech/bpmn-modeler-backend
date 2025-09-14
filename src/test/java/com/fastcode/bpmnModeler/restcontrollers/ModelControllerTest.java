package com.fastcode.bpmnModeler.restcontrollers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import org.springframework.core.env.Environment;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import com.fastcode.bpmnModeler.commons.search.SearchUtils;
import com.fastcode.bpmnModeler.application.model.ModelAppService;
import com.fastcode.bpmnModeler.application.model.dto.*;
import com.fastcode.bpmnModeler.domain.model.IModelRepository;
import com.fastcode.bpmnModeler.domain.model.Model;

import com.fastcode.bpmnModeler.application.modelhistory.ModelHistoryAppService;
import com.fastcode.bpmnModeler.application.modelrelation.ModelRelationAppService;
import com.fastcode.bpmnModeler.application.modelrelation.ModelRelationAppService;
import com.fastcode.bpmnModeler.DatabaseContainerConfig;
import com.fastcode.bpmnModeler.domain.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = "spring.profiles.active=test")
public class ModelControllerTest extends DatabaseContainerConfig{
	
	@Autowired
	protected SortHandlerMethodArgumentResolver sortArgumentResolver;

	@Autowired
	@Qualifier("modelRepository") 
	protected IModelRepository model_repository;
	

	@SpyBean
	@Qualifier("modelAppService")
	protected ModelAppService modelAppService;
	
    @SpyBean
    @Qualifier("modelHistoryAppService")
	protected ModelHistoryAppService  modelHistoryAppService;
	
    @SpyBean
    @Qualifier("modelRelationAppService")
	protected ModelRelationAppService  modelRelationAppService;
	
	@SpyBean
	protected LoggingHelper logHelper;

	@SpyBean
	protected Environment env;

	@Mock
	protected Logger loggerMock;

	protected Model model;

	protected MockMvc mvc;
	
	@Autowired
	EntityManagerFactory emf;
	
    static EntityManagerFactory emfs;
    
    static int relationCount = 10;
    static int yearCount = 1971;
    static int dayCount = 10;
	private BigDecimal bigdec = new BigDecimal(1.2);
    
	@PostConstruct
	public void init() {
	emfs = emf;
	}

	@AfterClass
	public static void cleanup() {
		EntityManager em = emfs.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("truncate table modeler.model CASCADE").executeUpdate();
		em.getTransaction().commit();
	}
	

	public Model createEntity() {
	
		Model modelEntity = new Model();
		modelEntity.setComment("1");
    	modelEntity.setCreated(SearchUtils.stringToDate("1996-09-01 09:15:22"));
		modelEntity.setDescription("1");
		modelEntity.setId("1");
		modelEntity.setKey("1");
    	modelEntity.setLastupdated(SearchUtils.stringToDate("1996-09-01 09:15:22"));
		modelEntity.setModeleditorjson("1");
		modelEntity.setModeltype(1);
		modelEntity.setName("1");
		modelEntity.setTenantid("1");
		modelEntity.setVersion(1);
		modelEntity.setVersiono(0L);
		
		return modelEntity;
	}

	@Test
	public void FindAll_SearchIsNotNullAndPropertyIsValid_ReturnStatusOk() throws Exception {

		mvc.perform(get("/model?search=id[equals]=1&limit=10&offset=1")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}
		
	

}

