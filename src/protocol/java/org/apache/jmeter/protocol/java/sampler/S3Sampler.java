/* Written By Alexandru Bordei
*  Bigstep.com
*  Distrubuted under the same license as apache jmeter itself.
* http://www.apache.org/licenses/LICENSE-2.0
*/
package protocol.java.org.apache.jmeter.protocol.java.sampler;


import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class S3Sampler extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final Logger log = LoggingManager.getLoggerForClass();

    // set up default arguments for the JMeter GUI
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("method", "GET");
        defaultParameters.addArgument("bucket", "");
        defaultParameters.addArgument("object", "");
        defaultParameters.addArgument("key_id", "");
        defaultParameters.addArgument("secret_key", "");
        defaultParameters.addArgument("local_file_path", "");
        defaultParameters.addArgument("proxy_host", "");
        defaultParameters.addArgument("proxy_port", "");
        defaultParameters.addArgument("endpoint", "");
        defaultParameters.addArgument("region", "");
        defaultParameters.addArgument("acl", "");
        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        // pull parameters
        String bucket = context.getParameter("bucket");
        String object = context.getParameter("object");
        String method = context.getParameter("method");
        String local_file_path = context.getParameter("local_file_path");
        String key_id = context.getParameter("key_id");
        String secret_key = context.getParameter("secret_key");
        String proxy_host = context.getParameter("proxy_host");
        String proxy_port = context.getParameter("proxy_port");
        String endpoint = context.getParameter("endpoint");
        String region = context.getParameter("region");
        String acl = context.getParameter("acl");

        log.debug("runTest:method=" + method + " local_file_path=" + local_file_path + " bucket=" + bucket + " object=" + object);

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
            ObjectMetadata meta = null;

            if (method.equals("GET")) {
                S3Object s3object = s3Client.getObject(bucket, object);
                S3ObjectInputStream stream = s3object.getObjectContent();
                if (local_file_path != null && !local_file_path.isEmpty()) {
                    Files.copy(stream, Paths.get(local_file_path), StandardCopyOption.REPLACE_EXISTING);
                }
                stream.close();
                meta = s3object.getObjectMetadata();
            } else if (method.equals("PUT")) {
                File file = new File(local_file_path);
                PutObjectRequest por = new PutObjectRequest(bucket, object, file);
                if (acl != null && !acl.isEmpty()) {
                  por = por.withCannedAcl(CannedAccessControlList.valueOf(acl));
                }

                s3Client.putObject(por);
            }

            result.sampleEnd(); // stop stopwatch
            result.setSuccessful(true);
            if (meta != null) {
                result.setResponseMessage("OK on url:" + bucket + "/" + object + ". Length=" + meta.getContentLength() + ". Last Modified=" + meta.getLastModified() + ". Storage Class=" + meta.getStorageClass());
            } else {
                result.setResponseMessage("OK on url:" + bucket + "/" + object + ".No metadata");
            }
            result.setResponseCodeOK(); // 200 code

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
