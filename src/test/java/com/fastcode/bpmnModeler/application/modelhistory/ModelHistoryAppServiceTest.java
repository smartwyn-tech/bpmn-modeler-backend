package com.fastcode.bpmnModeler.application.modelhistory;

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

import com.fastcode.bpmnModeler.domain.modelhistory.*;
import com.fastcode.bpmnModeler.commons.search.*;
import com.fastcode.bpmnModeler.application.modelhistory.dto.*;
import com.fastcode.bpmnModeler.domain.modelhistory.QModelHistory;
import com.fastcode.bpmnModeler.domain.modelhistory.ModelHistory;

import com.fastcode.bpmnModeler.domain.model.Model;
import com.fastcode.bpmnModeler.domain.model.IModelRepository;
import com.fastcode.bpmnModeler.commons.logging.LoggingHelper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import java.time.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class ModelHistoryAppServiceTest {

	@InjectMocks
	@Spy
	protected ModelHistoryAppService _appService;
	@Mock
	protected IModelHistoryRepository _modelHistoryRepository;
	
    @Mock
	protected IModelRepository _modelRepository;

	@Mock
	protected IModelHistoryMapper _mapper;

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

}
