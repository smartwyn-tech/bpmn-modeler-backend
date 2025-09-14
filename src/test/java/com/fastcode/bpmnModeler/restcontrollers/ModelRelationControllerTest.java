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
import com.fastcode.bpmnModeler.application.modelrelation.ModelRelationAppService;
import com.fastcode.bpmnModeler.application.modelrelation.dto.*;
import com.fastcode.bpmnModeler.domain.modelrelation.IModelRelationRepository;
import com.fastcode.bpmnModeler.domain.modelrelation.ModelRelation;

import com.fastcode.bpmnModeler.domain.model.IModelRepository;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.application.model.ModelAppService;
import com.fastcode.bpmnModeler.application.model.ModelAppService;
import com.fastcode.bpmnModeler.DatabaseContainerConfig;
import com.fastcode.bpmnModeler.domain.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = "spring.profiles.active=test")
public class ModelRelationControllerTest extends DatabaseContainerConfig{
	
	@Autowired
	protected SortHandlerMethodArgumentResolver sortArgumentResolver;

	@Autowired
	@Qualifier("modelRelationRepository") 
	protected IModelRelationRepository modelRelation_repository;
	
	@Autowired
	@Qualifier("modelRepository") 
	protected IModelRepository modelRepository;
	

	@SpyBean
	@Qualifier("modelRelationAppService")
	protected ModelRelationAppService modelRelationAppService;
	
    @SpyBean
    @Qualifier("modelAppService")
	protected ModelAppService  modelAppService;
	
	@SpyBean
	protected LoggingHelper logHelper;

	@SpyBean
	protected Environment env;

	@Mock
	protected Logger loggerMock;

	protected ModelRelation modelRelation;

	protected MockMvc mvc;
	
	@Autowired
	EntityManagerFactory emf;
	
    static EntityManagerFactory emfs;
    
    static int relationCount = 10;
    static int yearCount = 1971;
    static int dayCount = 10;
	private BigDecimal bigdec = new BigDecimal(1.2);
    
	int countModel = 10;
	
	@PostConstruct
	public void init() {
	emfs = emf;
	}

	@AfterClass
	public static void cleanup() {
		EntityManager em = emfs.createEntityManager();
		em.getTransaction().begin();
		em.createNativeQuery("truncate table modeler.model_relation CASCADE").executeUpdate();
		em.createNativeQuery("truncate table modeler.model CASCADE").executeUpdate();
		em.getTransaction().commit();
	}
	
	public Model createModelEntity() {
	
		if(countModel>60) {
			countModel = 10;
		}

		if(dayCount>=31) {
			dayCount = 10;
			yearCount++;
		}
		
		Model modelEntity = new Model();
		
  		modelEntity.setComment(String.valueOf(relationCount));
		modelEntity.setCreated(SearchUtils.stringToDate(yearCount+"-09-"+dayCount+" 05:25:22"));
  		modelEntity.setDescription(String.valueOf(relationCount));
  		modelEntity.setId(String.valueOf(relationCount));
  		modelEntity.setKey(String.valueOf(relationCount));
		modelEntity.setLastupdated(SearchUtils.stringToDate(yearCount+"-09-"+dayCount+" 05:25:22"));
  		modelEntity.setModeleditorjson(String.valueOf(relationCount));
		modelEntity.setModeltype(relationCount);
  		modelEntity.setName(String.valueOf(relationCount));
  		modelEntity.setTenantid(String.valueOf(relationCount));
		modelEntity.setVersion(relationCount);
		modelEntity.setVersiono(0L);
		relationCount++;
		if(!modelRepository.findAll().contains(modelEntity))
		{
			 modelEntity = modelRepository.save(modelEntity);
		}
		countModel++;
	    return modelEntity;
	}

	public ModelRelation createEntity() {
		Model model = createModelEntity();
		Model model2 = createModelEntity();
	
		ModelRelation modelRelationEntity = new ModelRelation();
		modelRelationEntity.setId("1");
		modelRelationEntity.setType("1");
		modelRelationEntity.setVersiono(0L);
		modelRelationEntity.setModel(model);
		modelRelationEntity.setParentModel(model2);
		
		return modelRelationEntity;
	}
	public ModelRelation createNewEntity() {
		ModelRelation modelRelation = new ModelRelation();
		modelRelation.setId("3");
		modelRelation.setType("3");
		
		return modelRelation;
	}
	
	public ModelRelation createUpdateEntity() {
		ModelRelation modelRelation = new ModelRelation();
		modelRelation.setId("4");
		modelRelation.setType("4");
		
		return modelRelation;
	}
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
    
		final ModelRelationController modelRelationController = new ModelRelationController(modelRelationAppService);
		when(logHelper.getLogger()).thenReturn(loggerMock);
		doNothing().when(loggerMock).error(anyString());

		this.mvc = MockMvcBuilders.standaloneSetup(modelRelationController)
				.setCustomArgumentResolvers(sortArgumentResolver)
				.setControllerAdvice()
				.build();
	}

	@Before
	public void initTest() {

		modelRelation= createEntity();
		List<ModelRelation> list= modelRelation_repository.findAll();
		if(!list.contains(modelRelation)) {
			modelRelation=modelRelation_repository.save(modelRelation);
		}

	}
	
	@Test
	public void FindById_IdIsValid_ReturnStatusOk() throws Exception {
	
		mvc.perform(get("/modelRelation/" + modelRelation.getId()+"/")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}  

	@Test
	public void FindById_IdIsNotValid_ReturnStatusNotFound() {

		 org.assertj.core.api.Assertions.assertThatThrownBy(() -> mvc.perform(get("/modelRelation/999")
				.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())).hasCause(new EntityNotFoundException("Not found"));

	}
	
	@Test
	public void GetModel_searchIsNotEmptyAndPropertyIsValid_ReturnList() throws Exception {
	
	   mvc.perform(get("/modelRelation/" + modelRelation.getId()+ "/model")
				.contentType(MediaType.APPLICATION_JSON))
	    		  .andExpect(status().isOk());
	}  
	
	
	@Test
	public void GetModel2_IdIsNotEmptyAndIdDoesNotExist_ReturnNotFound() {
  
	   org.assertj.core.api.Assertions.assertThatThrownBy(() ->  mvc.perform(get("/modelRelation/999/model2")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())).hasCause(new EntityNotFoundException("Not found"));
	
	}    
	
	@Test
	public void GetModel2_searchIsNotEmptyAndPropertyIsValid_ReturnList() throws Exception {
	
	   mvc.perform(get("/modelRelation/" + modelRelation.getId()+ "/model")
				.contentType(MediaType.APPLICATION_JSON))
	    		  .andExpect(status().isOk());
	}  
	
    
}

