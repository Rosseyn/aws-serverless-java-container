# Serverless Java container [![Build Status](https://travis-ci.org/awslabs/aws-serverless-java-container.svg?branch=master)](https://travis-ci.org/awslabs/aws-serverless-java-container) [![Help](http://img.shields.io/badge/help-gitter-E91E63.svg?style=flat-square)](https://gitter.im/awslabs/aws-serverless-java-container)
The `aws-serverless-java-container` makes it easy to run Java applications written with frameworks such as [Spring](https://spring.io/), [Spring Boot](https://projects.spring.io/spring-boot/), [Jersey](https://jersey.java.net/), or [Spark](http://sparkjava.com/) in [AWS Lambda](https://aws.amazon.com/lambda/).

Serverless Java Container natively supports API Gateway's proxy integration models for requests and responses, you can create and inject custom models for methods that use custom mappings.

Follow the quick start guides in [our wiki](https://github.com/awslabs/aws-serverless-java-container/wiki) to integrate Serverless Java Container with your project:
* [Spring quick start](https://github.com/awslabs/aws-serverless-java-container/wiki/Quick-start---Spring)
* [Spring Boot quick start](https://github.com/awslabs/aws-serverless-java-container/wiki/Quick-start---Spring-Boot)
* [Jersey quick start](https://github.com/awslabs/aws-serverless-java-container/wiki/Quick-start---Jersey)
* [Spark quick start](https://github.com/awslabs/aws-serverless-java-container/wiki/Quick-start---Spark)   

Below is the most basic AWS Lambda handler example that launches a Spring application. You can also take a look at the [samples](https://github.com/awslabs/aws-serverless-java-container/tree/master/samples) in this repository, our main wiki page includes a [step-by-step guide](https://github.com/awslabs/aws-serverless-java-container/wiki#deploying-the-sample-applications) on how to deploy the various sample applications using Maven and [SAM](https://github.com/awslabs/serverless-application-model). 

```java
public class StreamLambdaHandler implements RequestStreamHandler {
    private SpringLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    private Logger log = LoggerFactory.getLogger(StreamLambdaHandler.class);

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        if (handler == null) {
            try {
                handler = SpringLambdaContainerHandler.getAwsProxyHandler(PetStoreSpringAppConfig.class);
            } catch (ContainerInitializationException e) {
                log.error("Cannot initialize Spring container", e);
                outputStream.close();
                throw new RuntimeException(e);
            }
        }

        AwsProxyRequest request = LambdaContainerHandler.getObjectMapper().readValue(inputStream, AwsProxyRequest.class);

        AwsProxyResponse resp = handler.proxy(request, context);

        LambdaContainerHandler.getObjectMapper().writeValue(outputStream, resp);
        // just in case it wasn't closed by the mapper
        outputStream.close();
    }
}
``` 
