/**
 * HpcManagedDataService.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.service;

import gov.nih.nci.hpc.domain.DynamicField;
import gov.nih.nci.hpc.domain.HpcDataset;
import gov.nih.nci.hpc.domain.HpcManagedDataType;
import gov.nih.nci.hpc.domain.HpcManagedData;
import gov.nih.nci.hpc.exception.HpcException;

import java.util.List;

/**
 * <p>
 * HPC Managed Data Application Service Interface.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public interface HpcManagedDataService 
{         
    /**
     * Add managed data.
     *
     * @param type The managed data type.
     * @param datasets The datasets to start manage.
     * @return The added managed data ID.
     * 
     * @throws HpcException
     */
    public String add(HpcManagedDataType type,
    		          List<HpcDataset> datasets) throws HpcException;
    
    /**
     * Get managed data.
     *
     * @param id The managed data ID.
     * @return The managed data.
     * 
     * @throws HpcException
     */
    public HpcManagedData get(String id) throws HpcException;
}

 