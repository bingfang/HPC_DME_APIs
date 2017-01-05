/**
 * HpcDataManagementBusService.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.bus;

import gov.nih.nci.hpc.dto.datamanagement.HpcCollectionDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcCollectionRegistrationDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataManagementModelDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataObjectDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataObjectDownloadRequestDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataObjectDownloadResponseDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcDataObjectRegistrationDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcEntityPermissionRequestDTO;
import gov.nih.nci.hpc.dto.datamanagement.HpcEntityPermissionResponseListDTO;
import gov.nih.nci.hpc.exception.HpcException;

import java.io.File;
import java.util.List;

/**
 * <p>
 * HPC Data Management Business Service Interface.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public interface HpcDataManagementBusService 
{  
    /**
     * Register a Collection.
     *
     * @param path The collection's path.
     * @param collectionRegistration A DTO containing a list of metadata entries to attach to the collection.
     * @return true if a new collection was registered, false if the collection already exists
     *         and its metadata got updated.
     * @throws HpcException on service failure.
     */
    public boolean registerCollection(String path,
    		                          HpcCollectionRegistrationDTO collectionRegistration) 
    		                         throws HpcException;
    
    /**
     * Get Collection.
     *
     * @param path The collection's path.
     * @return A Collection DTO.
     * @throws HpcException on service failure.
     */
    public HpcCollectionDTO getCollection(String path) throws HpcException;
    
    /**
     * Register a Data object.
     *
     * @param path The data object's path.
     * @param dataObjectRegistration A DTO contains the metadata and data transfer locations.
     * @param dataObjectFile (Optional) The data object file. 2 options are available to upload the data -
     *                         Specify a source in 'dataObjectRegistrationDTO' or provide this file. The caller
     *                         is expected to provide one and only one option.
     * @return true if a new data object was registered, false if the collection already exists
     *         and its metadata got updated.
     * @throws HpcException on service failure.
     */
    public boolean registerDataObject(String path,
    		                          HpcDataObjectRegistrationDTO dataObjectRegistration,
    		                          File dataObjectFile) 
    		                         throws HpcException;
    
    /**
     * Get Data Object.
     *
     * @param path The data object's path.
     * @return A Data Object DTO.
     * 
     * @throws HpcException on service failure.
     */
    public HpcDataObjectDTO getDataObject(String path) throws HpcException;
    
    /**
     * Download Data Object.
     *
     * @param path The data object path.
     * @param downloadRequest The download request DTO.
     * @return Data Object Download ResponseDTO 
     * @throws HpcException on service failure.
     */
	public HpcDataObjectDownloadResponseDTO 
	          downloadDataObject(String path,
			                     HpcDataObjectDownloadRequestDTO downloadRequest)
			                    throws HpcException;

    /**
     * POST Set permissions.
     *
     * @param entityPermissionRequests Requests to set entities (Collections or Data Objects) permissions.
     * @return Responses with each request's result.
     * @throws HpcException on service failure.
     */
	public HpcEntityPermissionResponseListDTO setPermissions(
			                  List<HpcEntityPermissionRequestDTO> entityPermissionRequests)
			                  throws HpcException;
	
    /**
     * Get the Data Management Model (Metadata validation rules and hierarchy definition) for a DOC.
     *
     * @param doc The DOC to get the model for.
     * @return Data Management Model DTO.
     * @throws HpcException on service failure.
     */
	public HpcDataManagementModelDTO getDataManagementModel(String doc) throws HpcException;
}

 