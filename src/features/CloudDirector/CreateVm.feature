@createVm
Feature: CreateVm

###################################################
##                                               ##
## this feature adds new Vm to an existing Vapp! ##
##                                               ##
###################################################

  Scenario: create VM using vCloudDirector Rest api

    Given add new vm myVmName1 to vApp
    When powerOn vm myVmName1
    Then check vm myVmName1 status
      And check that DNS entry for remote host myVmName1 was updated