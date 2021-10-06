package com.graalvmonlambda.infra;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.StackProps;

public class InfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

        new InfrastructureStack(app, "MicronautSample", StackProps.builder().build());

        app.synth();
    }
}
