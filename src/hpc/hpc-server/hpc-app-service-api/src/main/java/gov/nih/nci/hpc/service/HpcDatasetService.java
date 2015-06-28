/**
 * HpcDatasetService.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.service;

import gov.nih.nci.hpc.domain.model.HpcDataset;
import gov.nih.nci.hpc.domain.dataset.HpcFileUploadRequest;
import gov.nih.nci.hpc.domain.dataset.HpcDatasetUserAssociation;
import gov.nih.nci.hpc.exception.HpcException;

import java.util.List;

/**
 * <p>
 * HPC Dataset Application Service Interface.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public interface HpcDatasetService 
{         
    /**
     * Add dataset.
     *
     * @param name The dataset name.
     * @param description The dataset description.
     * @param comments The dataset comments.
     * @param uploadRequests List of files to upload.
     * @return The registered dataset ID.
     * 
     * @throws HpcException
     */
    public String add(String name, String description, String comments,
    			      List<HpcFileUploadRequest> uploadRequests) 
    			     throws HpcException;
    
    /**
     * Get dataset.
     *
     * @param id The dataset ID.
     * @return The dataset.
     * 
     * @throws HpcException
     */
    public HpcDataset get(String id) throws HpcException;
    
    /**
     * Get datasets associated with a specific user.
     *
     * @param userId the user id.
     * @param association The association between the dataset and the user.
     * @return HpcDataset collection, or null if no results found.
     * 
     * @throws HpcException
     */
    public List<HpcDataset> 
           get(String userId, HpcDatasetUserAssociation association) 
        	  throws HpcException;
}

 