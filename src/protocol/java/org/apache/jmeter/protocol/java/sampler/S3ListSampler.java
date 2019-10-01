package protocol.java.org.apache.jmeter.protocol.java.sampler;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class S3ListSampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final Logger log = LoggingManager.getLoggerForClass();

    // set up default arguments for the JMeter GUI
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("bucket", "");
        defaultParameters.addArgument("path", "");
        defaultParameters.addArgument("key_id", "");
        defaultParameters.addArgument("secret_key", "");
        defaultParameters.addArgument("proxy_host", "");
        defaultParameters.addArgument("proxy_port", "");
        defaultParameters.addArgument("endpoint", "");
        defaultParameters.addArgument("region", "");
        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        String bucket = context.getParameter("bucket");
        String path = context.getParameter("path");
        String key_id = context.getParameter("key_id");
        String secret_key = context.getParameter("secret_key");
        String proxy_host = context.getParameter("proxy_host");
        String proxy_port = context.getParameter("proxy_port");
        String endpoint = context.getParameter("endpoint");
        String region = context.getParameter("region");

        SampleResult result = new SampleResult();

        result.sampleStart(); // start stopwatch
        try {
            ClientConfiguration config = new ClientConfiguration();
            if (proxy_host != null && !proxy_host.isEmpty()) {
                config.setProxyHost(proxy_host);
            }
            if (proxy_port != null && !proxy_port.isEmpty()) {
                config.setProxyPort(Integer.parseInt(proxy_port));
            }

            AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withClientConfiguration(config).withRegion(region);
            if (key_id != null && !key_id.isEmpty()) {
                builder = builder.withCredentials(new StaticCredentialsProvider(new BasicAWSCredentials(key_id, secret_key)));
            }

            if (endpoint != null && !endpoint.isEmpty()) {
                builder = builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region));
            }
            AmazonS3 s3Client = builder.build();

            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(bucket)
                    .withPrefix(path);
            ObjectListing objectListing;
            List<String> summaries = new ArrayList<>();
            do {
                objectListing = s3Client.listObjects(listObjectsRequest);
                for (S3ObjectSummary objectSummary :
                    objectListing.getObjectSummaries()) {
                    summaries.add(objectSummary.getKey());
                }
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
            result.setResponseData(String.join(",", summaries), "UTF-8");
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(true);
            result.setResponseCodeOK(); // 200 code
            result.setResponseMessageOK();

        } catch (Exception e) {
            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e);

            // get stack trace as a String to return as document data
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(stringWriter));
            result.setResponseData(stringWriter.toString());
            result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
            result.setResponseCode("500");
        }

        return result;
    }
}
