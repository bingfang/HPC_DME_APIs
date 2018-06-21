package gov.nih.nci.hpc.reports.ws.rest;

import gov.nih.nci.hpc.reports.model.VaultSummary;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@CrossOrigin
@RestController
@RequestMapping("/reports")
public class ReportsController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private JSONParser jsonParser = new JSONParser();
    private long teraBytes = 1099511627776L;


    @RequestMapping("/vaultsummary")
    public VaultSummary[] getVaultSummary() throws Exception {

        RestTemplate restTemplate = new RestTemplateBuilder().basicAuthorization("ncifhpcdmsvcp", "").build();
        ResponseEntity<String> reportData = restTemplate.getForEntity("https://fr-s-clvrsf-mgr.ncifcrf.gov/manager/api/json/1.0/listVaults.adm", String.class);
        if(!reportData.getStatusCode().equals(HttpStatus.OK)) {
            throw new Exception("Failed to call service: " + reportData.getStatusCode());
        }

        Collection<VaultSummary> vaultSummaries = fromJSON(reportData.getBody());
        VaultSummary[] vaultSummariesArray = new VaultSummary[vaultSummaries.size()];
        return vaultSummaries.toArray(vaultSummariesArray);
    }

    private Collection<VaultSummary> fromJSON(String responseStr) throws ParseException {
        Collection<VaultSummary> vaultSummaries = new ArrayList<VaultSummary>();
        JSONObject responseData =  (JSONObject) ((JSONObject) jsonParser.parse(responseStr)).get("responseData");

        JSONArray vaults = (JSONArray) responseData.get("vaults");
        if (vaults != null) {
            Iterator<JSONObject> vaultsIterator = vaults.iterator();
            while (vaultsIterator.hasNext()) {
                JSONObject vault = vaultsIterator.next();
                VaultSummary vaultSummary = new VaultSummary();
                vaultSummary.setName((String) vault.get("name"));
                vaultSummary.setDescription((String) vault.get("description"));
                vaultSummary.setCapacity((Long) vault.get("usableSize") / teraBytes);
                vaultSummary.setUsed((Long) vault.get("usedLogicalSizeFromStorage") / teraBytes);
                vaultSummary.setCreationDate((String) vault.get("creationDate"));
                vaultSummaries.add(vaultSummary);
            }
        }

        return vaultSummaries;
    }
}
