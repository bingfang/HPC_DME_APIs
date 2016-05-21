/**
 * HpcSystemBusServiceImpl.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.bus.impl;

import gov.nih.nci.hpc.bus.HpcSystemBusService;
import gov.nih.nci.hpc.domain.datamanagement.HpcDataObject;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataObjectUploadResponse;
import gov.nih.nci.hpc.domain.datatransfer.HpcDataTransferStatus;
import gov.nih.nci.hpc.domain.model.HpcDataObjectSystemGeneratedMetadata;
import gov.nih.nci.hpc.exception.HpcException;
import gov.nih.nci.hpc.service.HpcDataManagementService;
import gov.nih.nci.hpc.service.HpcDataTransferService;
import gov.nih.nci.hpc.service.HpcSecurityService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * HPC System Business Service Implementation.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id:$
 */

public class HpcSystemBusServiceImpl implements HpcSystemBusService
{  
    //---------------------------------------------------------------------//
    // Constants
    //---------------------------------------------------------------------//    
    
    // Data transfer status check timeout, in days. If these many days pass 
	// after the data registration date, and we still can't get a data transfer 
	// status, then the state will move to UNKNOWN.
	private static final int DATA_TRANSFER_STATUS_CHECK_TIMEOUT_PERIOD = 1;
	
    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//

    // Application service instances.
	
	@Autowired
    private HpcSecurityService securityService = null;
	
	@Autowired
    private HpcDataTransferService dataTransferService = null;
	
	@Autowired
    private HpcDataManagementService dataManagementService = null;
	
    // The logger instance.
	private final Logger logger = 
			             LoggerFactory.getLogger(this.getClass().getName());
	
    //---------------------------------------------------------------------//
    // Constructors
    //---------------------------------------------------------------------//
	
    /**
     * Constructor for Spring Dependency Injection.
     * 
     */
    private HpcSystemBusServiceImpl()
    {
    }   
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // HpcSystemBusService Interface Implementation
    //---------------------------------------------------------------------//  
    
    @Override
    public void updateDataTransferStatus() throws HpcException
    {
    	// Use system account to perform this service.
    	securityService.setSystemRequestInvoker();
    	
    	// Iterate through the data objects that their data transfer is in-progress
    	for(HpcDataObject dataObject : dataManagementService.getDataObjectsInProgress()) {
    		String path = dataObject.getAbsolutePath();
    		try {
    		     // Get current data transfer Request Info.
    			 HpcDataObjectSystemGeneratedMetadata systemGeneratedMetadata = 
    			    dataManagementService.getDataObjectSystemGeneratedMetadata(path);
    			 
    			 // Get the data transfer request status.
    			 HpcDataTransferStatus dataTransferStatus =
    		        dataTransferService.getDataTransferStatus(systemGeneratedMetadata.getDataTransferType(),
    		        		                                  systemGeneratedMetadata.getDataTransferRequestId());
    			 
    		     if(dataTransferStatus.equals(HpcDataTransferStatus.ARCHIVED) ||
    		        dataTransferStatus.equals(HpcDataTransferStatus.IN_TEMPORARY_ARCHIVE)) {
    		    	// Data transfer completed successfully. Update the system metadata.
     			    setDataTransferStatus(path, dataTransferStatus);
    		    	logger.info("Data transfer completed [" + dataTransferStatus + "]: " + path);
    		    	
    		     } else if(dataTransferStatus.equals(HpcDataTransferStatus.FAILED)) {
     		    	       // Data transfer failed. Remove the data object
     		    	       dataManagementService.delete(path);
     		    	       logger.info("Data transfer failed: " + path);
    		     }
    		     
    		} catch(HpcException e) {
    			    logger.error("Failed to process data transfer update:" + path, e);
    			    
    			    // If timeout occurred, move the status to unknown.
    			    this.setTransferStatusToUnknown(dataObject, true);
    		}
    	}
    }
    
    @Override
    public void processTemporaryArchive() throws HpcException
    {
    	// Use system account to perform this service.
    	securityService.setSystemRequestInvoker();
    	
    	// Iterate through the data objects that their data is in temporary archive
    	for(HpcDataObject dataObject : dataManagementService.getDataObjectsInTemporaryArchive()) {
    		String path = dataObject.getAbsolutePath();
    		HpcDataObjectSystemGeneratedMetadata systemGeneratedMetadata = null;
    		try {
    		     // Get the data object system generated metadata.
    			 systemGeneratedMetadata = 
    			       dataManagementService.getDataObjectSystemGeneratedMetadata(path);
    			 
    			 // Get an input stream to the data object in the temporary archive.
    			 File file = dataTransferService.getUploadFile(
    					         systemGeneratedMetadata.getDataTransferType(),
    					         systemGeneratedMetadata.getArchiveLocation().getFileId());
    			 InputStream dataObjectStream = new FileInputStream(file);
    			 
 				 // Transfer the data file.
 		         HpcDataObjectUploadResponse uploadResponse = 
 		        	dataTransferService.uploadDataObject(null, dataObjectStream, path, 
 		        			                             systemGeneratedMetadata.getRegistrarId(),
 		        			                             systemGeneratedMetadata.getCallerObjectId());
 		     
 		         // Delete the file.
 		         try {
 		              file.delete();
 		              
 		         } catch(Exception e) {
 		        	     logger.error("Failed to delete: " + 
 		                              systemGeneratedMetadata.getArchiveLocation().getFileId());
 		         }
 		         
 			     // Update system metadata of the data object.
 			     dataManagementService.updateDataObjectSystemGeneratedMetadata(
 			           		                 path, uploadResponse.getArchiveLocation(),
 			    			                 uploadResponse.getRequestId(), 
 			    			                 uploadResponse.getDataTransferStatus(),
 			    			                 uploadResponse.getDataTransferType()); 
 			     
    		} catch(FileNotFoundException fnf) {
    			    logger.error("File not found in temp archive: " + 
    			    		     systemGeneratedMetadata.getArchiveLocation().getFileId());
    			    
    			    // File not found in temporary archive. Set transfer status to unknown.
    			    this.setTransferStatusToUnknown(dataObject, false);
    		     
    		} catch(HpcException e) {
    			    logger.error("Failed to transfer data from temporary archive:" + path, e);
    			    
    			    // If timeout occurred, move the status to unknown.
    			    this.setTransferStatusToUnknown(dataObject, true);
    		}
    	}
    }
    
    //---------------------------------------------------------------------//
    // Helper Methods
    //---------------------------------------------------------------------//
    
    /** 
     * Determine if data transfer status check timed out.
     * 
     * @param dataObject The data object to check the timeout for.
     * 
     * @return true if status check timeout occurred. 
     */
	private boolean isDataTransferStatusCheckTimedOut(HpcDataObject dataObject) 
	{
		if(dataObject.getCreatedAt() == null) {
		   // Creation time unknown.
		   return true;	
		}
		
		// Calculate the timeout.
		Calendar timeout = Calendar.getInstance();
	    timeout.setTime(dataObject.getCreatedAt().getTime());
	    timeout.add(Calendar.DATE, DATA_TRANSFER_STATUS_CHECK_TIMEOUT_PERIOD);
	    
	    // Compare to now.
	    return Calendar.getInstance().after(timeout);
	}
	
    /** 
     * Set the data transfer status of a data object to unknown
     * 
     * @param dataObject The data object
     * @param checkTimeout If 'true', this method checks for transfer status timeout occurred 
     *                     before setting the status. 
     * @return HpcDataObjectUploadResponse
     * 
     * @throws HpcException
     */
	private void setTransferStatusToUnknown(HpcDataObject dataObject, 
			                                boolean checkTimeout)
	{
		// If timeout occurred, move the status to unknown.
		if(!checkTimeout || isDataTransferStatusCheckTimedOut(dataObject)) {
			try {
				 setDataTransferStatus(dataObject.getAbsolutePath(), 
    	                               HpcDataTransferStatus.UNKNOWN);
			} catch(Exception ex) {
				    logger.error("failed to set data transfer status to unknown: " + 
				    		     dataObject.getAbsolutePath(), ex);
			}
			
			logger.error("Unknown data transfer status: " + dataObject.getAbsolutePath());
		}
	}
	
    /** 
     * Set data transfer status of a data object
     * 
     * @param path The data object path.
     * @param dataTransferStatus The data transfer status.
     * 
     * @throws HpcException
     */
	private void setDataTransferStatus(String path, HpcDataTransferStatus dataTransferStatus)
	                                  throws HpcException
	{
		dataManagementService.updateDataObjectSystemGeneratedMetadata(
                                    path, null, null, dataTransferStatus, null);
	}
}

 