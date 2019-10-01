# Custom JMeter sampler for authenticated S3 requests

This is a custom java sampler class that can be used to benchmark any S3 compatible system.

[![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/wheresalice/jmeter_s3_custom_sampler?sort=semver&label=Release)](https://github.com/WheresAlice/jmeter_s3_custom_sampler/releases)

* Maintained by: WheresAlice
* Additional development by: ansoni
* Originally written by: Alex Bordei @ Bigstep

## How to use

Build using maven (or download from Releases)

```shell script
mvn package
```

Extract zip into JMeter directory

```shell script
cd jmeter_home
unzip path/to/jmeter_s3_custom_sampler/target/*.zip
```

Run JMeter as usual

```shell script
./bin/jmeter
```

Add a new jmeter Java sampler, use the com.bigstep.S3Sampler class.
![Alt text](img/jmeter1.png?raw=true "Select JMeter custom sampler")

Add your AWS key id, bucket, object and the rest.
![Alt text](img/jmeter2.png?raw=true "Configure JMeter sampler")

When testing against another system then AWS use the proxy settings to redirect requests somewhere else. Please note that the requests will still have the original host header pointing to amazonaws.com but the system should handle the requests nontheless.

Only GET and PUT methods are currently implemented but others should be very easy to add.