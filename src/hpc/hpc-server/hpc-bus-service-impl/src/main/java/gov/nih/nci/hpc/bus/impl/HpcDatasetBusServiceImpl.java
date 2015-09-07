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

import static gov.nih.nci.hpc.bus.impl.HpcBusServiceUtil.associateProjectMetadataLock;
import static gov.nih.nci.hpc.bus.impl.HpcBusServiceUtil.cloneJAXB;
import static gov.nih.nci.hpc.bus.impl.HpcBusServiceUtil.updateMetadataLock;
import gov.nih.nci.hpc.bus.HpcDatasetBusService;
import gov.nih.nci.hpc.domain.dataset.HpcDataTransferReport;
import gov.nih.nci.hpc.domain.dataset.HpcDataTransferRequest;
import gov.nih.nci.hpc.domain.dataset.HpcDataTransferStatus;
import gov.nih.nci.hpc.domain.dataset.HpcDatasetUserAssociation;
import gov.nih.nci.hpc.domain.dataset.HpcFile;
import gov.nih.nci.hpc.domain.dataset.HpcFileUploadRequest;
import gov.nih.nci.hpc.domain.error.HpcErrorType;
import gov.nih.nci.hpc.domain.error.HpcRequestRejectReason;
import gov.nih.nci.hpc.domain.metadata.HpcFileMetadata;
import gov.nih.nci.hpc.domain.metadata.HpcFilePrimaryMetadata;
import gov.nih.nci.hpc.domain.model.HpcDataset;
import gov.nih.nci.hpc.domain.model.HpcProject;
import gov.nih.nci.hpc.domain.model.HpcUser;
import gov.nih.nci.hpc.domain.user.HpcDataTransferAccount;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetAddFilesDTO;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetAddMetadataItemsDTO;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetAssociateFileProjectsDTO;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetCollectionDTO;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetDTO;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetRegistrationDTO;
import gov.nih.nci.hpc.dto.dataset.HpcDatasetUpdateFilePrimaryMetadataDTO;
import gov.nih.nci.hpc.dto.dataset.HpcFileDTO;
import gov.nih.nci.hpc.dto.dataset.HpcFilePrimaryMetadataDTO;
import gov.nih.nci.hpc.dto.project.HpcProjectCollectionDTO;
import gov.nih.nci.hpc.dto.project.HpcProjectDTO;
import gov.nih.nci.hpc.exception.HpcException;
import gov.nih.nci.hpc.service.HpcDataTransferService;
import gov.nih.nci.hpc.service.HpcDatasetService;
import gov.nih.nci.hpc.service.HpcProjectService;
import gov.nih.nci.hpc.service.HpcUserService;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * HPC Dataset Business Service Implementation.
 * </p>
 *
 * @author <a href="mailto:mahidhar.narra@nih.gov">Mahidhar Narra</a>
 * @version $Id$
 */

public class HpcDatasetBusServiceImpl implements HpcDatasetBusService
{         
    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//

    // Application service instances.
	
	@Autowired
    private HpcDatasetService datasetService = null;
	
	@Autowired
    private HpcUserService userService = null;
	
	@Autowired
    private HpcDataTransferService dataTransferService = null;
	
	@Autowired
    private HpcProjectService projectService = null;
    
    // The logger instance.
	private final Logger logger = 
			             LoggerFactory.getLogger(this.getClass().getName());
	
    //---------------------------------------------------------------------//
    // Constructors
    //---------------------------------------------------------------------//
	
    /**
     * Constructor for Spring Dependency Injection.
     * 
     * @throws HpcException Constructor is disabled.
     */
    private HpcDatasetBusServiceImpl() throws HpcException
    {
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
    	logger.info("Invoking registerDataset(HpcDatasetRegistrationDTO): " + 
                    datasetRegistrationDTO);
    	
    	// Input validation.
    	if(datasetRegistrationDTO == null) {
    	   throw new HpcException("Null HpcDatasetRegistrationDTO",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Validate there is at least one file attached. 
    	// Also validate the associated projects included in the upload requests.
    	validateUploadRequests(datasetRegistrationDTO.getUploadRequests());
    	
    	// Validate the associated users with this dataset have valid NIH 
    	// account registered with HPC. In addition, validate the registrar
    	// has a valid data transfer account.
    	validateAssociatedUsers(datasetRegistrationDTO.getUploadRequests());

    	// Validate this registrar has not already registered a dataset with the same name.
    	validateDatasetName(datasetRegistrationDTO.getName(),
    			            datasetRegistrationDTO.getUploadRequests());

    	// Add the dataset to the repository.
    	HpcDataset dataset = 
    	   datasetService.addDataset(
    		  		         datasetRegistrationDTO.getName(), 
    				         datasetRegistrationDTO.getDescription(),
    				         datasetRegistrationDTO.getComments(),
    				         false);
    	
    	// Process the file upload requests.
    	uploadFiles(dataset, datasetRegistrationDTO.getUploadRequests(), true);
    	
    	logger.info("Registered dataset id = " + dataset.getId());
    	return dataset.getId();
    }
    
    @Override
    public void addFiles(HpcDatasetAddFilesDTO addFilesDTO) throws HpcException
    {
       	logger.info("Invoking addFiles(HpcDatasetAddFilesDTO): " + addFilesDTO);
	
       	// Input validation.
       	if(addFilesDTO == null) {
       	   throw new HpcException("Null HpcDatasetRegistrationDTO",
			                      HpcErrorType.INVALID_REQUEST_INPUT);	
       	}
       	
       	// Validate there is at least one file attached. 
    	// Also validate the associated projects included in the upload requests.
    	validateUploadRequests(addFilesDTO.getUploadRequests());
       	
       	// Locate the dataset.
       	HpcDataset dataset = datasetService.getDataset(addFilesDTO.getDatasetId());
       	if(dataset == null) {
       	   throw new HpcException("Dataset was not found: " + addFilesDTO.getDatasetId(),
       			                  HpcRequestRejectReason.DATASET_NOT_FOUND);	
       	}
       	
       	// Process the file upload requests.
    	uploadFiles(dataset, addFilesDTO.getUploadRequests(), true);
    }
    
    public void associateProjects(
                         HpcDatasetAssociateFileProjectsDTO associateFileProjectsDTO) 
                         throws HpcException
    {
	   	logger.info("Invoking associateProjects(HpcDatasetAssociateFileProjectsDTO): " + 
                    associateFileProjectsDTO);
	
	   	// Input validation.
	   	if(associateFileProjectsDTO == null) {
	   	   throw new HpcException("Null HpcDatasetAssociateFileProjectsDTO",
			                      HpcErrorType.INVALID_REQUEST_INPUT);	
	   	}
	   	
		// Validate the associated projects included in the association request.
	   	List<String> projectIds = associateFileProjectsDTO.getProjectIds();
		validateProjects(projectIds);
	   	
	   	// Locate the dataset.
	   	HpcDataset dataset = datasetService.getDataset(
	   			                    associateFileProjectsDTO.getDatasetId());
	   	if(dataset == null) {
	   	   throw new HpcException("Dataset was not found: " + 
	   	                          associateFileProjectsDTO.getDatasetId(),
	   			                  HpcRequestRejectReason.DATASET_NOT_FOUND);	
	   	}
	   	
	   	// Associate the projects to the dataset. This is a bi-directional association.
	   	for(String projectId : projectIds) {
	   		synchronized(associateProjectMetadataLock(projectId)) {
	   			synchronized(associateProjectMetadataLock(dataset.getId())) {
		   			datasetService.associateProject(dataset, 
						                            associateFileProjectsDTO.getFileId(),
						                            projectId, true);
				    projectService.associateDataset(projectId, dataset.getId(), true);
	   			}
	   		}
		}
   }
    
    @Override
    public HpcFilePrimaryMetadataDTO 
           addPrimaryMetadataItems(
    		         HpcDatasetAddMetadataItemsDTO addMetadataItemsDTO) 
                     throws HpcException
    {
       	logger.info("Invoking addPrimaryMetadataItems(HpcDatasetAddMetadataItemsDTO): " + 
                                                      addMetadataItemsDTO);
    	
       	// Input validation.
       	if(addMetadataItemsDTO == null) {
       	   throw new HpcException("Null HpcDatasetAddMetadataItemsDTO",
			                      HpcErrorType.INVALID_REQUEST_INPUT);	
       	}
       	
       	// Locate the dataset.
       	HpcDataset dataset = datasetService.getDataset(addMetadataItemsDTO.getDatasetId());
       	if(dataset == null) {
       	   throw new HpcException("Dataset was not found: " + addMetadataItemsDTO.getDatasetId(),
       			                  HpcRequestRejectReason.DATASET_NOT_FOUND);	
       	}
       	
       	String fileId = addMetadataItemsDTO.getFileId();
       	HpcFilePrimaryMetadata updatedPrimaryMetadata = null;
       	
       	// Update and Version Metadata in an atomic operation.
       	synchronized(updateMetadataLock(fileId)) {
			 // Clone the current metadata object.
		     HpcFileMetadata currentMetadata = 
		    		         cloneFileMetadata(dataset, fileId);
		
			 // Add metadata items.
		     updatedPrimaryMetadata = 
		     datasetService.addPrimaryMetadataItems(
		    		           dataset, fileId,
		                       addMetadataItemsDTO.getMetadataItems(), 
		                       true);
		
		     // Add a metadata version to the history collection.
		     datasetService.addFileMetadataVersion(
		    		                       fileId, 
		    		                       currentMetadata, true);
       	}
       	
       	return toDTO(updatedPrimaryMetadata);
    }
    
    @Override
    public HpcFilePrimaryMetadataDTO 
           updatePrimaryMetadata(
    		            HpcDatasetUpdateFilePrimaryMetadataDTO updateMetadataDTO) 
                        throws HpcException
    {
       	logger.info("Invoking updatePrimaryMetadata(HpcDatasetUpdateFilePrimaryMetadataDTO): " + 
       			    updateMetadataDTO);

		// Input validation.
		if(updateMetadataDTO == null) {
		   throw new HpcException("Null HpcDatasetUpdateFilePrimaryMetadataDTO",
		                          HpcErrorType.INVALID_REQUEST_INPUT);	
		}
		
		// Locate the dataset.
		HpcDataset dataset = datasetService.getDataset(updateMetadataDTO.getDatasetId());
		if(dataset == null) {
		   throw new HpcException("Dataset was not found: " + updateMetadataDTO.getDatasetId(),
		                          HpcRequestRejectReason.DATASET_NOT_FOUND);	
		}
       	
       	String fileId = updateMetadataDTO.getFileId();
       	HpcFilePrimaryMetadata updatedPrimaryMetadata = null;

       	// Update and Version Metadata in an atomic operation.
       	synchronized(updateMetadataLock(fileId)) {
		     // Clone the current metadata object.
		     HpcFileMetadata currentMetadata = 
		    		         cloneFileMetadata(dataset, fileId);
		     
		     // Update the primary metadata.
		     updatedPrimaryMetadata = 
		     datasetService.updatePrimaryMetadata(
		    		              dataset, fileId,
		                          updateMetadataDTO.getMetadata(), 
		                          true);  
		
		     // Add a metadata version to the history collection.
		     datasetService.addFileMetadataVersion(fileId, 
		    		                               currentMetadata, 
		    		                               true);
       	}
       	
       	return toDTO(updatedPrimaryMetadata);
	}
    
    @Override
    public HpcDatasetDTO getDataset(String id, boolean skipDataTransferStatusUpdate) 
    		                       throws HpcException
    {
    	logger.info("Invoking getDataset(String id): " + id + 
    			    ", skipDataTransferStatusCheck: " + 
    			    skipDataTransferStatusUpdate);
    	
    	// Input validation.
    	if(id == null) {
    	   throw new HpcException("Null Dataset ID",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}

    	// Locate the dataset.
    	HpcDataset dataset = datasetService.getDataset(id);

    	// Update the data transfer requests status.
    	if(dataset != null && !skipDataTransferStatusUpdate) {
    	   updateDataTransferRequestsStatus(dataset);
    	}
    	
    	return toDTO(dataset);
    }
    
    @Override
    public HpcFileDTO getFile(String id) throws HpcException
    {
    	logger.info("Invoking getFile(String id): " + id);
    	
    	// Input validation.
    	if(id == null) {
    	   throw new HpcException("Null File ID",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Get the file domain object and return it as DTO.
    	return toDTO(datasetService.getFile(id));
    }
    
    @Override
    public HpcDatasetCollectionDTO getDatasets(List<String> userIds, 
                                      HpcDatasetUserAssociation association) 
                                      throws HpcException
    {
    	// Input validation.
    	if(userIds == null || userIds.isEmpty() || association == null) {
    	   throw new HpcException("Null user-ids or association",
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	return toDTO(datasetService.getDatasets(userIds, association));
    }
    
    public HpcDatasetCollectionDTO getDatasets(String firstName, String lastName, 
	                                           HpcDatasetUserAssociation association) 
	                                           throws HpcException
    {
    	// Input validation.
    	if((firstName == null && lastName == null) || association == null) {
           throw new HpcException("Null first/last name or association",
   			                      HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	// Search for users that match the first/last name.
    	List<HpcUser> users = userService.getUsers(firstName, lastName);
    	if(users == null || users.isEmpty()) {
    	   return null;
    	}
    	
    	List<String> userIds = new ArrayList<String>();
    	for(HpcUser user : users) {
    		userIds.add(user.getNihAccount().getUserId());
    	}
    		
    	return toDTO(datasetService.getDatasets(userIds, association));
   }
    
    @Override
    public HpcDatasetCollectionDTO getDatasets(String name, boolean regex) 
    		                                  throws HpcException
    {
    	// Input validation.
    	if(name == null) {
    	   throw new HpcException("Null name", HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	return toDTO(datasetService.getDatasets(name, regex));
    }
    
    @Override
    public HpcDatasetCollectionDTO getDatasets(
    		                   HpcFilePrimaryMetadataDTO primaryMetadataDTO) 
                               throws HpcException
    {
    	// Input validation.
    	if(primaryMetadataDTO == null) {
    	   throw new HpcException("Null primary metadata query DTO", 
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	return toDTO(datasetService.getDatasets(primaryMetadataDTO.getMetadata()));
    }    

    @Override
	public HpcDatasetCollectionDTO getDatasets(HpcDataTransferStatus dataTransferStatus,
			                                   Boolean uploadRequests, 
                                               Boolean downloadRequests) 
                                              throws HpcException
    {
    	// Input validation.
    	if(dataTransferStatus == null || 
    	   (uploadRequests == null && downloadRequests == null)) {
    	   throw new HpcException("Null transfer status / upload requests / download requests", 
    			                  HpcErrorType.INVALID_REQUEST_INPUT);	
    	}
    	
    	return toDTO(datasetService.getDatasets(dataTransferStatus, uploadRequests, 
    			                                          downloadRequests));
	}
    
	@Override
	public HpcDatasetCollectionDTO getDatasetsByProjectId(String projectId)
	                                                     throws HpcException
	{
	   	// Input validation.
		if(projectId == null) {
		   throw new HpcException("Null projectId", HpcErrorType.INVALID_REQUEST_INPUT);	
		}
		
		return toDTO(datasetService.getDatasetsByProjectId(projectId));	
	}

    //---------------------------------------------------------------------//
    // Helper Methods
    //---------------------------------------------------------------------//  
	
    /**
     * Create a dataset DTO from a domain object.
     * 
     * @param dataset the domain object.
     *
     * @return The DTO.
     */
    private HpcDatasetDTO toDTO(HpcDataset dataset) throws HpcException
    {
		if (dataset == null) {
			return null;
		}

		HpcDatasetDTO datasetDTO = new HpcDatasetDTO();
		datasetDTO.setId(dataset.getId());
		datasetDTO.setFileSet(dataset.getFileSet());
		datasetDTO.setCreated(dataset.getCreated());
		datasetDTO.setLastUpdated(dataset.getLastUpdated());
		for (HpcDataTransferRequest uploadRequest : dataset.getUploadRequests()) {
			datasetDTO.getUploadRequests().add(uploadRequest);
		}

		List<HpcFile> files = dataset.getFileSet().getFiles();
		if (files != null && !files.isEmpty()) {
			HpcProjectCollectionDTO projectCollection = new HpcProjectCollectionDTO();
			for (HpcFile file : files) {
				List<String> projectIds = file.getProjectIds();
				if (projectIds != null && !projectIds.isEmpty()) {
					List<HpcProject> projects = new ArrayList<HpcProject>();
					for (String projectId : projectIds) {
						try {
							HpcProject project = projectService
									.getProject(projectId);
							if (project != null) {
								projectCollection.getHpcProjectDTO().add(toDTO(project));
							}
						} catch (HpcException e) {
							throw new HpcException(
									"Failed to retrieve Project "
											+ projectId, e);
						}
					}
				}
			}
			datasetDTO.setHpcProjectCollectionDTO(projectCollection);
		}
    	
    	return datasetDTO;
    }  
    
    /**
     * Create a file DTO from a domain object.
     * 
     * @param file the domain object.
     *
     * @return The DTO.
     */
    private HpcFileDTO toDTO(HpcFile file)
    {
    	if(file == null) {
     	   return null;
     	}
    	
    	HpcFileDTO fileDTO = new HpcFileDTO();
    	fileDTO.setFile(file);
    	
    	return fileDTO;
    }  
    
    /**
     * Create a dataset collection DTO from a list of domain objects.
     * 
     * @param datasets the domain object.
     *
     * @return The collection DTO.
     */
    private HpcDatasetCollectionDTO toDTO(List<HpcDataset> datasets) throws HpcException
    {
    	if(datasets == null || datasets.isEmpty()) {
    	   return null;
    	}
 	
    	HpcDatasetCollectionDTO datasetCollectionDTO = 
 			                    new HpcDatasetCollectionDTO();
 	    for(HpcDataset dataset : datasets) {
 		    datasetCollectionDTO.getHpcDatasetDTO().add(toDTO(dataset));
 	    }
 	    
 	    return datasetCollectionDTO;
    }
    
    /**
     * Create a primary metadata DTO from a domain object.
     * 
     * @param primaryMetadata the domain object.
     *
     * @return The DTO.
     */
    private HpcFilePrimaryMetadataDTO toDTO(HpcFilePrimaryMetadata primaryMetadata)
    {
    	if(primaryMetadata == null) {
     	   return null;
     	}
    	
    	HpcFilePrimaryMetadataDTO primaryMetadataDTO = new HpcFilePrimaryMetadataDTO();
    	primaryMetadataDTO.setMetadata(primaryMetadata);
    	
    	return primaryMetadataDTO;
    }  
   
    /**
     * Create a project DTO from a domain object.
     * 
     * @param project the domain object.
     *
     * @return The DTO.
     */
    private HpcProjectDTO toDTO(HpcProject project) 
    		                   throws HpcException 
    {
    	if(project == null) {
     	   return null;
     	}
    	
       	HpcProjectDTO dto = new HpcProjectDTO();
    	dto.setId(project.getId());
    	dto.setMetadata(project.getMetadata());
    	dto.setCreated(project.getCreated());
    	dto.setLastUpdated(project.getLastUpdated());    	
    	return dto;
    }  
    
    /**
     * Validate the users associated with the upload request are valid.
     * The associated users are - creator, registrar and primary investigator.
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
    		
    		// Verify PI and Registrar are registered with HPC.
    		validateUser(uploadRequest.getMetadata().getPrincipalInvestigatorNihUserId(),
    				     HpcDatasetUserAssociation.PRINCIPAL_INVESTIGATOR);
    		HpcUser registrar =
    		validateUser(uploadRequest.getMetadata().getRegistrarNihUserId(),
			             HpcDatasetUserAssociation.REGISTRAR);
    		
    		// Validate the registrar Data Transfer Account.
        	if(!dataTransferService.validateDataTransferAccount(
        	   registrar.getDataTransferAccount())) {
         	   throw new HpcException(
         			        "Invalid Data Transfer Account: username = " + 
         			        registrar.getDataTransferAccount().getUsername(), 
                            HpcRequestRejectReason.INVALID_DATA_TRANSFER_ACCOUNT);	
         	}
    	}
    }
    
    /**
     * Validate this registrar has not already registered a dataset with the same name.
     * 
     * @param name The dataset name.
     * @param uploadRequests The upload requests to validate. 
     *
     * @throws HpcException if any validation error found.
     */
    private void validateDatasetName(String name, 
    		                         List<HpcFileUploadRequest> uploadRequests)
    		                        throws HpcException
    {
    	if(uploadRequests == null || name == null) {
    	   return;
    	}
    	if(name.length() > 255) {
    		 throw new HpcException(
  			        "Dataset name <" + name + "> exceeds the dataset name size limitation(255) ",
                     HpcRequestRejectReason.DATASET_NAME_SIZE_LIMITATION);
      	}     	
    	for(HpcFileUploadRequest uploadRequest : uploadRequests) {
    		if(uploadRequest.getMetadata() == null || 
    		   uploadRequest.getMetadata().getRegistrarNihUserId() == null) {
    		   continue;	
    		}
    		
    		if(datasetService.exists(name, uploadRequest.getMetadata().getRegistrarNihUserId(), 
    				                 HpcDatasetUserAssociation.REGISTRAR)) {
         	   throw new HpcException(
         			        "Dataset name <" + name + "> already registered by " + 
         			        uploadRequest.getMetadata().getRegistrarNihUserId(),
                            HpcRequestRejectReason.DATASET_NAME_ALREADY_EXISTS);	
         	}
    	}
    }
 
    /**
     * Validate a user is registered with HPC.
     * 
     * @param nihUserId The NIH User ID.
     * @param userAssociation The user's association to the dataset.
     * 
     * @return The HpcUser.
     *
     * @throws HpcException if the user is not registered with HPC.
     */
    private HpcUser validateUser(String nihUserId, 
    		                     HpcDatasetUserAssociation userAssociation)
    		                    throws HpcException
    {
    	HpcUser user = userService.getUser(nihUserId);
    	if(user == null) {
    	   throw new HpcException("Could not find "+ userAssociation +
    				               " user with nihUserID = " + nihUserId,
    		                       HpcRequestRejectReason.INVALID_NIH_ACCOUNT);	
    	}
    	
    	return user;
    }
    
    /**
     * Validate upload requests are not empty.
     * 
     * @param uploadRequests The upload requests.
     *
     * @throws HpcException if any validation error found.
     */
    private void validateUploadRequests(List<HpcFileUploadRequest> uploadRequests)
                                       throws HpcException
    {
    	// Validating there is at least one file attached.
    	if(uploadRequests == null || uploadRequests.isEmpty()) {
 	       throw new HpcException("No files attached to this request", 
 			                      HpcErrorType.INVALID_REQUEST_INPUT);
    	}
    	
    	// Validate the associated projects exist.
       	for(HpcFileUploadRequest uploadRequest : uploadRequests) {
    		if(uploadRequest.getProjectIds() == null || 
    		   uploadRequest.getProjectIds().isEmpty()) {
    		   continue;	
    		}
    		validateProjects(uploadRequest.getProjectIds());
    	}
    }
    
    /**
     * Validate projects exist.
     * 
     * @param projectIds The list of project-id to validate.
     *
     * @throws HpcException if any project on the list was not found.
     */
    private void validateProjects(List<String> projectIds) throws HpcException
    {
    	if(projectIds == null || projectIds.isEmpty()) {
    	   throw new HpcException("No project IDs attached to this request", 
                                  HpcErrorType.INVALID_REQUEST_INPUT);	
     	}
    	
    	for(String projectId : projectIds) {
    		if(projectService.getProject(projectId) == null) {
    		   throw new HpcException("Project not found: " + projectId,
    		                          HpcRequestRejectReason.PROJECT_NOT_FOUND);	
    		}
    	}
	}
    
    /**
     * Update a dataset's upload and download data transfer requests with a 
     * current status polled from data-transfer.
     * 
     * @param dataset The dataset to update the transfer requests status
     *
     * @throws HpcException 
     */
    private void updateDataTransferRequestsStatus(HpcDataset dataset)
    		                                     throws HpcException
    {
    	// Update both upload and download transfer requests.
    	boolean transferStatusChanged = 
    			updateDataTransferRequestsStatus(dataset.getUploadRequests());
    	transferStatusChanged |= 
    			updateDataTransferRequestsStatus(dataset.getDownloadRequests());
    	
    	// Persist if any status has changed.
    	if(transferStatusChanged) {
    	   datasetService.persist(dataset);	
    	}
    }
    
    /**
     * Update data transfer requests with current status polled from data-transfer.
     * 
     * @param dataTransferRequests A collection of transfer requests to update.
     * @return True if at least one request had a change of status.
     *
     * @throws HpcException 
     */
    private boolean updateDataTransferRequestsStatus(
    		              List<HpcDataTransferRequest> dataTransferRequests)
    		              throws HpcException
    {
    	if(dataTransferRequests == null || dataTransferRequests.isEmpty()) {
    		return false;
    	}
    	
    	boolean transferStatusChanged = false;
		for(HpcDataTransferRequest dataTransferRequest : dataTransferRequests)
		{
			if(dataTransferRequest.getStatus() == null || 
					dataTransferRequest.getStatus() == HpcDataTransferStatus.IN_PROGRESS ||
							dataTransferRequest.getStatus() == HpcDataTransferStatus.FAILED ||
									dataTransferRequest.getStatus() == HpcDataTransferStatus.INITIATED)
			{
				        // Get the data transfer account to use in checking status.
    		            HpcDataTransferAccount dataTransferAccount = 
 	    		               userService.getUser(dataTransferRequest.getRequesterNihUserId()).
 	    		                           getDataTransferAccount();
    		
    		            // Get updated report from data transfer.
			            HpcDataTransferReport dataTransferReport = 
			            	   dataTransferService.getTransferRequestStatus(
			            			                  dataTransferRequest.getDataTransferId(),
			            				              dataTransferAccount);
			            
			            // Update the upload request.
			            transferStatusChanged |= 
			            		datasetService.setDataTransferRequestStatus(dataTransferRequest, 
			            		                                            dataTransferReport);
			            
			}
		}
		
		return transferStatusChanged;
    }
    
    /**
     * Process upload files request.
     * 
     * @param uploadRequests The file upload requests.
     * @param persist If set to true, the dataset will be persisted.
     *
     * @throws HpcException 
     */
    private void uploadFiles(HpcDataset dataset,
		                     List<HpcFileUploadRequest> uploadRequests,
		                     boolean persist) 
		                     throws HpcException
    {
    	// Upload files and attach them to the dataset
    	for(HpcFileUploadRequest uploadRequest : uploadRequests) {
    		logger.debug("Handling upload request: "+ uploadRequest);
    		
    		// Add a new file to the domain model
    		HpcFile file = datasetService.addFile(dataset, uploadRequest, false);
    		
    		// Associate this dataset with the projects.
    		if(file.getProjectIds() != null) {
    		   for(String projectId : file.getProjectIds()) {
    		       projectService.associateDataset(projectId, dataset.getId(), true);
    		   }
    		}
    		logger.debug("New File: " + file);
			
			// Transfer the file. 
    		String registrarNihId = uploadRequest.getMetadata().getRegistrarNihUserId();
			HpcUser user = userService.getUser(registrarNihId);
    		
			logger.debug("Submitting data transfer request: " + 
			             uploadRequest.getLocations());
			HpcDataTransferReport dataTransferReport = null;
			try {				
				 dataTransferReport =
                 dataTransferService.transferDataset(uploadRequest.getLocations(), 
				                                     user,dataset.getFileSet().getName());				 
			} catch(HpcException e) {
				    // Failed to upload file. Log and continue.
					logger.info("Failed to upload file: " + uploadRequest, e);
			}

			// Attach an upload data transfer request to the dataset.
			HpcDataTransferRequest transferRequest = 
			   datasetService.addDataTransferUploadRequest(
					          dataset, registrarNihId, file.getId(), 
					          uploadRequest.getLocations(), dataTransferReport, 
					          false);
			logger.debug("Data Transfer Request: " + transferRequest);
    	}
    	
    	// Persist if requested.
    	if(persist) {
    	   datasetService.persist(dataset);
    	}
	}
    
    /**
     * Clone (deep copy) a file metadata object .
     * 
     * @param dataset The dataset.
     * @param fileId The file to clone its metadata object.
     *
     * @throws HpcException 
     */
    private HpcFileMetadata cloneFileMetadata(HpcDataset dataset, String fileId) 
		                                     throws HpcException
	{
	   	// Locate the file to attach the metadata items.
	   	HpcFile file = datasetService.getFile(dataset, fileId);
	   	if(file == null) {
	   	   throw new HpcException("File not found: " + fileId, 
	   			                  HpcRequestRejectReason.FILE_NOT_FOUND);
	   	}
	   	
	   	// Get a copy of the current metadata object.
	   	return cloneJAXB(file.getMetadata());
	}
}

 