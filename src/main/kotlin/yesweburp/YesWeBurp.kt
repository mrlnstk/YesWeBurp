package yesweburp

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import yesweburp.ui.YesWeBurpTab

class YesWeBurp : BurpExtension {
    override fun initialize(montoyaApi: MontoyaApi) {
        burpApi = montoyaApi

        burpApi.extension().setName("YesWeBurp $VERSION")
        burpApi.userInterface().registerSuiteTab("YesWeBurp", YesWeBurpTab())
    }
}
