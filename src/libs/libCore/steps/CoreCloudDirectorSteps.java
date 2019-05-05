package libs.libCore.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.response.ValidatableResponse;
import libs.libCore.modules.*;

public class CoreCloudDirectorSteps extends BaseSteps {


    /**
     * [DEPRECATED]
     * Create new vApp via vCloudDirector api
     *
     * @param name String, name of the vm configuration
     */
    @Given("^create new vApp (.+)$")
    public void create_new_vApp(String name) {
        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String catalog = Storage.get("TestData." + name + ".Catalog");
        String template = Storage.get("TestData." + name + ".VAppTemplate");
        String catalogItem = Storage.get("TestData." + name + ".CatalogItem");
        String vm = Storage.get("TestData." + name + ".VmTemplate");
        String vdc = Storage.get("TestData." + name + ".Vdc");
        String vm_name = Storage.get("TestData." + name + ".VmName");
        String network = Storage.get("TestData." + name + ".Network");

        if ( catalog == null || catalog.equals("") ) {
            Log.error("Value of TestData." + name + ".Catalog null or empty!");
        }
        if ( template == null || template.equals("") ) {
            Log.error("Value of TestData." + name + ".VAppTemplate null or empty!");
        }
        if ( catalogItem == null || catalogItem.equals("") ) {
            Log.error("Value of TestData." + name + ".CatalogItem null or empty!");
        }
        if ( vm == null || vm.equals("") ) {
            Log.error("Value of TestData." + name + ".VmTemplate null or empty!");
        }
        if ( vdc == null || vdc.equals("") ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }
        if ( vm_name == null || vm_name.equals("") ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( network == null || network.equals("") ) {
            Log.error("Value of TestData." + name + ".Network null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get catalog using its name and get org response
        String catalogHref = CloudDirectorCore.getHrefOfCatalog(catalog, orgResp);
        ValidatableResponse catalogResp = CloudDirectorCore.getFromHref(catalogHref);

        //get catalog item using its name and catalog response
        String itemHref = CloudDirectorCore.getHrefOfCatalogItem(catalogItem, catalogResp);
        ValidatableResponse itemResp = CloudDirectorCore.getFromHref(itemHref);

        //get app template from catalog item
        String appTemplateHref = CloudDirectorCore.getHrefOfVAppTemplate(template, itemResp);
        ValidatableResponse appTemplateResp = CloudDirectorCore.getFromHref(appTemplateHref);

        //get vm template from app template
        String vmTemplateHref = CloudDirectorCore.getHrefOfVmTemplate(vm, appTemplateResp);
        ValidatableResponse vmTemplate = CloudDirectorCore.getFromHref(vmTemplateHref);

        //get vdc url
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);

        //get network url
        String networkHref = CloudDirectorCore.getHrefOfNetwork(network, orgResp);

        //create new vapp from app template and new vm in it
        CloudDirectorCore.createNewVAppFromTemplate(vdcHref, appTemplateHref, vm_name, network, networkHref);

        //teardown session
        CloudDirectorCore.logout();
    }


    /**
     * Adds new VM to an existing vApp using vCloudDirector api
     *
     * @param name String, name of the vm configuration
     */
    @When("^add new vm (.+) to vApp$")
    public void add_new_vm_to_vApp(String name) {

        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String catalog = Storage.get("TestData." + name + ".Catalog");
        String template = Storage.get("TestData." + name + ".VAppTemplate");
        String catalogItem = Storage.get("TestData." + name + ".CatalogItem");
        String vm = Storage.get("TestData." + name + ".VmTemplate"); //name of the vm template from vApp template
        String vdc = Storage.get("TestData." + name + ".Vdc");
        String vm_name = Storage.get("TestData." + name + ".VmName"); //name of the vm to be deployed in vApp
        String network = Storage.get("TestData." + name + ".Network");
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String ipAllocationMode = Storage.get("TestData." + name + ".ip_allocation_mode");

        if ( catalog == null || catalog.equals("") ) {
            Log.error("Value of TestData." + name + ".Catalog null or empty!");
        }
        if ( template == null || template.equals("") ) {
            Log.error("Value of TestData." + name + ".VAppTemplate null or empty!");
        }
        if ( catalogItem == null || catalogItem.equals("") ) {
            Log.error("Value of TestData." + name + ".CatalogItem null or empty!");
        }
        if ( vm == null || vm.equals("") ) {
            Log.error("Value of TestData." + name + ".VmTemplate null or empty!");
        }
        if ( vdc == null || vdc.equals("") ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }
        if ( vm_name == null || vm_name.equals("") ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( network == null || network.equals("") ) {
            Log.error("Value of TestData." + name + ".Network null or empty!");
        }
        if ( vapp == null || vapp.equals("") ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( ipAllocationMode == null || ipAllocationMode.equals("") ) {
            Log.error("Value of TestData." + name + ".ip_allocation_mode null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get catalog using its name and get org response
        String catalogHref = CloudDirectorCore.getHrefOfCatalog(catalog, orgResp);
        ValidatableResponse catalogResp = CloudDirectorCore.getFromHref(catalogHref);

        //get catalog item using its name and catalog response
        String itemHref = CloudDirectorCore.getHrefOfCatalogItem(catalogItem, catalogResp);
        ValidatableResponse itemResp = CloudDirectorCore.getFromHref(itemHref);

        //get app template from catalog item
        String appTemplateHref = CloudDirectorCore.getHrefOfVAppTemplate(template, itemResp);
        ValidatableResponse appTemplateResp = CloudDirectorCore.getFromHref(appTemplateHref);

        //get vm template from app template
        String vmTemplateHref = CloudDirectorCore.getHrefOfVmTemplate(vm, appTemplateResp);
        ValidatableResponse vmTemplate = CloudDirectorCore.getFromHref(vmTemplateHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);

        //get network url
        String networkHref = CloudDirectorCore.getHrefOfNetwork(network, orgResp);

        //add new vm to an existing vapp
        CloudDirectorCore.addVmToVapp(appHref, vmTemplateHref, vapp, vm_name, network, networkHref, ipAllocationMode);

        //get updated app
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //set guest customization
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);
        CloudDirectorCore.setGuestCustomization(vmHref);

        //customize resources
        String memorySize = Storage.get("TestData." + name + ".Memory");
        if ( memorySize != null && ! memorySize.equals("") ){
            CloudDirectorCore.setVmMemory(vmHref, memorySize);
        } else {
            Log.warn("Value of TestData." + name + ".Memory not set or empty. No RAM memory adjustment will be done");
        }

        String numberOfCpu = Storage.get("TestData." + name + ".Cpu");
        if ( numberOfCpu != null && ! numberOfCpu.equals("") ) {
            CloudDirectorCore.setVmCpu(vmHref, numberOfCpu);
        } else {
            Log.warn("Value of TestData." + name + ".Cpu not set or empty. No Cpu adjustment will be done");
        }

        String diskSize = Storage.get("TestData." + name + ".DiskSize");
        if ( diskSize != null && ! diskSize.equals("") ) {
            String initialDiskSize = Storage.get("TestData." + name + ".InitialDiskSize");
            if ( initialDiskSize == null || initialDiskSize.equals("") ){
                initialDiskSize = "102400";
                Log.warn("Value of TestData." + name + ".InitialDiskSize not set or empty. Going to extend capacity of disk with initial capacity of 102400 MB");
            }
            CloudDirectorCore.setVmDisk(vmHref, initialDiskSize, diskSize);
        } else {
            Log.warn("Value of TestData." + name + ".DiskSize not set or empty. No disk capacity adjustment will be done.");
        }

        //deploy vm in an existing vapp
        CloudDirectorCore.deployVm(vmHref);

        //teardown session
        CloudDirectorCore.logout();

        Log.debug("Waiting 3 minutes for DHCP to assign an ip address");
        StepCore.sleep(180);
    }


    /**
     * [DEPRECATED]
     * Forces re-customization of a Vm via vCloudDirector api
     *
     * @param name String, name of the vm configuration
     */
    @When("^force recustomization for vm (.+)$")
    public void force_recustomization_for_vm(String name) {

        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".VmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //un-deploy
        Log.debug("Forced recofniguration - power off (undeploy)");
        CloudDirectorCore.powerOffVm(vmHref);
        //issue put request to set needsCustomization=true in vm params
        //Log.debug("Forced reconfiguration - set needs customization flag to true");
        //vmMgmt.setNeedsCustomizationFlagForVm(newVmUrl);
        //deploy the vm with flag force re-customization set
        Log.debug("Forced reconfiguration - deploy with force customization");
        CloudDirectorCore.deployVm(vmHref);
        //power on vm
        Log.debug("Forced reconfiguration - power on");
        CloudDirectorCore.powerOnVm(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }


    /**
     * Powers on an exitsing VM via vCloudDirector api
     *
     * @param name String, name of the vm configuration
     */
    @When("^powerOn vm (.+)$")
    public void powerOn_vm(String name) {

        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".VmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //power-on vm
        CloudDirectorCore.powerOnVm(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }


    /**
     * Disconnects vm from network
     * @param name String, name of the vm configuration
     */
    @When("^disconnect network for vm (.+)$")
    public void disconnect_network_for_vm(String name)  {

        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".VmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //disconnect default network from vm
        CloudDirectorCore.disconnectNetworkConnection(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }


    /**
     * Connects back vm to network
     * @param name String, name of the vm configuration
     */
    @When("^connect network for vm (.+)$")
    public void connect_network_for_vm(String name) {
        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".VmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //connect default network
        CloudDirectorCore.connectNetworkConnection(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }


    /**
     * Returns VM details from cloudDirector
     * @param name String, name of the vm configuration
     */
    @When("^get network cards details for vm (.+)$")
    public void get_network_cards_details(String name) {
        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".VmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //get network card details
        CloudDirectorCore.getVmNetworkCardsDetails(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }




    /**
     * [DEPRECATED]
     * Power off an existsing vm via vCloudDirector api
     *
     * @param name String, name of the vm configuration
     */
    @When("^powerOff vm (.+)$")
    public void powerOff_vm(String name) {
        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".VmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //power-off vm
        CloudDirectorCore.powerOffVm(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }

    /**
     * Reboots an existing VM via vCloudDirector api
     *
     * @param name String, name of the vm configuration
     */
    @Given("^reboot vm (.+)$")
    public void reboot_vm(String name) {
        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".VmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //reboot vm
        CloudDirectorCore.rebootVm(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }


    /**
     * Checks Vm status via vCLoudDirector api and prints it into the log file
     *
     * @param name String, name of the vm configuration
     */
    @Then("^check vm (.+) status$")
    public void check_vm_status(String name) {

        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vdc = Storage.get("TestData." + name + ".Vdc");
        String vm_name = Storage.get("TestData." + name + ".VmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //read vm details
        CloudDirectorCore.getFromHref(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }


    /**
     * Removes an existing vm from vApp via vCloudDirector api
     *
     * @param name String, name of the vm configuration
     */
    @When("^remove vm (.+) from vApp$")
    public void remove_vm_from_vApp(String name) {
        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vdc = Storage.get("TestData." + name + ".Vdc");
        String vm_name = Storage.get("TestData." + name + ".VmName");
        String vapp = Storage.get("TestData." + name + ".VApp");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".VmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        //create new session
        ValidatableResponse session = CloudDirectorCore.login();

        //get organization using its name and login response
        String orgHref = CloudDirectorCore.getHrefOfOrganization(org, session);
        ValidatableResponse orgResp = CloudDirectorCore.getFromHref(orgHref);

        //get vdc
        String vdcHref = CloudDirectorCore.getHrefOfVdc(vdc, orgResp);
        ValidatableResponse vdcResp = CloudDirectorCore.getFromHref(vdcHref);

        //get app url
        String appHref = CloudDirectorCore.getHrefOfVApp(vapp, vdcResp);
        ValidatableResponse appResp = CloudDirectorCore.getFromHref(appHref);

        //get vm url
        String vmHref = CloudDirectorCore.getHrefOfVm(vm_name, appResp);

        //remove vm from app
        CloudDirectorCore.removeVmFromVApp(vmHref);

        //teardown session
        CloudDirectorCore.logout();
    }

}