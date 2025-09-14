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
import com.fastcode.bpmnModeler.application.modelhistory.ModelHistoryAppService;
import com.fastcode.bpmnModeler.application.modelhistory.dto.*;
import com.fastcode.bpmnModeler.domain.modelhistory.IModelHistoryRepository;
import com.fastcode.bpmnModeler.domain.modelhistory.ModelHistory;

import com.fastcode.bpmnModeler.domain.model.IModelRepository;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.application.model.ModelAppService;
import com.fastcode.bpmnModeler.DatabaseContainerConfig;
import com.fastcode.bpmnModeler.domain.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = "spring.profiles.active=test")
public class ModelHistoryControllerTest extends DatabaseContainerConfig{
	
	@Autowired
	protected SortHandlerMethodArgumentResolver sortArgumentResolver;

	@Autowired
	@Qualifier("modelHistoryRepository") 
	protected IModelHistoryRepository modelHistory_repository;
	
	@Autowired
	@Qualifier("modelRepository") 
	protected IModelRepository modelRepository;
	

	@SpyBean
	@Qualifier("modelHistoryAppService")
	protected ModelHistoryAppService modelHistoryAppService;
	
    @SpyBean
    @Qualifier("modelAppService")
	protected ModelAppService  modelAppService;
	
	@SpyBean
	protected LoggingHelper logHelper;

	@SpyBean
	protected Environment env;

	@Mock
	protected Logger loggerMock;

	protected ModelHistory modelHistory;

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
		em.createNativeQuery("truncate table modeler.model_history CASCADE").executeUpdate();
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

	public ModelHistory createEntity() {
		Model model = createModelEntity();
	
		ModelHistory modelHistoryEntity = new ModelHistory();
		modelHistoryEntity.setComment("1");
    	modelHistoryEntity.setCreated(SearchUtils.stringToDate("1996-09-01 09:15:22"));
		modelHistoryEntity.setDescription("1");
		modelHistoryEntity.setId("1");
		modelHistoryEntity.setKey("1");
    	modelHistoryEntity.setLastupdated(SearchUtils.stringToDate("1996-09-01 09:15:22"));
		modelHistoryEntity.setModeleditorjson("1");
		modelHistoryEntity.setModeltype(1);
		modelHistoryEntity.setName("1");
    	modelHistoryEntity.setRemovaldate(SearchUtils.stringToLocalDateTime("1996-09-01 09:15:22"));
		modelHistoryEntity.setTenantid("1");
		modelHistoryEntity.setVersion(1);
		modelHistoryEntity.setVersiono(0L);
		modelHistoryEntity.setModel(model);
		
		return modelHistoryEntity;
	}
	public ModelHistory createNewEntity() {
		ModelHistory modelHistory = new ModelHistory();
		modelHistory.setComment("3");
    	modelHistory.setCreated(SearchUtils.stringToDate("1996-08-11 05:35:22"));
		modelHistory.setDescription("3");
		modelHistory.setId("3");
		modelHistory.setKey("3");
    	modelHistory.setLastupdated(SearchUtils.stringToDate("1996-08-11 05:35:22"));
		modelHistory.setModeleditorjson("3");
		modelHistory.setModeltype(3);
		modelHistory.setName("3");
    	modelHistory.setRemovaldate(SearchUtils.stringToLocalDateTime("1996-08-11 05:35:22"));
		modelHistory.setTenantid("3");
		modelHistory.setVersion(3);
		
		return modelHistory;
	}
	
	public ModelHistory createUpdateEntity() {
		ModelHistory modelHistory = new ModelHistory();
		modelHistory.setComment("4");
    	modelHistory.setCreated(SearchUtils.stringToDate("1996-09-09 05:45:22"));
		modelHistory.setDescription("4");
		modelHistory.setId("4");
		modelHistory.setKey("4");
    	modelHistory.setLastupdated(SearchUtils.stringToDate("1996-09-09 05:45:22"));
		modelHistory.setModeleditorjson("4");
		modelHistory.setModeltype(4);
		modelHistory.setName("4");
    	modelHistory.setRemovaldate(SearchUtils.stringToLocalDateTime("1996-09-09 05:45:22"));
		modelHistory.setTenantid("4");
		modelHistory.setVersion(4);
		
		return modelHistory;
	}
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
    
		final ModelHistoryController modelHistoryController = new ModelHistoryController(modelHistoryAppService,
		logHelper,env);
		when(logHelper.getLogger()).thenReturn(loggerMock);
		doNothing().when(loggerMock).error(anyString());

		this.mvc = MockMvcBuilders.standaloneSetup(modelHistoryController)
				.setCustomArgumentResolvers(sortArgumentResolver)
				.setControllerAdvice()
				.build();
	}

	@Before
	public void initTest() {

		modelHistory= createEntity();
		List<ModelHistory> list= modelHistory_repository.findAll();
		if(!list.contains(modelHistory)) {
			modelHistory=modelHistory_repository.save(modelHistory);
		}

	}
	
	@Test
	public void FindById_IdIsValid_ReturnStatusOk() throws Exception {
	
		mvc.perform(get("/modelHistory/" + modelHistory.getId()+"/")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}  

	@Test
	public void FindById_IdIsNotValid_ReturnStatusNotFound() {

		 org.assertj.core.api.Assertions.assertThatThrownBy(() -> mvc.perform(get("/modelHistory/999")
				.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())).hasCause(new EntityNotFoundException("Not found"));

	}

	@Test
	public void FindAll_SearchIsNotNullAndPropertyIsValid_ReturnStatusOk() throws Exception {

		mvc.perform(get("/modelHistory?search=id[equals]=1&limit=10&offset=1")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}
		
	
	@Test
	public void GetModel_IdIsNotEmptyAndIdDoesNotExist_ReturnNotFound() {
  
	   org.assertj.core.api.Assertions.assertThatThrownBy(() ->  mvc.perform(get("/modelHistory/999/model")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())).hasCause(new EntityNotFoundException("Not found"));
	
	}    
	
	@Test
	public void GetModel_searchIsNotEmptyAndPropertyIsValid_ReturnList() throws Exception {
	
	   mvc.perform(get("/modelHistory/" + modelHistory.getId()+ "/model")
				.contentType(MediaType.APPLICATION_JSON))
	    		  .andExpect(status().isOk());
	}  
	
    
}

