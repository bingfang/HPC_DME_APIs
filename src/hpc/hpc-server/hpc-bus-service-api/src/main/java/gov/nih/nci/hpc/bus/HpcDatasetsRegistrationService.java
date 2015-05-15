/**
 * HpcDatasetsRegistrationService.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.bus;

import gov.nih.nci.hpc.dto.service.HpcDatasetsRegistrationInputDTO;
import gov.nih.nci.hpc.exception.HpcException;

/**
 * <p>
 * HPC Datasets Registration Business Service Interface.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public interface HpcDatasetsRegistrationService 
{         
    /**
     * Register Datasets.
     *
     * @param registrationInputDTO The datasets registration input DTO.
     * 
     * @throws HpcException
     */
    public void registerDatasets(
    		            HpcDatasetsRegistrationInputDTO registrationInputDTO)
    		            throws HpcException;
}

 