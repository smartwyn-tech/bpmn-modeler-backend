package com.fastcode.bpmnModeler.application.modelrelation;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fastcode.bpmnModeler.domain.modelrelation.*;
import com.fastcode.bpmnModeler.commons.search.*;
import com.fastcode.bpmnModeler.application.modelrelation.dto.*;
import com.fastcode.bpmnModeler.domain.modelrelation.QModelRelation;
import com.fastcode.bpmnModeler.domain.modelrelation.ModelRelation;

import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.domain.model.IModelRepository;
import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.domain.model.IModelRepository;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import java.time.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class ModelRelationAppServiceTest {

	@InjectMocks
	@Spy
	protected ModelRelationAppService _appService;
	@Mock
	protected IModelRelationRepository _modelRelationRepository;
	
    @Mock
	protected IModelRepository _modelRepository;

	@Mock
	protected IModelRelationMapper _mapper;

	@Mock
	protected Logger loggerMock;

	@Mock
	protected LoggingHelper logHelper;
	
    protected static String ID="15";
	 
	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(_appService);
		when(logHelper.getLogger()).thenReturn(loggerMock);
		doNothing().when(loggerMock).error(anyString());
	}
	
	@Test
	public void findModelRelationById_IdIsNotNullAndIdDoesNotExist_ReturnNull() {
		Optional<ModelRelation> nullOptional = Optional.ofNullable(null);
		Mockito.when(_modelRelationRepository.findById(anyString())).thenReturn(nullOptional);
		Assertions.assertThat(_appService.findById(ID.toString())).isEqualTo(null);
	}
	
	@Test
	public void findModelRelationById_IdIsNotNullAndIdExists_ReturnModelRelation() {

		ModelRelation modelRelation = mock(ModelRelation.class);
		Optional<ModelRelation> modelRelationOptional = Optional.of((ModelRelation) modelRelation);
		Mockito.when(_modelRelationRepository.findById(anyString())).thenReturn(modelRelationOptional);
		
	    Assertions.assertThat(_appService.findById(ID.toString())).isEqualTo(_mapper.modelRelationToFindModelRelationByIdOutput(modelRelation));
	}
	
	
	@Test 
    public void createModelRelation_ModelRelationIsNotNullAndModelRelationDoesNotExist_StoreModelRelation() { 
 
        ModelRelation modelRelationEntity = mock(ModelRelation.class); 
    	CreateModelRelationInput modelRelationInput = new CreateModelRelationInput();
		
        Model model = mock(Model.class);
		Optional<Model> modelOptional = Optional.of((Model) model);
        modelRelationInput.setModelId("15");
		
        Mockito.when(_modelRepository.findById(any(String.class))).thenReturn(modelOptional);
        
		
        Model model2 = mock(Model.class);
		Optional<Model> model2Optional = Optional.of((Model) model2);
        modelRelationInput.setParentModelId("15");
		
        Mockito.when(_modelRepository.findById(any(String.class))).thenReturn(model2Optional);
        
		
        Mockito.when(_mapper.createModelRelationInputToModelRelation(any(CreateModelRelationInput.class))).thenReturn(modelRelationEntity); 
        Mockito.when(_modelRelationRepository.save(any(ModelRelation.class))).thenReturn(modelRelationEntity);

	   	Assertions.assertThat(_appService.create(modelRelationInput)).isEqualTo(_mapper.modelRelationToCreateModelRelationOutput(modelRelationEntity));

    } 
    @Test
	public void createModelRelation_ModelRelationIsNotNullAndModelRelationDoesNotExistAndChildIsNullAndChildIsNotMandatory_StoreModelRelation() {

		ModelRelation modelRelation = mock(ModelRelation.class);
		CreateModelRelationInput modelRelationInput = mock(CreateModelRelationInput.class);
		
		
		Mockito.when(_mapper.createModelRelationInputToModelRelation(any(CreateModelRelationInput.class))).thenReturn(modelRelation);
		Mockito.when(_modelRelationRepository.save(any(ModelRelation.class))).thenReturn(modelRelation);
	    Assertions.assertThat(_appService.create(modelRelationInput)).isEqualTo(_mapper.modelRelationToCreateModelRelationOutput(modelRelation)); 
	}
	
    @Test
	public void updateModelRelation_ModelRelationIsNotNullAndModelRelationDoesNotExistAndChildIsNullAndChildIsNotMandatory_ReturnUpdatedModelRelation() {

		ModelRelation modelRelation = mock(ModelRelation.class);
		UpdateModelRelationInput modelRelationInput = mock(UpdateModelRelationInput.class);
		
		Optional<ModelRelation> modelRelationOptional = Optional.of((ModelRelation) modelRelation);
		Mockito.when(_modelRelationRepository.findById(anyString())).thenReturn(modelRelationOptional);
		
		Mockito.when(_mapper.updateModelRelationInputToModelRelation(any(UpdateModelRelationInput.class))).thenReturn(modelRelation);
		Mockito.when(_modelRelationRepository.save(any(ModelRelation.class))).thenReturn(modelRelation);
		Assertions.assertThat(_appService.update(ID,modelRelationInput)).isEqualTo(_mapper.modelRelationToUpdateModelRelationOutput(modelRelation));
	}
	
		
	@Test
	public void updateModelRelation_ModelRelationIdIsNotNullAndIdExists_ReturnUpdatedModelRelation() {

		ModelRelation modelRelationEntity = mock(ModelRelation.class);
		UpdateModelRelationInput modelRelation= mock(UpdateModelRelationInput.class);
		
		Optional<ModelRelation> modelRelationOptional = Optional.of((ModelRelation) modelRelationEntity);
		Mockito.when(_modelRelationRepository.findById(anyString())).thenReturn(modelRelationOptional);
	 		
		Mockito.when(_mapper.updateModelRelationInputToModelRelation(any(UpdateModelRelationInput.class))).thenReturn(modelRelationEntity);
		Mockito.when(_modelRelationRepository.save(any(ModelRelation.class))).thenReturn(modelRelationEntity);
		Assertions.assertThat(_appService.update(ID,modelRelation)).isEqualTo(_mapper.modelRelationToUpdateModelRelationOutput(modelRelationEntity));
	}
    
	@Test
	public void deleteModelRelation_ModelRelationIsNotNullAndModelRelationExists_ModelRelationRemoved() {

		ModelRelation modelRelation = mock(ModelRelation.class);
		Optional<ModelRelation> modelRelationOptional = Optional.of((ModelRelation) modelRelation);
		Mockito.when(_modelRelationRepository.findById(anyString())).thenReturn(modelRelationOptional);
 		
		_appService.delete(ID); 
		verify(_modelRelationRepository).delete(modelRelation);
	}
	
	@Test
	public void find_ListIsEmpty_ReturnList() throws Exception {

		List<ModelRelation> list = new ArrayList<>();
		Page<ModelRelation> foundPage = new PageImpl(list);
		Pageable pageable = mock(Pageable.class);
		List<FindModelRelationByIdOutput> output = new ArrayList<>();
		SearchCriteria search= new SearchCriteria();

		Mockito.when(_appService.search(any(SearchCriteria.class))).thenReturn(new BooleanBuilder());
		Mockito.when(_modelRelationRepository.findAll(any(Predicate.class),any(Pageable.class))).thenReturn(foundPage);
		Assertions.assertThat(_appService.find(search, pageable)).isEqualTo(output);
	}
	
	@Test
	public void find_ListIsNotEmpty_ReturnList() throws Exception {

		List<ModelRelation> list = new ArrayList<>();
		ModelRelation modelRelation = mock(ModelRelation.class);
		list.add(modelRelation);
    	Page<ModelRelation> foundPage = new PageImpl(list);
		Pageable pageable = mock(Pageable.class);
		List<FindModelRelationByIdOutput> output = new ArrayList<>();
        SearchCriteria search= new SearchCriteria();

		output.add(_mapper.modelRelationToFindModelRelationByIdOutput(modelRelation));
		
		Mockito.when(_appService.search(any(SearchCriteria.class))).thenReturn(new BooleanBuilder());
    	Mockito.when(_modelRelationRepository.findAll(any(Predicate.class),any(Pageable.class))).thenReturn(foundPage);
		Assertions.assertThat(_appService.find(search, pageable)).isEqualTo(output);
	}
	
	@Test
	public void searchKeyValuePair_PropertyExists_ReturnBooleanBuilder() {
		QModelRelation modelRelation = QModelRelation.modelRelationEntity;
	    SearchFields searchFields = new SearchFields();
		searchFields.setOperator("equals");
		searchFields.setSearchValue("xyz");
	    Map<String,SearchFields> map = new HashMap<>();
        map.put("type",searchFields);
		Map<String,String> searchMap = new HashMap<>();
        searchMap.put("xyz",String.valueOf(ID));
		BooleanBuilder builder = new BooleanBuilder();
        builder.and(modelRelation.type.eq("xyz"));
		Assertions.assertThat(_appService.searchKeyValuePair(modelRelation,map,searchMap)).isEqualTo(builder);
	}
	
	@Test (expected = Exception.class)
	public void checkProperties_PropertyDoesNotExist_ThrowException() throws Exception {
		List<String> list = new ArrayList<>();
		list.add("xyz");
		_appService.checkProperties(list);
	}
	
	@Test
	public void checkProperties_PropertyExists_ReturnNothing() throws Exception {
		List<String> list = new ArrayList<>();
        list.add("type");
		_appService.checkProperties(list);
	}
	
	@Test
	public void search_SearchIsNotNullAndSearchContainsCaseThree_ReturnBooleanBuilder() throws Exception {
	
		Map<String,SearchFields> map = new HashMap<>();
		QModelRelation modelRelation = QModelRelation.modelRelationEntity;
		List<SearchFields> fieldsList= new ArrayList<>();
		SearchFields fields=new SearchFields();
		SearchCriteria search= new SearchCriteria();
		search.setType(3);
		search.setValue("xyz");
		search.setOperator("equals");
        fields.setFieldName("type");
        fields.setOperator("equals");
		fields.setSearchValue("xyz");
        fieldsList.add(fields);
        search.setFields(fieldsList);
		BooleanBuilder builder = new BooleanBuilder();
        builder.or(modelRelation.type.eq("xyz"));
        Mockito.doNothing().when(_appService).checkProperties(any(List.class));
		Mockito.doReturn(builder).when(_appService).searchKeyValuePair(any(QModelRelation.class), any(HashMap.class), any(HashMap.class));
        
		Assertions.assertThat(_appService.search(search)).isEqualTo(builder);
	}
	
	@Test
	public void search_StringIsNull_ReturnNull() throws Exception {

		Assertions.assertThat(_appService.search(null)).isEqualTo(null);
	}
   
    //Model
	@Test
	public void GetModel2_IfModelRelationIdAndModelIdIsNotNullAndModelRelationExists_ReturnModel() {
		ModelRelation modelRelation = mock(ModelRelation.class);
		Optional<ModelRelation> modelRelationOptional = Optional.of((ModelRelation) modelRelation);
		Model modelEntity = mock(Model.class);

		Mockito.when(_modelRelationRepository.findById(anyString())).thenReturn(modelRelationOptional);

		Mockito.when(modelRelation.getModel2()).thenReturn(modelEntity);
		Assertions.assertThat(_appService.getModel2(ID)).isEqualTo(_mapper.model2ToGetModelOutput(modelEntity, modelRelation));
	}

	@Test 
	public void GetModel2_IfModelRelationIdAndModelIdIsNotNullAndModelRelationDoesNotExist_ReturnNull() {
		Optional<ModelRelation> nullOptional = Optional.ofNullable(null);;
		Mockito.when(_modelRelationRepository.findById(anyString())).thenReturn(nullOptional);
		Assertions.assertThat(_appService.getModel2(ID)).isEqualTo(null);
	}

}
