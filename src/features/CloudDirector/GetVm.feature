@getVm
Feature: GetVm

###################################################
##                                               ##
## this feature reads Vm properties from Vapp!   ##
##                                               ##
###################################################

  Scenario: get VM using vCloudDirector Rest api

    When check vm myVmName1 status