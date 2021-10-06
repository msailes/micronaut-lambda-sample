package com.graalvmonlambda.infra;

import java.util.Arrays;
import java.util.List;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpApiProps;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.PayloadFormatVersion;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegration;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegrationProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;

import static java.util.Collections.singletonList;
import static software.amazon.awscdk.core.BundlingOutput.ARCHIVED;

public class InfrastructureStack extends Stack {

    public InfrastructureStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public InfrastructureStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        Table moviesTable = new Table(this, "MoviesTable", TableProps.builder()
                .tableName("books")
                .partitionKey(Attribute.builder().type(AttributeType.STRING)
                        .name("id").build())
                .build());

        Function bookFunction = new Function(this, "MNBookFunction", FunctionProps.builder()
                .runtime(Runtime.PROVIDED_AL2)
                .code(Code.fromAsset("../software/target/function.zip"))
                .handler("example.micronaut.BookRequestHandler")
                .memorySize(256)
                .logRetention(RetentionDays.ONE_WEEK)
                .tracing(Tracing.ACTIVE)
                .build());

        HttpApi httpApi = new HttpApi(this, "MicronautTest", HttpApiProps.builder()
                .apiName("MicronautTest")
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/book")
                .methods(singletonList(HttpMethod.POST))
                .integration(new LambdaProxyIntegration(LambdaProxyIntegrationProps.builder()
                        .handler(bookFunction)
                        .payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                        .build()))
                .build());

        moviesTable.grantWriteData(bookFunction);

        CfnOutput apiUrl = new CfnOutput(this, "ApiUrl", CfnOutputProps.builder()
                .exportName("ApiUrl")
                .value(httpApi.getApiEndpoint())
                .build());
    }
}
