/**
 * HpcFilePrimaryMetadataCodec.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

package gov.nih.nci.hpc.dao.mongo.codec;

import gov.nih.nci.hpc.domain.metadata.HpcFilePrimaryMetadata;
import gov.nih.nci.hpc.domain.metadata.HpcMetadataItem;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.BsonDocumentReader;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * HPC File Primary Metadata Codec. 
 * </p>
 *
 * @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
 * @version $Id$
 */

public class HpcFilePrimaryMetadataCodec extends HpcCodec<HpcFilePrimaryMetadata>
{ 
    //---------------------------------------------------------------------//
    // Instance members
    //---------------------------------------------------------------------//
	
	// The logger instance.
	private final Logger logger = 
			             LoggerFactory.getLogger(this.getClass().getName());
	
    //---------------------------------------------------------------------//
    // Constructors
    //---------------------------------------------------------------------//
	
    /**
     * Default Constructor.
     * 
     */
    public HpcFilePrimaryMetadataCodec()
    {
    }   
    
    //---------------------------------------------------------------------//
    // Methods
    //---------------------------------------------------------------------//
    
    //---------------------------------------------------------------------//
    // Codec<HpcFilePrimaryMetadata> Interface Implementation
    //---------------------------------------------------------------------//  
    
	@Override
	public void encode(BsonWriter writer, 
			           HpcFilePrimaryMetadata filePrimaryMetadata,
					   EncoderContext encoderContext) 
	{
		Document document = new Document();
 
		// Extract the data from the domain object.
		Boolean dataContainsPII = filePrimaryMetadata.isDataContainsPII();
		Boolean dataContainsPHI = filePrimaryMetadata.isDataContainsPHI();
		Boolean dataEncrypted = filePrimaryMetadata.isDataEncrypted();
		Boolean dataCompressed = filePrimaryMetadata.isDataCompressed();
		String fundingOrganization = filePrimaryMetadata.getFundingOrganization();
		String primaryInvestigatorNihUserId = 
				      filePrimaryMetadata.getPrimaryInvestigatorNihUserId();
		String creatorNihUserId = filePrimaryMetadata.getCreatorNihUserId();
		String registratorNihUserId = 
				          filePrimaryMetadata.getRegistratorNihUserId();
		String labBranch = filePrimaryMetadata.getLabBranch();
		String description = filePrimaryMetadata.getDescription();
		List<HpcMetadataItem> metadataItems = filePrimaryMetadata.getMetadataItems();
		
		// Set the data on the BSON document.
		if(dataContainsPII != null) {
		   document.put(FILE_PRIMARY_METADATA_DATA_CONTAINS_PII_KEY, 
				        dataContainsPII);
		}
		if(dataContainsPHI != null) {
		   document.put(FILE_PRIMARY_METADATA_DATA_CONTAINS_PHI_KEY, 
				        dataContainsPHI);
		}
		if(dataEncrypted != null) {
		   document.put(FILE_PRIMARY_METADATA_DATA_ENCRYPTED_KEY, 
				        dataEncrypted);
		}
		if(dataCompressed != null) {
		   document.put(FILE_PRIMARY_METADATA_DATA_COMPRESSED_KEY, 
					    dataCompressed);
		}
		if(fundingOrganization != null) {
		   document.put(FILE_PRIMARY_METADATA_FUNDING_ORGANIZATION_KEY, 
					    fundingOrganization);
		}
		if(primaryInvestigatorNihUserId != null) {
			   document.put(
			   FILE_PRIMARY_METADATA_PRIMARY_INVESTIGATOR_NIH_USER_ID_KEY, 
			   primaryInvestigatorNihUserId);
		}
		if(creatorNihUserId != null) {
		   document.put(FILE_PRIMARY_METADATA_CREATOR_NIH_USER_ID_KEY, 
				        creatorNihUserId);
		}
		if(registratorNihUserId != null) {
		   document.put(FILE_PRIMARY_METADATA_REGISTRATOR_NIH_USER_ID_KEY, 
				        registratorNihUserId);
		}
		if(labBranch != null) {
		   document.put(FILE_PRIMARY_METADATA_LAB_BRANCH_KEY, labBranch);
		}
		if(description != null) {
		   document.put(FILE_PRIMARY_METADATA_DESCRIPTION_KEY, description);
		}
		if(metadataItems != null && metadataItems.size() > 0) {
		   document.put(FILE_PRIMARY_METADATA_METADATA_ITEMS_KEY, 
				        metadataItems);
		}

		getRegistry().get(Document.class).encode(writer, document, 
				                                 encoderContext);
	}
 
	@Override
	public HpcFilePrimaryMetadata decode(BsonReader reader, 
			                             DecoderContext decoderContext) 
	{
		// Get the BSON Document.
		Document document = 
				 getRegistry().get(Document.class).decode(reader, 
						                                  decoderContext);
		
		// Map the BSON Document to a domain object.
		HpcFilePrimaryMetadata filePrimaryMetadata = new HpcFilePrimaryMetadata();
		filePrimaryMetadata.setDataContainsPII(
				   document.get(FILE_PRIMARY_METADATA_DATA_CONTAINS_PII_KEY, 
                                Boolean.class));
		filePrimaryMetadata.setDataContainsPHI(
				   document.get(FILE_PRIMARY_METADATA_DATA_CONTAINS_PHI_KEY, 
                                Boolean.class));
		filePrimaryMetadata.setDataEncrypted(
				   document.get(FILE_PRIMARY_METADATA_DATA_ENCRYPTED_KEY, 
                                Boolean.class));
		filePrimaryMetadata.setDataCompressed(
				   document.get(FILE_PRIMARY_METADATA_DATA_COMPRESSED_KEY, 
                                Boolean.class));
		filePrimaryMetadata.setFundingOrganization(
				   document.get(FILE_PRIMARY_METADATA_FUNDING_ORGANIZATION_KEY, 
                                String.class));
		filePrimaryMetadata.setPrimaryInvestigatorNihUserId(
		    document.get(FILE_PRIMARY_METADATA_PRIMARY_INVESTIGATOR_NIH_USER_ID_KEY, 
			             String.class));
		filePrimaryMetadata.setCreatorNihUserId(
		    document.get(FILE_PRIMARY_METADATA_CREATOR_NIH_USER_ID_KEY, 
						 String.class));
		filePrimaryMetadata.setRegistratorNihUserId(
		    document.get(FILE_PRIMARY_METADATA_REGISTRATOR_NIH_USER_ID_KEY, 
				    	 String.class));
		filePrimaryMetadata.setLabBranch(
			document.get(FILE_PRIMARY_METADATA_LAB_BRANCH_KEY, 
		                 String.class));
		filePrimaryMetadata.setDescription(
			document.get(FILE_PRIMARY_METADATA_DESCRIPTION_KEY, 
				         String.class));
		
		// Map the collections.
		List<Document> metadataItemDocuments = 
		    (List<Document>) document.get(FILE_PRIMARY_METADATA_METADATA_ITEMS_KEY);
		if(metadataItemDocuments != null) {
		   for(Document metadataItemDocument : metadataItemDocuments) {
			   filePrimaryMetadata.getMetadataItems().add(
					                  decodeMetadataItem(metadataItemDocument, 	
					                                     decoderContext));
		   }
		}
		
		return filePrimaryMetadata;
	}
	
	@Override
	public Class<HpcFilePrimaryMetadata> getEncoderClass() 
	{
		return HpcFilePrimaryMetadata.class;
	}
	
    //---------------------------------------------------------------------//
    // Helper Methods
    //---------------------------------------------------------------------//  
	
    /**
     * Decode HpcMetadataItem
     *
     * @param doc The HpcMetadataItem document
     * @param decoderContext
     * @return Decoded HpcMetadataItem object.
     */
    private HpcMetadataItem decodeMetadataItem(Document doc, 
    		                                   DecoderContext decoderContext)
    {
    	BsonDocumentReader docReader = 
    		new BsonDocumentReader(doc.toBsonDocument(Document.class, 
    				                                  getRegistry()));
		return getRegistry().get(HpcMetadataItem.class).decode(docReader, 
		                                                       decoderContext);
	}
}

 