/**
 * HpcDatasetsRegistrationRestService.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.deploy.rs;

import gov.nih.nci.hpc.dto.service.HpcDatasetsRegistrationInputDTO;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;

/**
 * <p>
 * HPC Datasets Registration REST Service Interface.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public interface HpcDatasetsRegistrationRestService
{    
    /**
     * GET.
     *
     * @param id The metadata ID.
     * @return The Metada object if found, otherwise returns null.
     */
    @GET
    @Path("/registration/{id}")
    @Produces("application/json,application/xml")
    public HpcDatasetsRegistrationInputDTO getRegistration(@PathParam("id") String id); 
    
    /**
     * POST registration request.
     *
     * @param registrationInputDTO The datasets registration input DTO.
     */
    @POST
    @Path("/registration")
    @Consumes("application/json,application/xml")
    public Response registerDatasets(
                            HpcDatasetsRegistrationInputDTO registrationInputDTO);
}

 