@removeVm
Feature: RemoveVm

######################################################
##                                                  ##
## this feature removes an existing Vm from a Vapp! ##
##                                                  ##
######################################################

  Scenario: remove VM using vCloudDirector Rest api

    When remove vm myVmName1 from vApp