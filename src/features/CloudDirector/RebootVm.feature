@rebootVm
Feature: RebootVm

######################################################
##                                                  ##
## this feature reboots an existing Vm in a Vapp!   ##
##                                                  ##
######################################################

  Scenario: reboots VM using vCloudDirector Rest api

    When reboot vm myVmName1
    Then check vm myVmName1 status