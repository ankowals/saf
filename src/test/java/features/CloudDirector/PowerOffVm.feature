@powerOffVm
Feature: PowerOffVm

######################################################
##                                                  ##
## this feature power off an existing Vm in a Vapp! ##
##                                                  ##
######################################################

  Scenario: power offs VM using vCloudDirector Rest api

    When powerOff vm myVmName1
    Then check vm myVmName1 status