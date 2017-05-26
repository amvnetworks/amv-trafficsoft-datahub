[![Build Status](https://travis-ci.org/amvnetworks/amv-trafficsoft-restclient-demo.svg?branch=master)](https://travis-ci.org/amvnetworks/amv-trafficsoft-restclient-demo)

amv-trafficsoft-restclient-demo
========
amv-trafficsoft-restclient-demo is a simple Java demo application to demonstrate 
accessing the AMV Trafficsoft API with 
[amv-trafficsoft-rest](https://github.com/amvnetworks/amv-trafficsoft-rest).

# build
```
./gradlew clean build
```

# usage
The [application.yml](src/main/resources/application.yml) acts as a 
template for your own configuration parameter.

## configuration
Copy the contents of the `application.yml` file to `application-my-profile.yml`
and start the application with `--spring.profiles.active=my-profile`.
Or simply adapt the `application.yml` contents to your needs.

## examples
### LastData
See [LastDataRunner.java](src/main/java/org/amv/trafficsoft/restclient/demo/command/LastDataRunner.java).

Example of a simple `GetLastData` call which prints the result to console.
```
long contractId = 42;
List<Long> vehicleIds = ImmutableList.of(1337L, 9001L);

Action1<List<NodeRestDto>> onNext = nodeRestDtos -> {
    log.info("Received Nodes: {}", nodeRestDtos);
};
Action1<Throwable> onError = error -> {
    log.error("{}", error);
};
Action0 onComplete = () -> {
    log.info("Completed.");
};

log.info("==================================================");
this.xfcdClient.getLastData(contractId, vehicleIds)
        .toObservable()
        .subscribe(onNext, onError, onComplete);
log.info("==================================================");
```
Example output:
```
[main] INFO LastDataRunner - ==================================================
[main] INFO LastDataRunner - Received Nodes: [NodeRestDto(id=1, timestamp=Thu Apr 13 14:25:29 CEST 2017, longitude=10.084392, latitude=38.384910, speed=null, heading=null, altitude=null, satellites=null, hdop=null, vdop=null, xfcds=[ParameterRestDto(param=kmrd, value=19221, timestamp=Fri Apr 14 18:39:00 CEST 2017, longitude=10.084373, latitude=38.385097)], states=[ParameterRestDto(param=move, value=0, timestamp=Thu Apr 13 14:21:19 CEST 2017, longitude=null, latitude=null)])]
[main] INFO LastDataRunner - Completed.
[main] INFO LastDataRunner - ==================================================
```

### AllSeriesAndModelsOfOem
See [AllSeriesAndModelsOfOemRunner.java](src/main/java/org/amv/trafficsoft/restclient/demo/command/AllSeriesAndModelsOfOemRunner.java).

Example of how to fetch all series and models of all oems.
```
long contractId = 42;

Action1<SeriesWithModels> onNext = seriesWithModels -> {
    log.info("Received SeriesWithModels: {}", seriesWithModels);
};
Action1<Throwable> onError = error -> {
    log.error("{}", error);
};
Action0 onComplete = () -> {
    log.info("Completed.");
};

// fetch all series of an oem
Func1<OemRestDto, Observable<SeriesRestDto>> fetchSeriesOfOem = oem -> asgRegisterClient
        .getSeries(contractId, oem.getOemCode())
        .toObservable()
        .flatMap(seriesResponse -> Observable.from(seriesResponse.getSeries()));


// fetch all models of a series and transform it to objects holding the series and a list of models
Func1<SeriesRestDto, Observable<ModelRestDto>> fetchModelsOfSeries = series -> asgRegisterClient
        .getModels(contractId, series.getOemCode(), series.getSeriesCode())
        .toObservable()
        .flatMap(modelsResponse -> Observable.from(modelsResponse.getModels()));

log.info("==================================================");
this.asgRegisterClient.getOems(contractId)
        .toObservable()
        .flatMap(oemResponse -> Observable.from(oemResponse.getOems()))
        .flatMap(fetchSeriesOfOem)
        .flatMap(series -> fetchModelsOfSeries.call(series)
                .toList()
                .map(modelList -> SeriesWithModels.builder()
                        .series(series)
                        .models(modelList)
                        .build()))
        .subscribe(onNext, onError, onComplete);
log.info("==================================================");
```
Example output:
```
[main] INFO AllSeriesAndModelsOfOemRunner - ==================================================
[main] INFO AllSeriesAndModelsOfOemRunner - Received SeriesWithModels: AllSeriesAndModelsOfOemRunner.SeriesWithModels(series=SeriesRestDto(oemCode=AUDI, seriesCode=A1, name=A1), models=[ModelRestDto(oemCode=AUDI, seriesCode=A1, modelCode=A18X, name=A1 [8X] (2010/01 - ))])
[main] INFO AllSeriesAndModelsOfOemRunner - Received SeriesWithModels: AllSeriesAndModelsOfOemRunner.SeriesWithModels(series=SeriesRestDto(oemCode=AUDI, seriesCode=A3, name=A3), models=[ModelRestDto(oemCode=AUDI, seriesCode=A3, modelCode=A38P, name=A3 [8P] (2002/07 - 2012/06)), ModelRestDto(oemCode=AUDI, seriesCode=A3, modelCode=A38Pco, name=A3 Cabrio [8P] (2007/07 - 2013/06)), ...])
[main] INFO AllSeriesAndModelsOfOemRunner - Received SeriesWithModels: AllSeriesAndModelsOfOemRunner.SeriesWithModels(series=SeriesRestDto(oemCode=AUDI, seriesCode=A4, name=A4), models=[ModelRestDto(oemCode=AUDI, seriesCode=A4, modelCode=A4B6, name=A4 B6 [8E] (2000/07 - 2004/06)), ModelRestDto(oemCode=AUDI, seriesCode=A4, modelCode=A4B6conf, name=A4 B6 Cabrio [8H] (2002/07 - 2006/06)), ...])
[main] INFO AllSeriesAndModelsOfOemRunner - Received SeriesWithModels: AllSeriesAndModelsOfOemRunner.SeriesWithModels(series=SeriesRestDto(oemCode=AUDI, seriesCode=A5, name=A5), models=[ModelRestDto(oemCode=AUDI, seriesCode=A5, modelCode=A5B8, name=A5 B8 [8T] (2007/07 - )), ModelRestDto(oemCode=AUDI, seriesCode=A5, modelCode=A5B8co, name=A5 B8 Cabrio [8F] (2009/07 - ))])
[main] INFO AllSeriesAndModelsOfOemRunner - Received SeriesWithModels: AllSeriesAndModelsOfOemRunner.SeriesWithModels(series=SeriesRestDto(oemCode=AUDI, seriesCode=A6, name=A6/A7), models=[ModelRestDto(oemCode=AUDI, seriesCode=A6, modelCode=A6C5, name=A6 C5 [4B] (1997/07 - 2005/06)), ModelRestDto(oemCode=AUDI, seriesCode=A6, modelCode=A6C6, name=A6 C6 [4F] (2004/07 - 2011/06)), ...])
[main] INFO AllSeriesAndModelsOfOemRunner - Received SeriesWithModels: AllSeriesAndModelsOfOemRunner.SeriesWithModels(series=SeriesRestDto(oemCode=AUDI, seriesCode=A8, name=A8), models=[ModelRestDto(oemCode=AUDI, seriesCode=A8, modelCode=A8D4, name=A8 D4 [4H] (2010/07 - ))])
...
[main] INFO AllSeriesAndModelsOfOemRunner - Completed.
[main] INFO AllSeriesAndModelsOfOemRunner - ==================================================
```