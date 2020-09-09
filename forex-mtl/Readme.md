# A local proxy for Forex rates

This repository contains implementation of a local proxy for getting Currency Exchange Rates.
 
See also [Forex requirements](https://github.com/paidy/interview/blob/master/Forex.md) 

## Running the project

Project requires [one-frame](https://hub.docker.com/r/paidyinc/one-frame) and Redis instances.

To run them both run:

```
> ./startContainers.sh
``` 
To stop and remove containers:

```
> ./rmContainers.sh
``` 

Start the application:

```
> sbt run
```

After the program successfully started query rates for pair of currencies: http://localhost:8081/rates?from=AUD&to=USD


### Running tests
Before starting tests make sure that one-frame and redis containers are up (as described in `Running the project` section)

Then run:

```
> sbt test
```

## Assumptions / limitations

Due to limited amount of time the following issues are still present in code:
  - it is possible to get out of one-frame limit on specific condition 
  (high number of parallel requests each 5 minutes with no requests between). 
  In order to fix this cache refresh operation should be protected by semaphore.
  
```
    Sync[F].bracket(cacheLock.acquire)(tryQueryAndRefresh(pairs))(cacheLock.release)

```  
   
 - program doesn't handle `one-frame` error responses
 - errors hierarchy doesn't reflect all possible error conditions.
 - queries to Redis and one-frame may throw exceptions which should be handled (converted to errors.Error)
 - missing test for cache invalidation after Rate expiration time 
 - tests require redis and one-frame containers to be started. (Redis should be empty)
 - 