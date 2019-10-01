# Custom JMeter sampler for authenticated S3 requests

This is a custom java sampler class that can be used to benchmark any S3 compatible system.  There is a class for GET/PUT and a class for listing.

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

### S3Sampler

Add a new JMeter Java sampler, use the `protocol.java.org.apache.jmeter.protocol.java.sampler.S3Sampler` class.
![Alt text](img/jmeter1.png?raw=true "Select JMeter custom sampler")

Add your AWS key id, bucket, object and the rest.
![Alt text](img/jmeter2.png?raw=true "Configure JMeter sampler")

### S3ListSampler

Add a new JMeter Java sampler, use the `protocol.java.org.apache.jmeter.protocol.java.sampler.S3ListSampler` class.

Add your AWS key id, bucket, prefix and the rest.

Caveat: S3ListSampler will still return successful if an empty list is returned.
