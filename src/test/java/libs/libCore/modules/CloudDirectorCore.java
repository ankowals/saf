package libs.libCore.modules;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.xml.XmlPath;
import io.restassured.path.xml.config.XmlPathConfig;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CloudDirectorCore {

    private SharedContext ctx;
    private Storage Storage;
    private FileCore FileCore;
    private StepCore StepCore;

    private String authToken;
    private ValidatableResponse vSessionResp;
    private ValidatableResponse vOrgResp;
    private ValidatableResponse vCatalogResp;
    private ValidatableResponse vCatalogItemResp;
    private ValidatableResponse vAppTemplateResp;
    private ValidatableResponse vAppTemplateVmResp;
    private ValidatableResponse vDcResp;
    private ValidatableResponse vAppResp;

    // PicoContainer injects class SharedContext
    public CloudDirectorCore(SharedContext ctx) {
        this.ctx = ctx;
        this.FileCore = ctx.Object.get("FileCore",FileCore.class);
        this.Storage = ctx.Object.get("Storage",Storage.class);
        this.StepCore = ctx.Object.get("StepCore",StepCore.class);
    }

    public void login(){

        String url = Storage.get("Environment.Active.vCloudDirector.host");
        String api = Storage.get("Environment.Active.vCloudDirector.api");
        String user = Storage.get("Environment.Active.vCloudDirector.user");
        String passwd = Storage.get("Environment.Active.vCloudDirector.pass");
        String org = Storage.get("Environment.Active.vCloudDirector.org");

        if (url == null) {
            Log.error("Environment.Active.vCloudDirector.host is null or empty");
        }
        if (api == null) {
            Log.warn("Environment.Active.vCloudDirector.api is null or empty. Setting api to 1.5");
            api = "1.5";
        }
        if (user == null) {
            Log.error("Environment.Active.vCloudDirector.user is null or empty");
        }
        if (passwd == null) {
            Log.error("Environment.Active.vCloudDirector.pass is null or empty");
        }
        if (org == null) {
            Log.error("Environment.Active.vCloudDirector.org is null or empty");
        }

        url = url + "/api/sessions";

        //build specification
        RequestSpecification request = given()
                .baseUri(url)
                .auth().basic(user + "@" + org, passwd)
                .header("Accept", "application/*+xml;version="+api);

        Log.debug("Trigger request to login using /api/sessions");
        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .post();

        //print status code
        Integer statusCode = response.getStatusCode();
        Log.debug("Response status code is " + statusCode);

        //print headers and extract auth token for re-usage in further requests
        Log.debug("Response headers are");
        Boolean authHeaderPresent = false;
        Headers allHeaders = response.headers();
        for( Header header : allHeaders){
            Log.debug(header.getName() + ": " + header.getValue());
            if ( header.getName().equalsIgnoreCase("x-vcloud-authorization") ){
                authToken = header.getValue();
                authHeaderPresent = true;
            }
        }

        //print response body
        Log.debug("Response body is");
        Log.debug(response.prettyPrint());


        //validate response status code
        if ( statusCode != 200 ) {
            Log.error("Wrong status code received. Login failed!");
        }

        //validate authorization header
        if ( ! authHeaderPresent ){
            Log.error("Authorization failed. Header x-vcloud-authorization is not present in the response.");
        }

        vSessionResp = response.then();
    }

    public void logout(){
        String url = Storage.get("Environment.Active.vCloudDirector.host");
        url = url + "/api/session";

        RequestSpecification request = buildRequest(url);
        Log.debug("Trigger request to logout using /api/session");
        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .delete();

        //print status code
        Integer statusCode = response.getStatusCode();
        Log.debug("Response status code is " + statusCode);

        //print headers and extract auth token for re-usage in further requests
        Log.debug("Response headers are");
        Headers allHeaders = response.headers();
        for( Header header : allHeaders){
            Log.debug(header.getName() + ": " + header.getValue());
        }

        //print response body
        //Log.debug("Response body is");
        //Log.debug(response.prettyPrint());

        //validate response status code
        if ( statusCode != 204 ) {
            Log.error("Wrong status code received. Logout failed!");
        }

    }

    public void getOrganization(String name) {
        Log.debug("Extract org href for " + name + " from response to /api/sessions request");
        XmlPath xml = setRoot(vSessionResp,"Session");
        String href = extractHref(xml,"Link.@type", "Link", "vcloud.org+xml", name);
        Log.debug("Trigger request to get org " + name + " details");
        RequestSpecification request = buildRequest(href);
        vOrgResp = triggerGetRequest(request, 200);
    }

    public void getCatalog(String name) {
        Log.debug("Extract catalog href for " + name + " from response to /api/org request");
        XmlPath xml = setRoot(vOrgResp,"Org");
        String href = extractHref(xml,"Link.@type", "Link", "vcloud.catalog+xml", name);
        Log.debug("Trigger request to get catalog " + name + " details");
        RequestSpecification request = buildRequest(href);
        vCatalogResp = triggerGetRequest(request, 200);
    }

    public void getCatalogItem(String name) {
        Log.debug("Extract catalog item href for " + name + " from response to /api/catalog request");
        XmlPath xml = setRoot(vCatalogResp,"Catalog");
        String href = extractHref(xml,"CatalogItems.CatalogItem.@type", "CatalogItem", "vcloud.catalogItem+xml", name);
        Log.debug("Trigger request to get catalog item " + name + " details");
        RequestSpecification request = buildRequest(href);
        vCatalogItemResp = triggerGetRequest(request, 200);

    }

    public String getVAppTemplate(String name) {
        Log.debug("Extract vApp template entity href for " + name + " from response to /api/catalogItem request");
        XmlPath xml = setRoot(vCatalogItemResp,"CatalogItem");
        String href = extractHref(xml,"Entity.@type", "Entity", "vcloud.vAppTemplate+xml", name);
        Log.debug("Trigger request to get vApp template " + name + " details");
        RequestSpecification request = buildRequest(href);
        vAppTemplateResp = triggerGetRequest(request, 200);

        Log.debug("href to be used in instantiateVAppTemplateParams request is " + href);

        return href;

    }

    public String getVmTemplateFromVAppTemplate(String name) {
        Log.debug("Extract vm href for " + name + " from response to /api/vAppTemplate request");
        XmlPath xml = setRoot(vAppTemplateResp,"VAppTemplate");
        String href = extractHref(xml,"Children.Vm.@type", "Vm", "vcloud.vm+xml", name);
        Log.debug("Trigger request to get vm " + name + " details");
        RequestSpecification request = buildRequest(href);
        vAppTemplateVmResp = triggerGetRequest(request, 200);

        return href;
    }

    public String getVdc(String name) {
        Log.debug("Extract vdc href for " + name + " from response /api/org request");
        XmlPath xml = setRoot(vOrgResp,"Org");
        String href = extractHref(xml,"Link.@type", "Link", "vcloud.vdc+xml", name);
        Log.debug("Trigger request to get vdc " + name + " details");
        RequestSpecification request = buildRequest(href);
        vDcResp = triggerGetRequest(request, 200);

        Log.debug("Extract instantiateVAppTemplateParams href for " + name + " from response /api/vdc request");
        xml = setRoot(vDcResp,"Vdc");
        href = extractHref(xml,"Link.@type", "Link", "vcloud.instantiateVAppTemplateParams+xml", "");

        Log.debug("new vApp can be instantiated by triggering post request towards following url " + href);

        return href;
    }

    public String getNetwork(String name) {
        Log.debug("Extract network href for " + name + " from response /api/org request");

        ValidatableResponse resp = vOrgResp;

        XmlPath xml = setRoot(resp,"Org");
        String href = extractHref(xml,"Link.@type", "Link", "vcloud.orgNetwork+xml", name);

        return href;
    }

    public String getVApp(String name){
        Log.debug("Extract vApp href for " + name + " from response /api/vdc request");

        ValidatableResponse resp = vDcResp;

        XmlPath xml = setRoot(resp,"Vdc");
        String href = extractHref(xml,"ResourceEntities.ResourceEntity.@type", "ResourceEntity", "vcloud.vApp+xml", name);
        Log.debug("Trigger request to get vApp " + name + " details");
        RequestSpecification request = buildRequest(href);
        vAppResp = triggerGetRequest(request, 200);

        return href;
    }

    public void createNewVAppFromTemplate(String vdcHref, String vAppTemplateEntityHref, String name, String network, String networkHref) {
        Log.debug("Create new VApp with name " + name + " from template");

        Storage.set("TestData.vAppTemplate.VApp_name", name);
        Storage.set("TestData.vAppTemplate.vAppTemplateEntity_href", vAppTemplateEntityHref);
        Storage.set("TestData.vAppTemplate.Vdc_network_name", network);
        Storage.set("TestData.vAppTemplate.Vdc_network_href", networkHref);

        File file = StepCore.evaluateTemplate("InstantiateVAppTemplateParams");
        String content = FileCore.readToString(file);

        RequestSpecification request = buildRequest(vdcHref);
        request.contentType("application/vnd.vmware.vcloud.instantiateVAppTemplateParams+xml")
                .body(content);

        ValidatableResponse response = triggerPostRequest(request, 201);

        Log.debug("Checking task status");
        Log.debug("Extract instantiateVAppTemplateParam Task href from response to /api/vdc request");

        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to create new vApp");

    }


    public void addVmToVapp(String vAppHref, String vm_templateHref, String vApp_name, String vm_name, String network, String networkHref, String ipAllocationMode) {
        Log.debug("Add new VM with name " + vm_name + " to vApp from template " + vm_templateHref);

        Storage.set("TestData.vAppTemplate.Vm_name", vm_name);
        Storage.set("TestData.vAppTemplate.Vm_template_href", vm_templateHref);
        Storage.set("TestData.vAppTemplate.VApp_name", vApp_name);
        Storage.set("TestData.vAppTemplate.Vdc_network_name", network);
        Storage.set("TestData.vAppTemplate.Vdc_network_href", networkHref);
        Storage.set("TestData.vAppTemplate.ip_address_allocation_mode", ipAllocationMode);

        File file = StepCore.evaluateTemplate("RecomposeVappParams");
        String content = FileCore.readToString(file);

        RequestSpecification request = buildRequest(vAppHref + "/action/recomposeVApp");
        request.contentType("application/vnd.vmware.vcloud.recomposeVAppParams+xml")
                .body(content);

        ValidatableResponse response = triggerPostRequest(request, 202);

        Log.debug("Checking task status");
        Log.debug("Extract vdcRecomposeVapp Task href from response to /api/vApp/{id}/action/recomposeVApp request");

        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to add new vm to vApp");

        //give some time to dhcp to assign new ip address
        //extract ip address of new vm??

    }


    public String getVmFromVApp(String name, String vAppHref){
        Log.debug("Trigger request to get new vm " + name + " href from vApp " + vAppHref);

        RequestSpecification request = buildRequest(vAppHref);
        vAppResp = triggerGetRequest(request, 200);

        Log.debug("Extract vm href for " + name + " from response /api/vApp request");
        XmlPath xml = setRoot(vAppResp,"VApp");
        String href = extractHref(xml,"Children.Vm.@type", "Vm", "vcloud.vm+xml", name);

        return href;
    }

    public ValidatableResponse getVAppDetails(String vAppHref){
        Log.debug("Trigger request to get details of vApp " + vAppHref);

        RequestSpecification request = buildRequest(vAppHref);
        ValidatableResponse response = triggerGetRequest(request, 200);

        return response;

    }


    public ValidatableResponse getVmDetails(String vmHref){
        Log.debug("Trigger request to get details of vm " + vmHref);

        RequestSpecification request = buildRequest(vmHref);
        ValidatableResponse response = triggerGetRequest(request, 200);

        return response;

    }



    public void getVmNetworkCardsDetails(String vmHref){
        Log.debug("Trigger request to get network cards details of vm " + vmHref);

        RequestSpecification request = buildRequest(vmHref + "/virtualHardwareSection/networkCards");
        ValidatableResponse response = triggerGetRequest(request, 200);


    }


    public void setVmMemory(String vmHref, String memorySize) {
        Log.debug("Trigger request to set memory amount of vm " + vmHref);

        if ( ! NumberUtils.isNumber(memorySize) ){
            Log.error("memorySize shall be a number");
        }

        RequestSpecification request = buildRequest(vmHref + "/virtualHardwareSection/memory");
        ValidatableResponse response = triggerGetRequest(request, 200);

        String content = response.extract().asString();
        content = content.replaceAll("ElementName>\\d+", "ElementName>" + memorySize);
        content = content.replaceAll("VirtualQuantity>\\d+", "VirtualQuantity>" + memorySize);

        //resend the response content with new values of memory amount
        request = buildRequest(vmHref + "/virtualHardwareSection/memory");
        request.contentType("application/vnd.vmware.vcloud.rasdItem+xml")
                .body(content);

        response = triggerPutRequest(request, 202);
        Log.debug("Extract set memory size Task href from response to /api/vm/{id}/virtualHardwareSection/memory request");
        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to set memory size");

    }


    public void setVmCpu(String vmHref, String numberOfCpu) {
        Log.debug("Trigger request to set number of cpu of vm " + vmHref);

        if ( ! NumberUtils.isNumber(numberOfCpu) ){
            Log.error("numberOfCpu shall be a number");
        }

        RequestSpecification request = buildRequest(vmHref + "/virtualHardwareSection/cpu");
        ValidatableResponse response = triggerGetRequest(request, 200);

        String content = response.extract().asString();
        content = content.replaceAll("ElementName>\\d+", "ElementName>" + numberOfCpu);
        content = content.replaceAll("VirtualQuantity>\\d+", "VirtualQuantity>" + numberOfCpu);

        //resend the response content with new values of memory amount
        request = buildRequest(vmHref + "/virtualHardwareSection/cpu");
        request.contentType("application/vnd.vmware.vcloud.rasdItem+xml")
                .body(content);

        response = triggerPutRequest(request, 202);
        Log.debug("Extract set number of Cpu Task href from response to /api/vm/{id}/virtualHardwareSection/cpu request");
        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to set number of Cpu");

    }


    /**
     * Modifies IsConnected flag for all connections from true to false
     *
     * @param vmHref String, href used to identified particular VM
     */
    public void disconnectNetworkConnection(String vmHref){
        Log.debug("Trigger request to disconnect network from " + vmHref);

        //get networkConnectionSection
        RequestSpecification request = buildRequest(vmHref + "/networkConnectionSection");
        ValidatableResponse response = triggerGetRequest(request, 200);

        String content = response.extract().asString();
        content = content.replaceAll("IsConnected>true", "IsConnected>false");

        //resend the response content with new values of memory amount
        request = buildRequest(vmHref + "/networkConnectionSection");
        request.contentType("application/vnd.vmware.vcloud.networkConnectionSection+xml")
                .body(content);

        response = triggerPutRequest(request, 202);
        Log.debug("Extract diconnectNetworkConnection Task href from response to /api/vm/{id}/networkConnectionSection request");
        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to disconnect network");

    }


    /**
     * Modifies IsConnected flag for all connections from false to true
     *
     * @param vmHref String, href used to identified particular VM
     */
    public void connectNetworkConnection(String vmHref){
        Log.debug("Trigger request to connect network to " + vmHref);

        //get networkConnectionSection
        RequestSpecification request = buildRequest(vmHref + "/networkConnectionSection");
        ValidatableResponse response = triggerGetRequest(request, 200);

        String content = response.extract().asString();
        content = content.replaceAll("IsConnected>false", "IsConnected>true");

        //resend the response content with new values of memory amount
        request = buildRequest(vmHref + "/networkConnectionSection");
        request.contentType("application/vnd.vmware.vcloud.networkConnectionSection+xml")
                .body(content);

        response = triggerPutRequest(request, 202);
        Log.debug("Extract diconnectNetworkConnection Task href from response to /api/vm/{id}/networkConnectionSection request");
        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to disconnect network");

    }



    public void setVmDisk(String vmHref, String initialDiskSize, String diskSize) {
        Log.debug("Trigger request to set disk size of vm " + vmHref);

        if ( ! NumberUtils.isNumber(initialDiskSize) ){
            Log.error("intialDiskSize shall be a number");
        }
        if ( ! NumberUtils.isNumber(diskSize) ){
            Log.error("diskSize shall be a number");
        }

        if ( Integer.parseInt(initialDiskSize) > Integer.parseInt(diskSize) ) {

            Log.warn("New diskSize value lower than initialDiskSize value! Nothing to do!");

        } else {

            Log.debug("Consolidating this vm");

            //VMs deployed in cloudDirector are FastProvisioned - VM Consolidation has to be done before expanding the disk size
            RequestSpecification request = buildRequest(vmHref + "/action/consolidate");
            ValidatableResponse response = triggerPostRequest(request, 202);

            Log.debug("Extract vm consolidate Task href from response to /api/vm/{id}/action/consolidate");
            XmlPath xml = setRoot(response, "Task");
            String href = xml.getString("@href");
            href = href.replace("api/api", "api");
            validateTaskStatus("success", href, "Failed to consolidate the vm");

            //Change the disk size
            Log.debug("Changing disk capacity");

            request = buildRequest(vmHref + "/virtualHardwareSection/disks");
            response = triggerGetRequest(request, 200);

            String content = response.extract().asString();
            content = content.replaceAll("vcloud:capacity=\"" + initialDiskSize, "vcloud:capacity=\"" + diskSize);

            //resend the response content with new values of memory amount
            request = buildRequest(vmHref + "/virtualHardwareSection/disks");
            request.contentType("application/vnd.vmware.vcloud.rasdItemsList+xml")
                    .body(content);

            response = triggerPutRequest(request, 202);
            Log.debug("Extract set disk size Task href from response to /api/vm/{id}/virtualHardwareSection/disks request");
            xml = setRoot(response, "Task");
            href = xml.getString("@href");
            href = href.replace("api/api", "api");
            validateTaskStatus("success", href, "Failed to set disk size");
        }
    }


    public void setGuestCustomization(String vmHref){
        Log.debug("Setting GuestCustomization for vm " + vmHref);

        File file = StepCore.evaluateTemplate("GuestCustomizationSection");
        String content = FileCore.readToString(file);

        RequestSpecification request = buildRequest(vmHref + "/guestCustomizationSection");
        request.contentType("application/vnd.vmware.vcloud.guestCustomizationSection+xml")
                .body(content);

        ValidatableResponse response = triggerPutRequest(request, 202);

        Log.debug("Extract guestCustomizationSection Task href from response to /api/vm/{id}/guestCustomizationSection request");
        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to set GuestCustomization");

    }


    public void powerOnVm(String vmHref){
        Log.debug("Powering on vm (deploy) " + vmHref);

        RequestSpecification request = buildRequest(vmHref + "/power/action/powerOn");
        ValidatableResponse response = triggerPostRequest(request, 202);

        Log.debug("Extract powerOn Task href from response to /api/vm/{id}/power/action/powerOn request");
        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to power on vm");

    }


    public void powerOffVm(String vmHref){
        Log.debug("Powering off vm (un-deploy)" + vmHref);

        File file = StepCore.evaluateTemplate("UndeployVAppParams");
        String content = FileCore.readToString(file);

        RequestSpecification request = buildRequest(vmHref + "/action/undeploy");
        request.contentType("application/vnd.vmware.vcloud.undeployVAppParams+xml")
                .body(content);

        ValidatableResponse response = triggerPostRequest(request, 202);
        Log.debug("Extract undeploy Task href from response to /api/vm/{id}//action/undeploy request");
        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to power off vm");

    }


    public void rebootVm(String vmHref){
        Log.debug("Rebooting vm " + vmHref);

        RequestSpecification request = buildRequest(vmHref + "/power/action/reboot");
        ValidatableResponse response = triggerPostRequest(request, 202);

        Log.debug("Extract Task href from response to /api/vm/{id}/power/action/reboot request");
        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to reboot vm");
    }


    public void removeVmFromVApp(String vmHref){
        Log.debug("Removing vm " + vmHref + " from VApp");

        //check status and deployment
        ValidatableResponse details = getVmDetails(vmHref);

        XmlPath xml = setRoot(details,"details");
        String vmStatus = xml.getString("@status");
        String vmDeployed = xml.getString("@deployed");
        String vmName = xml.getString("@name");

        Log.debug("Vm " + vmName + " status is " + vmStatus + ", deployed=" + vmDeployed);

        //if deployed=true and status=4 -> shutdown, power off, delete
        if ( vmStatus.equals("4") && vmDeployed.equals("true") ) {
            //shutdown
            Log.debug("Shutting down vm " + vmHref);

            RequestSpecification request = buildRequest(vmHref + "/power/action/shutdown");
            ValidatableResponse response = triggerPostRequest(request, 202);

            Log.debug("Extract guestCustomizationSection Task href from response to /api/vm/{id}/guestCustomizationSection request");
            xml = setRoot(response, "Task");
            String href = xml.getString("@href");
            href = href.replace("api/api", "api");
            validateTaskStatus("success", href, "Failed to shutdown vm " + vmName);

            //status shall be 8 now
            //power off (un-deploy)
            powerOffVm(vmHref);
        }
        //if deployed=true and status=8 -> power off, delete
        if ( vmStatus.equals("8") && vmDeployed.equals("true") ) {
            //power off (un-deploy)
            powerOffVm(vmHref);
        }

        //if deployed=false and status=8 -> delete
        RequestSpecification request = buildRequest(vmHref);
        ValidatableResponse response = triggerDeleteRequest(request, 202);

        Log.debug("Extract delete Task href from response to /api/vm/{id}");
        xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to remove vm");
    }


    public void deployVm(String vmHref){
        File file = StepCore.evaluateTemplate("DeployVAppParams");
        String content = FileCore.readToString(file);

        RequestSpecification request = buildRequest(vmHref + "/action/deploy");
        request.contentType("application/vnd.vmware.vcloud.deployVAppParams+xml")
                .body(content);

        ValidatableResponse response = triggerPostRequest(request, 202);

        Log.debug("Extract deploy Task href from response to /api/vm/{id}/action/deploy request");
        XmlPath xml = setRoot(response,"Task");
        String href = xml.getString("@href");
        href = href.replace("api/api","api");
        validateTaskStatus("success", href, "Failed to deploy vm");
    }


    private void validateTaskStatus(String expectedStatus, String href, String errorMsg){
        Log.debug("Checking status of task " + href);

        String status = "running";
        Integer count = 0;
        while ( status.equals("running") ){
            status = checkTaskStatus(href);

            Log.debug("Current task status is " + status);
            if ( ! status.equals("running") ){
                break;
            }
            //Waiting 5 seconds before next check, timeout is 300 sec (300/5=60)
            StepCore.sleep(5);
            count++;
            if ( count == 60 ){
                Log.warn("Timeout");
                break;
            }
        }
        Log.debug("reported status of task " + href + " is " + status);

        if ( ! status.equals(expectedStatus) ){
            Log.error(errorMsg);
        }
    }


    private String checkTaskStatus(String href){

        Log.debug("Trigger request to get status of task " + href);
        RequestSpecification request = buildRequest(href);
        ValidatableResponse response = triggerGetRequest(request, 200);

        Log.debug("Extract status of Task " + href + " from response /api/task request");

        XmlPath xml = setRoot(response,"Task");
        String result = xml.getString("@status");

        return result;
    }


    private XmlPath setRoot(ValidatableResponse vResp, String path){

        String respBody = vResp.extract().response().asString();

        //set xmlpath features (optional) and set root
        Map<String, Boolean> features = new HashMap<>();
        features.put("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        features.put("http://apache.org/xml/features/disallow-doctype-decl", false);
        features.put("http://xml.org/sax/features/namespaces", false);

        XmlPath xml = new XmlPath(respBody).
                using(XmlPathConfig.xmlPathConfig().with().features(features))
                .setRoot(path);

        //Log.debug("Response body is " + respBody);

        return xml;
    }

    private String extractHref(XmlPath xml, String listPath, String type, String typeContent, String name) {
        List<String> links = xml.getList(listPath);

        //Log.debug("Extract " + typeContent + " href from response");
        String href ="";
        for( String link : links ){
            if ( link.contains(typeContent) ) {
                if ( name.equals("") ){
                    href = xml.getString("**.find { it.name() == '" + type + "' && it.@type == '" + link + "' }.@href");
                } else {
                    href = xml.getString("**.find { it.name() == '" + type + "' && it.@type == '" + link + "' && it.@name == '" + name + "' }.@href");
                }
            }
        }

        if ( href.equals("") ){
            Log.error("Requested " + typeContent + " " + name + " is not available");
        }
        href = href.replace("api/api","api");

        Log.debug("href is " + href);

        return href;
    }

    private RequestSpecification buildRequest(String url){

        String api = Storage.get("Environment.Active.vCloudDirector.api");

        //build specification
        RequestSpecification request = given()
                .baseUri(url)
                .header("x-vcloud-authorization", authToken)
                .header("Accept", "application/*+xml;version=" + api);

        return request;
    }

    private ValidatableResponse triggerGetRequest(RequestSpecification request, Integer expectedStatusCode){

        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .get();

        //print status code
        Integer statusCode = response.getStatusCode();
        Log.debug("Response status code is " + statusCode);

        //print headers
        Log.debug("Response headers are");
        Headers allHeaders = response.headers();
        for( Header header : allHeaders){
            Log.debug(header.getName() + ": " + header.getValue());
        }

        //print response body
        Log.debug("Response body is");
        Log.debug(response.prettyPrint());

        //validate response code
        if ( ! statusCode.equals(expectedStatusCode) ) {
            Log.error("Wrong status code received " + statusCode + " but expected was " + expectedStatusCode + ". Request failed!");
        }

        ValidatableResponse result = response.then();

        return result;
    }


    private ValidatableResponse triggerPostRequest(RequestSpecification request, Integer expectedStatusCode){

        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .post();

        //print status code
        Integer statusCode = response.getStatusCode();
        Log.debug("Response status code is " + statusCode);

        //print headers
        Log.debug("Response headers are");
        Headers allHeaders = response.headers();
        for( Header header : allHeaders){
            Log.debug(header.getName() + ": " + header.getValue());
        }

        //print response body
        Log.debug("Response body is");
        Log.debug(response.prettyPrint());

        //validate response status code
        if ( ! statusCode.equals(expectedStatusCode) ) {
            Log.error("Wrong status code received " + statusCode + " but expected was " + expectedStatusCode + ". Request failed!");
        }

        ValidatableResponse result = response.then();

        return result;
    }

    private ValidatableResponse triggerPutRequest(RequestSpecification request, Integer expectedStatusCode){
        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .put();

        //print status code
        Integer statusCode = response.getStatusCode();
        Log.debug("Response status code is " + statusCode);

        //print headers
        Log.debug("Response headers are");
        Headers allHeaders = response.headers();
        for( Header header : allHeaders){
            Log.debug(header.getName() + ": " + header.getValue());
        }

        //print response body
        Log.debug("Response body is");
        Log.debug(response.prettyPrint());

        //validate response status code
        if ( ! statusCode.equals(expectedStatusCode) ) {
            Log.error("Wrong status code received " + statusCode + " but expected was " + expectedStatusCode + ". Request failed!");
        }

        ValidatableResponse result = response.then();

        return result;

    }


    private ValidatableResponse triggerDeleteRequest(RequestSpecification request, Integer expectedStatusCode){
        //trigger request and log it (it will be added as attachment to the report)
        Response response = request
                .when()
                .log()
                .all()
                .delete();

        //print status code
        Integer statusCode = response.getStatusCode();
        Log.debug("Response status code is " + statusCode);

        //print headers
        Log.debug("Response headers are");
        Headers allHeaders = response.headers();
        for( Header header : allHeaders){
            Log.debug(header.getName() + ": " + header.getValue());
        }

        //print response body
        Log.debug("Response body is");
        Log.debug(response.prettyPrint());

        //validate response status code
        if ( ! statusCode.equals(expectedStatusCode) ) {
            Log.error("Wrong status code received " + statusCode + " but expected was " + expectedStatusCode + ". Request failed!");
        }

        ValidatableResponse result = response.then();

        return result;

    }

}
