--
-- hpc_data_migration.sql
--
-- Copyright SVG, Inc.
-- Copyright Leidos Biomedical Research, Inc
-- 
-- Distributed under the OSI-approved BSD 3-Clause License.
-- See http://ncip.github.com/HPC/LICENSE.txt for details.
--
--
-- @author <a href="mailto:eran.rosenberg@nih.gov">Eran Rosenberg</a>
--
  
CREATE TABLE HPC_DATA_MIGRATION_TASK (
	ID VARCHAR2(50) PRIMARY KEY,
	PARENT_ID VARCHAR2(50),
	USER_ID VARCHAR2(50),
	PATH VARCHAR2(2700),
	CONFIGURATION_ID VARCHAR2(50),
	FROM_S3_ARCHIVE_CONFIGURATION_ID VARCHAR2(50),
	TO_S3_ARCHIVE_CONFIGURATION_ID VARCHAR2(50),
	TYPE VARCHAR2(50),
	STATUS VARCHAR2(50),
	CREATED TIMESTAMP(6)
);

CREATE TABLE HPC_DATA_MIGRATION_TASK_RESULT (
	ID VARCHAR2(50) PRIMARY KEY,
	PARENT_ID VARCHAR2(50),
	USER_ID VARCHAR2(50),
	PATH VARCHAR2(2700),
	CONFIGURATION_ID VARCHAR2(50),
	FROM_S3_ARCHIVE_CONFIGURATION_ID VARCHAR2(50),
	TO_S3_ARCHIVE_CONFIGURATION_ID VARCHAR2(50),
	FROM_S3_ARCHIVE_LOCATION_FILE_CONTAINER_ID VARCHAR2(50),
	FROM_S3_ARCHIVE_LOCATION_FILE_ID VARCHAR2(2700),
	TO_S3_ARCHIVE_LOCATION_FILE_CONTAINER_ID VARCHAR2(50),
	TO_S3_ARCHIVE_LOCATION_FILE_ID VARCHAR2(2700),
	TYPE VARCHAR2(50),
	MESSAGE VARCHAR2(4000),
	RESULT VARCHAR2(50),
	CREATED TIMESTAMP(6),
	COMPLETED TIMESTAMP(6)
);
        
                  

