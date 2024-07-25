package com.burp.save;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;

public class RegisterMain implements BurpExtension {
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        Logging logging=montoyaApi.logging();
        logging.logToOutput("Loading successfully...");
        montoyaApi.extension().setName("Repeater requests saved");
        montoyaApi.userInterface().registerSuiteTab("RepeaterSave", RepeaterSave.burpUI.getRoot());
        montoyaApi.http().registerHttpHandler(new RepeaterSave(montoyaApi));
    }
}
