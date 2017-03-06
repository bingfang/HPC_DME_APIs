/**
 * HpcSecurityRestServiceImpl.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.ws.rs.impl;

import gov.nih.nci.hpc.bus.HpcSecurityBusService;
import gov.nih.nci.hpc.dto.security.HpcAuthenticationResponseDTO;
import gov.nih.nci.hpc.dto.security.HpcGroupRequestDTO;
import gov.nih.nci.hpc.dto.security.HpcGroupResponseDTO;
import gov.nih.nci.hpc.dto.security.HpcSystemAccountDTO;
import gov.nih.nci.hpc.dto.security.HpcUpdateUserRequestDTO;
import gov.nih.nci.hpc.dto.security.HpcUserDTO;
import gov.nih.nci.hpc.dto.security.HpcUserListDTO;
import gov.nih.nci.hpc.exception.HpcException;
import gov.nih.nci.hpc.ws.rs.HpcSecurityRestService;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * HPC Security REST Service Implementation.
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id: HpcSecurityRestServiceImpl.java 1015 2016-03-27 14:44:36Z rosenbergea $
 */

public class HpcSecurityRestServiceImpl extends HpcRestServiceImpl
             implements HpcSecurityRestService
{   
    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//

    // The Security Business Service instance.
	@Autowired
    private HpcSecurityBusService securityBusService = null;
    
	// The Logger instance.
	private final Logger logger = 
			             LoggerFactory.getLogger(this.getClass().getName());
    
    //---------------------------------------------------------------------//
    // constructors
    //---------------------------------------------------------------------//
     
    /**
     * Constructor for Spring Dependency Injection.
     * 
     * @throws HpcException Constructor is disabled.
     */
    private HpcSecurityRestServiceImpl() throws HpcException
    {
    }  
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // HpcSecurityRestService Interface Implementation
    //---------------------------------------------------------------------//  
	
    @Override
    public Response registerUser(HpcUserDTO userRegistrationDTO)
    {	
		try {
			securityBusService.registerUser(userRegistrationDTO);
			 
		} catch(HpcException e) {
			    logger.error("RS: PUT /user failed:", e);
			    return errorResponse(e);
		}
		
		return createdResponse(userRegistrationDTO.getNciAccount().getUserId());
	}
    
    @Override
    public Response updateUser(String nciUserId,
                               HpcUpdateUserRequestDTO updateUserRequestDTO)
    {	
		try {
			 securityBusService.updateUser(nciUserId, updateUserRequestDTO);
			 
		} catch(HpcException e) {
			    logger.error("RS: POST /user failed:", e);
			    return errorResponse(e);
		}
		
		return okResponse(null, false);
	}   
    
    @Override
    public Response getUser(String nciUserId)
    {
		HpcUserDTO user = null;
		try {
			 user = securityBusService.getUser(nciUserId);
			 
		} catch(HpcException e) {
			    logger.error("RS: GET /user/{nciUserId} failed:", e);
			    return errorResponse(e);
		}
		
		return okResponse(user, true);
	}
    
    @Override
    public Response getUsers(String nciUserId, String firstName, String lastName)
    {
		HpcUserListDTO users = null;
		try {
			 users = securityBusService.getUsers(nciUserId, firstName, lastName);
			 
		} catch(HpcException e) {
			    logger.error("RS: GET /user failed:", e);
			    return errorResponse(e);
		}
		
		return okResponse(!users.getNciAccounts().isEmpty() ? users : null, true);
    }
    
    @Override
    public Response authenticate()
    {
		HpcAuthenticationResponseDTO authenticationResponse = null;
		try {
			 authenticationResponse = securityBusService.getAuthenticationResponse();

		} catch(HpcException e) {
			    logger.error("RS: POST /authenticate failed", e);
			    return errorResponse(e);
		}
		
		return okResponse(authenticationResponse, false);
	}    

    @Override
    public Response setGroup(HpcGroupRequestDTO groupRequest)
    {
    	HpcGroupResponseDTO groupResponse = null;
		try {
			 groupResponse = securityBusService.setGroup(groupRequest);
			 
		} catch(HpcException e) {
			    logger.error("RS: POST /group: " + groupResponse + " failed:", e);
			    return errorResponse(e);
		}
		
		return okResponse(groupResponse, false);
    }    
    
    @Override
    public Response registerSystemAccount(HpcSystemAccountDTO systemAccountRegistrationDTO)
    {
		try {
			 securityBusService.registerSystemAccount(systemAccountRegistrationDTO);
			 
		} catch(HpcException e) {
			    logger.error("RS: PUT /systemAccount failed:", e);
			    return errorResponse(e);
		}
		
		return createdResponse(systemAccountRegistrationDTO.getAccount().getIntegratedSystem().value());    	
    }
}

 
