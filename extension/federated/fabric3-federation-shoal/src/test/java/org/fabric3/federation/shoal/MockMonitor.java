package org.fabric3.federation.shoal;

import org.fabric3.api.annotation.logging.Config;
import org.fabric3.api.annotation.logging.Fine;
import org.fabric3.api.annotation.logging.Finer;
import org.fabric3.api.annotation.logging.Finest;
import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.api.annotation.logging.Warning;

public class MockMonitor implements FederationServiceMonitor {

    @Info
    public void joined(String name, String runtimeName) {
        System.out.println("Joined domain " + name);
    }

    @Info
    public void exited(String name) {
        System.out.println("Exited domain " + name);
    }

    public void joinedControllerGroup(String groupName, String runtimeName) {

    }

    @Severe
    public void onException(String description, String domainName, Throwable throwable) {
        throwable.printStackTrace();
    }

    public void onSignalException(String description, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Severe
    public void onError(String description, String domainName) {
        System.out.println(description);
    }

    @Fine
    public void onSignal(String message) {

    }

    @Config
    public void onConfig(String message) {
        // ignore
    }

    @Info
    public void onInfo(String message) {
        System.out.println(message);
    }

    @Finer
    public void onFiner(String message) {
        // ignore
    }

    @Finest
    public void onFinest(String message) {
        // ignore
    }

    @Fine
    public void onFine(String message) {
        // ignore
    }

    @Warning
    public void onWarning(String message) {
        System.out.println(message);
    }

    @Severe
    public void onSevere(String message) {
        System.out.println(message);
    }

    public void joinedControllerGroup() {

    }
}
