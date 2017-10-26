[![Build Status](https://travis-ci.org/amvnetworks/amv-trafficsoft-datahub.svg?branch=master)](https://travis-ci.org/amvnetworks/amv-trafficsoft-datahub)

amv-trafficsoft-datahub
========

# todo
- make all optional attributes "nullable" in database structure

# usage
The [application.yml](src/main/resources/application.yml) acts as a 
template for your own configuration parameter.

## configuration
Copy the contents of the `application.yml` file to `application-my-profile.yml`
and start the application with `--spring.profiles.active=my-profile`.
Or simply adapt the `application.yml` contents to your needs.


# build
Build a snapshot from a clean working directory
```bash
$ ./gradlew releaseCheck clean build -Prelease.stage=SNAPSHOT -Prelease.scope=patch
```

When a parameter `minimal` is provided, certain tasks will be skipped to make the build faster.
e.g. `findbugs`, `checkstyle`, `javadoc` - tasks which results are not essential for a working build.
```bash
./gradlew clean build -Pminimal
```

## publish SNAPSHOT to local maven repository
```
./gradlew clean build -Pminimal -Prelease.stage=SNAPSHOT -Prelease.scope=patch publishToMavenLocal
```

## create a release
```bash
./gradlew final -Prelease.scope=patch
```

## release to bintray
```bash
./gradlew clean build final bintrayUpload
  -Prelease.useLastTag=true
  -PreleaseToBintray
  -PbintrayUser=${username}
  -PbintrayApiKey=${apiKey}
```

# license
The project is licensed under the Apache License. See [LICENSE](LICENSE) for details.

