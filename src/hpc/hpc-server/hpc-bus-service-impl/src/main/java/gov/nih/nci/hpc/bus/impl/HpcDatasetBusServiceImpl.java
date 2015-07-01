/**
 * HpcDatasetBusServiceImpl.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.bus.impl;

import gov.nih.nci.hpc.bus.HpcDatasetBusService;
import gov.nih.nci.hpc.service.HpcDatasetService;
import gov.nih.nci.hpc.service.HpcUserService;
import gov.nih.nci.hpc.service.HpcDataTransferService;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetRegistrationDTO;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetDTO;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetCollectionDTO;
import gov.nih.nci.hpc.domain.model.HpcDataset;
import gov.nih.nci.hpc.domain.model.HpcUser;
import gov.nih.nci.hpc.domain.dataset.HpcDataTransferReport;
import gov.nih.nci.hpc.domain.dataset.HpcFileSet;
import gov.nih.nci.hpc.domain.dataset.HpcFile;
import gov.nih.nci.hpc.domain.dataset.HpcFileUploadRequest;
import gov.nih.nci.hpc.domain.dataset.HpcDatasetUserAssociation;
import gov.nih.nci.hpc.domain.user.HpcDataTransferAccount;
import gov.nih.nci.hpc.domain.error.HpcErrorType;
import gov.nih.nci.hpc.domain.error.HpcRequestRejectReason;
import gov.nih.nci.hpc.exception.HpcException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>
 * HPC Dataset Business Service Implementation.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public class HpcDatasetBusServiceImpl implements HpcDatasetBusService
{         
    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//

    // Application service instances.
    private HpcDatasetService datasetService = null;
    private HpcUserService userService = null;
    private HpcDataTransferService dataTransferService = null;
    
    // The logger instance.
	private final Logger logger = 
			             LoggerFactory.getLogger(this.getClass().getName());
	
    //---------------------------------------------------------------------//
    // Constructors
    //---------------------------------------------------------------------//
	
    /**
     * Default Constructor.
     * 
     * @throws HpcException Constructor is disabled.
     */
    private HpcDatasetBusServiceImpl() throws HpcException
    {
    	throw new HpcException("Constructor Disabled",
                               HpcErrorType.SPRING_CONFIGURATION_ERROR);
    }   
    
    /**
     * Constructor for Spring Dependency Injection.
     * 
     * @param dataService The dataset application service.
     * @param userService The user application service.
     * @param dataTransferService The data transfer application service.
     * 
     * @throws HpcException If any application service provided is null.
     */
    private HpcDatasetBusServiceImpl(
    		          HpcDatasetService datasetService,
    		          HpcUserService userService,
    		          HpcDataTransferService dataTransferService)
                      throws HpcException
    {
    	if(datasetService == null || userService == null ||
    	   dataTransferService == null) {
     	   throw new HpcException("Null App Service(s) instance",
     			                  HpcErrorType.SPRING_CONFIGURATION_ERROR);
     	}
    	
    	this.datasetService = datasetService;
    	this.userService = userService;
    	this.dataTransferService = dataTransferService;
    }  
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // HpcDatasetBusService Interface Implementation
    //---------------------------------------------------------------------//  
    
    @Override
    public String registerDataset(HpcDatasetRegistrationDTO datasetRegistrationDTO)  
    		                     throws HpcException
    {
    	logger.info("Invoking registerDataset(HpcDatasetDTO): " + 
                    datasetRegistrationDTO);
    	
    	// Input validation.
    	if(datasetRegistrationDTO == null) {
    	   throw new HpcException("Null HpcDatasetRegistrationDTO",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Validate the associated users with this dataset have valid NIH 
    	// account registered with HPC. In addition, validate the registrator
    	// has a valid data transfer account.
    	validateAssociatedUsers(datasetRegistrationDTO.getUploadRequests());
    	
    	// Add the dataset to the managed collection.
    	String datasetId = 
    		   datasetService.add(
    				  datasetRegistrationDTO.getName(), 
    				  datasetRegistrationDTO.getDescription(),
    				  datasetRegistrationDTO.getComments(),
    				  datasetRegistrationDTO.getUploadRequests());
    	logger.info("Registered dataset id = " + datasetId);
    	
    	// Upload all files in registration request.
    	for(HpcFileUploadRequest uploadRequest : 
    		datasetRegistrationDTO.getUploadRequests()) { 
    		// Get the data transfer account of the registrator.
    		HpcDataTransferAccount dataTransferAccount = 
    		   userService.get(
    			   uploadRequest.getMetadata().getRegistratorNihUserId()).
    			                               getDataTransferAccount();
        	
        	// Submit data transfer request for this file.
    		logger.info("Submiting Data Transfer Request: "+ 
        	            uploadRequest.getLocations());
    		HpcDataTransferReport hpcDataTransferReport = 
    				dataTransferService.transferDataset(
    				                    uploadRequest.getLocations(), 
    				                    dataTransferAccount.getUsername(), 
    				                    dataTransferAccount.getPassword());
    		logger.info("Data Transfer status : " + hpcDataTransferReport.getTaskID());
    	}
    	
    	return datasetId;
    }
    
    @Override
    public HpcDatasetDTO getDataset(String id) throws HpcException
    {
    	logger.info("Invoking getDataset(String id): " + id);
    	
    	// Input validation.
    	if(id == null) {
    	   throw new HpcException("Null Dataset ID",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Get the managed dataset domain object and return it as DTO.
    	return toDTO(datasetService.get(id));
    }
    
    @Override
    public HpcDatasetCollectionDTO getDatasets(String userId, 
                                      HpcDatasetUserAssociation association) 
                                      throws HpcException
    {
    	// Input validation.
    	if(userId == null || association == null) {
    	   throw new HpcException("Null user-id or association",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Get the managed dataset collection.
    	List<HpcDataset> datasets = datasetService.get(userId, association);
    	if(datasets == null || datasets.size() == 0) {
    	   return null;
    	}
    	
    	// Map it to the DTO.
    	HpcDatasetCollectionDTO datasetCollectionDTO = 
    			                       new HpcDatasetCollectionDTO();
    	for(HpcDataset dataset : datasets) {
    		datasetCollectionDTO.getHpcDatasetDTO().add(toDTO(dataset));
    	}
    	
    	return datasetCollectionDTO;
    }
    
    //---------------------------------------------------------------------//
    // Helper Methods
    //---------------------------------------------------------------------//  
	
    /**
     * Create a dataset DTO from a domain object
     * 
     * @param dataset the domain object.
     *
     * @return The DTO.
     */
    private HpcDatasetDTO toDTO(HpcDataset dataset)
    {
    	if(dataset == null) {
     	   return null;
     	}
    	
    	HpcDatasetDTO datasetDTO = new HpcDatasetDTO();
    	datasetDTO.setId(dataset.getId());
    	datasetDTO.setFileSet(dataset.getFileSet());
    	
    	return datasetDTO;
    }  
    
    /**
     * Validate the users associated with the upload request are valid.
     * The associated users are - creator, registrator and primary investigator.
     * 
     * @param uploadRequests The upload requests to validate. 
     *
     * @throws HpcException if any validation error found.
     */
    private void validateAssociatedUsers(
    		             List<HpcFileUploadRequest> uploadRequests)
    		             throws HpcException
    {
    	if(uploadRequests == null) {
    	   return;
    	}
    	
    	for(HpcFileUploadRequest uploadRequest : uploadRequests) {
    		if(uploadRequest.getMetadata() == null) {
    		   continue;	
    		}
    		
    		validateUser(uploadRequest.getMetadata().getPrimaryInvestigatorNihUserId(),
    				     HpcDatasetUserAssociation.PRIMARY_INVESTIGATOR);
    		validateUser(uploadRequest.getMetadata().getCreatorNihUserId(),
				         HpcDatasetUserAssociation.CREATOR);
    		validateUser(uploadRequest.getMetadata().getRegistratorNihUserId(),
			             HpcDatasetUserAssociation.REGISTRATOR);
    	}
    }
    
    /**
     * Validate a user is registered with HPC
     * 
     * @param nihUserId The NIH User ID.
     * @param userAssociation The user's association to the dataset.
     *
     * @throws HpcException if the user is not registered with HPC.
     */
    private void validateUser(String nihUserId, 
    		                  HpcDatasetUserAssociation userAssociation)
    		                 throws HpcException
    {
    	if(userService.get(nihUserId) == null) {
    	   throw new HpcException("Could not find "+ userAssociation +
    				               " user with nihUserID = " + nihUserId,
    		                       HpcRequestRejectReason.INVALID_NIH_ACCOUNT);	
    	}
    }
}

 