/******************************
@ddblock_begin copyright
/**
 * Readme.txt
 * @author: George Zaki 
 *
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/HPC/LICENSE.txt for details.
 */

@ddblock_end copyright
******************************/

Register a Dataset with valid inputs. 
At the time this test is checked, the input.json file follows the policies file.  The returned HTTP code should equal 201.

The dataset should follow the hierarchy /project/datatset/dataObject

Therefore, a project should be registered first in readme, then a dataset in runme.  
