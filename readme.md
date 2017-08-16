[![Build Status](https://travis-ci.org/amvnetworks/amv-trafficsoft-datahub.svg?branch=master)](https://travis-ci.org/amvnetworks/amv-trafficsoft-datahub)

amv-trafficsoft-datahub
========

# build
```
./gradlew clean build
```

# todo
- make all optional attributes "nullable" in database structure

# usage
The [application.yml](src/main/resources/application.yml) acts as a 
template for your own configuration parameter.

## configuration
Copy the contents of the `application.yml` file to `application-my-profile.yml`
and start the application with `--spring.profiles.active=my-profile`.
Or simply adapt the `application.yml` contents to your needs.
