package libs.libCore.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import libs.libCore.modules.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;

public class CoreCloudDirectorSteps extends BaseSteps {

    /**
     * Adds a remote hostname or fqdn to the trusted host table
     *
     * @param node String, node name from winrm configuration of the remote that shall be added
     */
    @Given("^add node (.+) to trusted hosts$")
    public void add_node_to_trusted_hosts(String node){
        String address = Storage.get("Environment.Active.WinRM." + node + ".host");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        WinRSCore.addToTrustedHosts(address);

    }

    /**
     * periodically checks if DNS entry was updated and ip address of new vm can be resolved to its hostname
     * checks are done every minute with timeout 55 minutes
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    @Then("^check that DNS entry for remote host (.+) was updated$")
    public void check_that_DNS_entry_for_remote_host_was_updated(String node) {
        File workingDir = FileCore.getTempDir();
        String address = Storage.get("Environment.Active.WinRM." + node + ".host");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        //check every minute for 55 minutes if DNS entry was updated and we can access VM using its hostname
        Log.debug("Checking if DNS entry was updated for host with address " + address + " with timeout of 55 minutes");
        Integer numberOfChecks = 55;
        Integer i=0;
        String result = "";
        while ( i < numberOfChecks ) {
            ByteArrayOutputStream out = ExecutorCore.execute("nslookup " + address, workingDir, 60, true);
            result = new String(out.toByteArray(), Charset.defaultCharset());
            Log.debug("Output is " + result);

            if ( ! (result.contains("can't find")) ) {
                break;
            } else {
                StepCore.sleep(60);
            }

            i++;
        }

        if ( result.contains("can't find") ) {
            Log.error("Timeout. DNS entry was not updated");
        }

    }

    /**
     * checks if remote host is accessible and winrm connection using winrs client can be established
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    @Given("^remote host (.+) is accessible$")
    public void remote_host_is_accessible(String node) {
        String address = Storage.get("Environment.Active.WinRM." + node + ".host");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        //WA for Kerberso issues
        WinRSCore.awaitForHostAvailability(node);

    }


    /**
     * Sets new password for Ms Sql instance on newly created vm
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     * @param password String, new ms sql password
     */
    @When("^on remote host (.+) set new MS SQL password (.+) for user sa$")
    public void remote_host_set_new_MS_SQL_password_for_user_sa(String node, String password) {
        String pass = StepCore.checkIfInputIsVariable(password);

        Log.debug("Setting new MSSQL password for user sa");

        String script = "temp.bat";
        String cmd = "call osql -E -Q \"exec sp_password NULL, '" + pass + "', 'sa'\"";
        WinRSCore.transferScript(node, cmd, script);
        WinRSCore.executeSingleCommandOnVM("call " + script, node, 120);

    }


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
        String vm_name = Storage.get("TestData." + name + ".NewVmName");
        String network = Storage.get("TestData." + name + ".Network");

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getCatalog(catalog);
        CloudDirectorCore.getCatalogItem(catalogItem);
        String vAppTemplateUrl = CloudDirectorCore.getVAppTemplate(template);
        CloudDirectorCore.getVmTemplateFromVAppTemplate(vm);
        String vdcUrl = CloudDirectorCore.getVdc(vdc);
        String networkUrl = CloudDirectorCore.getNetwork(network);

        CloudDirectorCore.createNewVAppFromTemplate(vdcUrl, vAppTemplateUrl, vm_name, network, networkUrl);
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
        String vm_name = Storage.get("TestData." + name + ".NewVmName"); //name of the vm to be deployed in vApp
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
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
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

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getCatalog(catalog);
        CloudDirectorCore.getCatalogItem(catalogItem);
        CloudDirectorCore.getVAppTemplate(template);
        String vmTemplateUrl = CloudDirectorCore.getVmTemplateFromVAppTemplate(vm);
        CloudDirectorCore.getVdc(vdc);
        String networkUrl = CloudDirectorCore.getNetwork(network);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);

        CloudDirectorCore.addVmToVapp(vAppUrl, vmTemplateUrl, vapp, vm_name, network, networkUrl, ipAllocationMode);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.setGuestCustomization(newVmUrl);

        String memorySize = Storage.get("TestData." + name + ".Memory");
        if ( memorySize != null && ! memorySize.equals("") ){
            CloudDirectorCore.setVmMemory(newVmUrl, memorySize);
        } else {
            Log.warn("Value of TestData." + name + ".Memory not set or empty. No RAM memory adjustment will be done");
        }

        String numberOfCpu = Storage.get("TestData." + name + ".Cpu");
        if ( numberOfCpu != null && ! numberOfCpu.equals("") ) {
            CloudDirectorCore.setVmCpu(newVmUrl, numberOfCpu);
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
            CloudDirectorCore.setVmDisk(newVmUrl, initialDiskSize, diskSize);
        } else {
            Log.warn("Value of TestData." + name + ".DiskSize not set or empty. No disk capacity adjustment will be done.");
        }

        CloudDirectorCore.deployVm(newVmUrl);
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
        String vm_name = Storage.get("TestData." + name + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        //un-deploy
        Log.debug("Forced recofniguration - power off (undeploy)");
        CloudDirectorCore.powerOffVm(newVmUrl);
        //issue put request to set needsCustomization=true in vm params
        //Log.debug("Forced recofniguration - set needs customization flag to true");
        //vmMgmt.setNeedsCustomizationFlagForVm(newVmUrl);
        //deploy the vm with flag force recustomization set
        Log.debug("Forced recofniguration - deploy with force customization");
        CloudDirectorCore.deployVm(newVmUrl);
        //power on vm
        Log.debug("Forced recofniguration - power on");
        CloudDirectorCore.powerOnVm(newVmUrl);
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
        String vm_name = Storage.get("TestData." + name + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.powerOnVm(newVmUrl);
        CloudDirectorCore.logout();
    }


    /**
     * Disconnects vm from network
     * @param name String, name of the vm configuration
     */
    @When("^disconnect network for vm (.+)$")
    public void disconnect_network_for_vm(String name)  {

        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.disconnectNetworkConnection(newVmUrl);
        CloudDirectorCore.logout();
    }


    /**
     * Connects back vm to network
     * @param name String, name of the vm configuration
     */
    @When("^connect network for vm (.+)$")
    public void connect_network_for_vm(String name) {
        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.connectNetworkConnection(newVmUrl);
        CloudDirectorCore.logout();
    }


    /**
     * Returns VM details from cloudDirector
     * @param name String, name of the vm configuration
     */
    @When("^get network cards details for vm (.+)$")
    public void get_network_cards_details(String name) {
        String org = Storage.get("Environment.Active.vCloudDirector.org");
        String vm_name = Storage.get("TestData." + name + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.getVmNetworkCardsDetails(newVmUrl);
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
        String vm_name = Storage.get("TestData." + name + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.powerOffVm(newVmUrl);
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
        String vm_name = Storage.get("TestData." + name + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed
        String vdc = Storage.get("TestData." + name + ".Vdc");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String newVmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.rebootVm(newVmUrl);
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
        String vm_name = Storage.get("TestData." + name + ".NewVmName"); //name of the vm to be deployed in vApp
        String vapp = Storage.get("TestData." + name + ".VApp");//name of the vApp where vm shall be deployed

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String vmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.getVmDetails(vmUrl);
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
        String vm_name = Storage.get("TestData." + name + ".NewVmName");
        String vapp = Storage.get("TestData." + name + ".VApp");

        if ( vm_name == null ) {
            Log.error("Value of TestData." + name + ".NewVmName null or empty!");
        }
        if ( vapp == null ) {
            Log.error("Value of TestData." + name + ".VApp null or empty!");
        }
        if ( vdc == null ) {
            Log.error("Value of TestData." + name + ".Vdc null or empty!");
        }

        CloudDirectorCore.login();
        CloudDirectorCore.getOrganization(org);
        CloudDirectorCore.getVdc(vdc);
        String vAppUrl = CloudDirectorCore.getVApp(vapp);
        String vmUrl = CloudDirectorCore.getVmFromVApp(vm_name, vAppUrl);
        CloudDirectorCore.removeVmFromVApp(vmUrl);
        CloudDirectorCore.logout();
    }


    /**
     * Forces soft restart of machine by calling shutdown -r command
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    @When("^on remote host (.+) force restart$")
    public void on_remote_host_force_restart(String node) {

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");

        if ( address == null ){
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        Log.debug("Rebooting remote host " + address);
        WinRSCore.executeSingleCommandOnVM("shutdown -r -t 30 -f", node, 30);

        StepCore.sleep(180);

        //WA for Kerberos issues
        WinRSCore.awaitForHostAvailability(node);

    }


    /**
     * Sets default user on a newly created VM
     *
     * @param node String, node name from winrm configuration of the remote that shall be used
     */
    @When("^on remote host (.+) set default user$")
    public void on_remote_host_set_default_user(String node) {

        String address = Storage.get("Environment.Active.WinRM." + node + ".host");
        String user = Storage.get("Environment.Active.WinRM." + node + ".user");

        if (address == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }
        if (user == null) {
            Log.error("Configuration for Environment.Active.WinRM." + node + ".host not found or null!");
        }

        //
        // this is needed to force HF installation as a super user and wfcautouser!
        Log.debug("Setting DefaultUserName to " + user);
        String cmd = "(Get-ItemProperty 'HKLM:\\Software\\Microsoft\\Windows NT\\CurrentVersion\\WinLogon').DefaultUserName";
        String results = WinRSCore.executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
        if (!results.trim().equals(user)) {
            cmd = "Set-ItemProperty -Path 'HKLM:\\Software\\Microsoft\\Windows NT\\CurrentVersion\\WinLogon' -Name DefaultUserName -Value " + user.toLowerCase();
            WinRSCore.executeSingleCommandOnVM("Powershell.exe \"" + cmd + "\"", node, 120);
        }
    }


}