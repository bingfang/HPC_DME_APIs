/**
 * HpcCollectionRestServiceImpl.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.ws.rs.impl;

import gov.nih.nci.hpc.bus.HpcDataManagementBusService;
import gov.nih.nci.hpc.domain.dataset.HpcManagedEntity;
import gov.nih.nci.hpc.domain.metadata.HpcMetadataEntry;
import gov.nih.nci.hpc.dto.dataset.HpcDataObjectRegistrationDTO;
import gov.nih.nci.hpc.dto.dataset.HpcManagedEntityCollectionDTO;
import gov.nih.nci.hpc.dto.metadata.HpcMetadataEntryParam;
import gov.nih.nci.hpc.exception.HpcException;
import gov.nih.nci.hpc.ws.rs.HpcDataManagementRestService;

import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * HPC Collection REST Service Implementation.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public class HpcDataManagementRestServiceImpl extends HpcRestServiceImpl
             implements HpcDataManagementRestService
{   
    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//

    // The Collection Business Service instance.
	@Autowired
    private HpcDataManagementBusService dataManagementBusService = null;
	
	// The Logger instance.
	private final Logger logger = 
			             LoggerFactory.getLogger(this.getClass().getName());
    
    //---------------------------------------------------------------------//
    // constructors
    //---------------------------------------------------------------------//
     
    /**
     * Constructor for Spring Dependency Injection.
     * 
     */
    private HpcDataManagementRestServiceImpl() 
    {
    }  
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // HpcCollectionRestService Interface Implementation
    //---------------------------------------------------------------------//  
	
    @Override
    public Response registerCollection(String path, 
    		                           List<HpcMetadataEntry> metadataEntries)
    {	
    	path = toAbsolutePath(path);
		logger.info("Invoking RS: PUT /collection" + path);
		
		try {
			 dataManagementBusService.registerCollection(path, metadataEntries);
			 
		} catch(HpcException e) {
			    logger.error("RS: PUT /collection" + path + " failed:", e);
			    return errorResponse(e);
		}
		
		return createdResponse(null);
	}
    
    @Override
    public Response queryCollections(List<HpcMetadataEntryParam> metadataEntries)
    {
    	logger.info("Invoking RS: GET /collection/" + metadataEntries);
    	
    	HpcManagedEntityCollectionDTO collections = new HpcManagedEntityCollectionDTO();
		try {
			 // Validate the metadata entries input (JSON) was parsed successfully.
			 for(HpcMetadataEntryParam metadataEntry : metadataEntries)
			 if(metadataEntry.getJSONParsingException() != null) {
				throw metadataEntry.getJSONParsingException();
			 }
			 
		} catch(HpcException e) {
			    logger.error("RS: GET /collection/" + metadataEntries + " failed:", e);
			    return errorResponse(e);
		}
		
		HpcManagedEntity ent = new HpcManagedEntity();
		ent.setId(1002);
		ent.setPath("/folder-a/folder-b");
		collections.getManagedEntities().add(ent);
		
		HpcManagedEntity ent1 = new HpcManagedEntity();
		ent1.setId(100255);
		ent1.setPath("/folder-c/folder-d");
		collections.getManagedEntities().add(ent1);
		
		return okResponse(collections, false);
    }
    
    @Override
    public Response registerDataObject(String path, 
    		                           HpcDataObjectRegistrationDTO dataObjectRegistrationDTO)
    {	
    	path = toAbsolutePath(path);
		logger.info("Invoking RS: PUT /dataObject" + path);
		
		try {
			 dataManagementBusService.registerDataObject(path, dataObjectRegistrationDTO);
			 
		} catch(HpcException e) {
			    logger.error("RS: PUT /dataObject" + path + " failed:", e);
			    return errorResponse(e);
		}
    	
		return createdResponse(null);
	}
}

 