package com.amazonaws.serverless.sample.spark;


import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.testutils.Timer;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class StreamLambdaHandler implements RequestStreamHandler {
    private boolean isInitialized = false;
    private SparkLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    private Logger log = LoggerFactory.getLogger(StreamLambdaHandler.class);

    public StreamLambdaHandler() {
        // we enable the timer for debugging. This SHOULD NOT be enabled in production.
        Timer.enable();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        if (!isInitialized) {
            isInitialized = true;
            try {
                handler = SparkLambdaContainerHandler.getAwsProxyHandler();
                SparkResources.defineResources();
                Spark.awaitInitialization();
            } catch (ContainerInitializationException e) {
                log.error("Cannot initialize Spark application", e);
                return;
            }
        }

        AwsProxyRequest request = LambdaContainerHandler.getObjectMapper().readValue(inputStream, AwsProxyRequest.class);

        AwsProxyResponse resp = handler.proxy(request, context);

        LambdaContainerHandler.getObjectMapper().writeValue(outputStream, resp);

        System.err.println(LambdaContainerHandler.getObjectMapper().writeValueAsString(Timer.getTimers()));

        // just in case it wasn't closed by the mapper
        outputStream.close();
    }
}
