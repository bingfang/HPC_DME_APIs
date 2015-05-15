/**
 * HpcManagedDatasetsBsonCodec.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.dao.mongo.codec;

import gov.nih.nci.hpc.dto.types.HpcDataset;
import gov.nih.nci.hpc.exception.HpcException;
import gov.nih.nci.hpc.exception.HpcErrorType;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * <p>
 * HPC Managed Datasets BSON Codec. 
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public class HpcManagedDatasetsBsonCodec 
             implements CollectibleCodec<HpcManagedDatasetsBson>
{ 
    //---------------------------------------------------------------------//
    // Constants
    //---------------------------------------------------------------------//    
    
    // BSON Document keys.
    private final static String MONGO_ID_KEY = "_id";
    private final static String DATASETS_KEY = "datasets"; 
    
    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//
	
	// The logger instance.
	private final Logger logger = 
			             LoggerFactory.getLogger(this.getClass().getName());
	
	// The codec registry.
	private CodecRegistry codecRegistry;
	
    //---------------------------------------------------------------------//
    // Constructors
    //---------------------------------------------------------------------//
	
    /**
     * Default Constructor.
     * 
     * @throws HpcException Constructor is disabled.
     */
    private HpcManagedDatasetsBsonCodec() throws HpcException
    {
    	throw new HpcException("Constructor Disabled",
                                HpcErrorType.SPRING_CONFIGURATION_ERROR);
    }   
    
    /**
     * Constructor w/ codec registry.
     * 
     * @param codecRegistry registry..
     */
    public HpcManagedDatasetsBsonCodec(CodecRegistry codecRegistry) 
    {
    	this.codecRegistry = codecRegistry;
    }  
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // CollectibleCodec<HpcManagedDatasetsBson> Interface Implementation
    //---------------------------------------------------------------------//  
    
	@Override
	public void encode(BsonWriter writer, 
					   HpcManagedDatasetsBson managedDatasets,
					   EncoderContext encoderContext) 
	{
		Document document = new Document();
 
		// Extract the data from the BSON POJO.
		ObjectId objectId = managedDatasets.getObjectId();
		List<HpcDataset> datasets = 
				         managedDatasets.getManagedDatasets().getDatasets();
 
		// Set the data on the BSON document.
		if(objectId != null) {
		   document.put(MONGO_ID_KEY, objectId);
		}
		if(datasets != null && datasets.size() > 0) {
		   document.put(DATASETS_KEY, datasets);
		}

		codecRegistry.get(Document.class).encode(writer, document, encoderContext);
	}
 
	@Override
	public HpcManagedDatasetsBson decode(BsonReader reader, 
			                             DecoderContext decoderContext) 
	{
		// Get the BSON Document.
		Document document = 
				 codecRegistry.get(Document.class).decode(reader, decoderContext);
		
		// Map the BSON Document to a BSON POJO.
		HpcManagedDatasetsBson managedDatasets = new HpcManagedDatasetsBson();
		managedDatasets.setObjectId(document.getObjectId(MONGO_ID_KEY));
		List<HpcDataset> datasets = 
				         (List<HpcDataset>) document.get(DATASETS_KEY);
		if(datasets != null) {
		   for(HpcDataset dataset : datasets) {
			   managedDatasets.getManagedDatasets().getDatasets().add(dataset);
		   }
		}
		
		return managedDatasets;
	}
	
	@Override
	public Class<HpcManagedDatasetsBson> getEncoderClass() 
	{
		return HpcManagedDatasetsBson.class;
	}
 
	@Override
	public HpcManagedDatasetsBson generateIdIfAbsentFromDocument(
			                      HpcManagedDatasetsBson managedDatasets) 
	{
		return documentHasId(managedDatasets) ? 
				             managedDatasets : new HpcManagedDatasetsBson();
	}
 
	@Override
	public boolean documentHasId(HpcManagedDatasetsBson managedDatasets) 
	{
		return managedDatasets.getObjectId() != null;
	}
 
	@Override
	public BsonValue getDocumentId(HpcManagedDatasetsBson managedDatasets) 
	{
	    return new BsonString(managedDatasets.getObjectId().toHexString());
	}
}

 