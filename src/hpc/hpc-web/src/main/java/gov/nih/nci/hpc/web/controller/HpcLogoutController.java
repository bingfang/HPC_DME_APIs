/**
 * HpcLoginController.java
 *
 * Copyright SVG, Inc.
 * Copyright Leidos Biomedical Research, Inc
 * 
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See https://ncisvn.nci.nih.gov/svn/HPC_Data_Management/branches/hpc-prototype-dev/LICENSE.txt for details.
 */
package gov.nih.nci.hpc.web.controller;

import gov.nih.nci.hpc.web.model.HpcLogin;

import javax.servlet.http.HttpSession;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
/**
 * <p>
 * HPC Web Login controller
 * </p>
 *
 * @author <a href="mailto:Prasad.Konka@nih.gov">Prasad Konka</a>
 * @version $Id: HpcLoginController.java 
 */

@Controller
@EnableAutoConfiguration
@RequestMapping("/logout")
public class HpcLogoutController extends AbstractHpcController {

  @RequestMapping(method = RequestMethod.GET)
  public String home(Model model, HttpSession session){
	  HpcLogin hpcLogin = new HpcLogin();
	  model.addAttribute("hpcLogin", hpcLogin);
	  session.invalidate();
	  session = null;
	  return "index";
  }
}
