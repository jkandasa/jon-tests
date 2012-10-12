package com.redhat.qe.jon.sahi.tests.plugins.eap6.standalone;

import net.sf.sahi.client.ElementStub;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.jon.sahi.tests.plugins.eap6.AS7PluginSahiTasks;

/**
 * check persiscence of connection settings on AS7 resource
 * @author jmartisk@redhat.com
 * see TCMS testcase 96430
 */
public class ResourceCreationTest extends AS7StandaloneTest {

    // this address will be set in the connection settings, should be any random address that isn't running any AS instance :)
    private static final String IP_ADDR = "239.12.33.74";

    @BeforeClass(groups = "resourceCreation001")
    protected void setupAS7Plugin() {
        as7SahiTasks = new AS7PluginSahiTasks(sahiTasks);
        as7SahiTasks.importResource(server);
    }

    @Test(groups = "resourceCreation001", alwaysRun=true)
    public void checkPersistenceOfChanges() {        
        server.inventory().connectionSettings();

        ElementStub startScript_element = sahiTasks.textbox("startScript");
        
        String old_startScript = startScript_element.getText();

        // set new values
        log.info("old start script = " + old_startScript + ", will try to set to \"abccbcblsd.sh\"");
        try {
            startScript_element.setValue("abccbcblsd.sh");
            sahiTasks.cell("Save").click();
        } finally {
            
        	server.inventory().connectionSettings();

            // check that the changes are persistent
            startScript_element = sahiTasks.textbox("startScript");
            String selm = startScript_element.getValue();
            
            log.fine("text in \"Start script\" after refreshing the page:" + selm);

            // return the values back
            ElementStub startScript_element2 = sahiTasks.textbox("startScript");
            startScript_element2.setValue(old_startScript);
            sahiTasks.cell("Save").click();

            Assert.assertEquals(selm, "abccbcblsd.sh", "Testing if changes to connection settings are persistent");
        }
    }

    @Test(groups = "resourceCreation001", alwaysRun=true)
    public void inputValidButIncorrectConnectionSettings() {                      
        server.inventory().connectionSettings();

        ElementStub hostname_element = sahiTasks.textbox("hostname");
        ElementStub port_element = sahiTasks.textbox("port");

        String old_hostname = hostname_element.getText();
        String old_port = port_element.getText();

        // set incorrect values
        log.info("old hostname = " + old_hostname + ", will try to set to " + IP_ADDR);
        log.info("old port = " + old_port + ", will try to set to " + Integer.toString(Integer.parseInt(old_port) + 1349));
        hostname_element.setValue(IP_ADDR);
        port_element.setValue(Integer.toString(Integer.parseInt(old_port) + 1349));
        sahiTasks.cell("Save").click();
        boolean ok = false;
        try {
            // the resource should go down after some time -- check for it

            for (int i = 0; i < 12; i++) {
                sahiTasks.waitFor(30000);
                log.fine("Checking that resource went offline: try #" + Integer.toString(i + 1) + " of 12");
                if (!server.isAvailable()) {
                    log.fine("Success - Resource went offline! Now I will change connection settings back to normal.");
                    ok = true;
                    break;
                }
            }
        } finally {
            // return the old values back
            server.inventory().connectionSettings();

            hostname_element = sahiTasks.textbox("hostname");
            port_element = sahiTasks.textbox("port");

            hostname_element.setValue(old_hostname);
            port_element.setValue(old_port);
            sahiTasks.cell("Save").click();
            log.fine("Connection settings restored back to correct state");
            // the resource should go up after some time -- check for it

            for (int i = 0; i < 12; i++) {
                sahiTasks.waitFor(30000);
                log.fine("Checking that resource is back online: try #" + Integer.toString(i + 1) + " of 12");
                if (server.isAvailable()) {
                    log.fine("Success - Resource is back online!");
                    break;
                }
            }

            if (!ok) {
                Assert.fail("AS7's connection settings were changed to incorrect, but the AS didn't appear offline even after more than 6 minutes");
            }
        }

    }
}