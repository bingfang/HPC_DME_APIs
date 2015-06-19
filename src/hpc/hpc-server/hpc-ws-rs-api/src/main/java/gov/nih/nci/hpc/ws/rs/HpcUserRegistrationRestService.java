/**
 * HpcUserRegistrationRestService.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.ws.rs;

import gov.nih.nci.hpc.dto.userregistration.HpcUserDTO;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;

/**
 * <p>
 * HPC User Registration REST Service Interface.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

@Path("/")
public interface HpcUserRegistrationRestService
{    
    /**
     * GET user by ID.
     *
     * @param id The registered user ID.
     * @return gov.nih.nci.hpc.dto.userregistration.HpcUserDTO entity.
     */
    @GET
    @Path("/user/{id}")
    @Produces("application/json,application/xml")
    public Response getUser(@PathParam("id") String id); 
    
    /**
     * POST Validate User request.
     *
     * @param userDTO The user DTO to validate.
     */
    @POST
    @Path("/user")
    @Consumes("application/json,application/xml")
    public boolean validateUser(HpcUserDTO userDTO); 
    
    /**
     * POST registration request.
     *
     * @param userDTO The user DTO to register.
     */
    @POST
    @Path("/user")
    @Consumes("application/json,application/xml")
    public Response registerUser(HpcUserDTO userDTO);
    
    
}

 