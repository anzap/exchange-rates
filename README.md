# Exchange Rate Calculation sample app

## Application design

Application is designed in a classic Controller - Service - Repository architecture.
For simplicity it uses an in-memory H2 database for storing data, which means that information is not persisted between application restarts. Database schema evolution management is based on Flyway project.  
As the exchange rate provider BitPay service is used and specifically endpoint to fetch exchange rate between cryptocurrency and fiat currency https://bitpay.com/api/#rest-api-resources-rates-fetch-the-rates-used-by-bitpay-for-a-specific-cryptocurrency-fiat-pair.  


Package description:

* `api` package - Contains REST API controllers exposing endpoints, as well as validators to validate incoming requests based on Bean Validation framework and configuration for translating errors to useful api responses.  
Endpoints created as part of the exercise are `/api/rates` and `/api/rates/snapshots`, which fetch latest exchange rate and historical data respectively.  

  `/api/rates` - Accepts 2 optional parameters `in` and `out` for the currency to convert from and to respectively. If no params are passed in then the defaults are used `in: BTC` and `out: USD`

  `/api/rates/snapshots` - Accepts 4 optional parameters `in`, `out`, `from` and `to`. For the first 2 the same as in the previous endpoint apply. `from` and `to` form a date range to get historical data for. Their format is ISO based `yyyy-MM-dd'T'HH:mm:ss.SSSXXX`. At least `from` should be provided for the api to work correctly.

* `config` package - Contains utilities for application configuration based on application.properties as well as configuration for Swagger integration.

* `persistence` package - Contains JPA entity as well as Spring DATA JPA based repositories to save and fetch information from the database. Furthermore contains JPA Specification based criteria to help with building more dynamic db queries based on incoming request.

* `providers` package - Contains the api client for the exchange rate provider BitPay. Package is being built with extensibility in mind for later on adding more providers if needed. A common interface `CurrencyExchangeRateProvider` is being defined, that api clients must implement, as well as a specific exception `ProviderException` to be used to wrap any specific provider errors.   
The network call is using Spring Reactive Webclient project as a network client implementation.

* `business` package - Contains the core logic of the application. Main entrypoints are `ExchangeRateCollector`, a recurrent task to poll the providers for exchange rate information and save it to db. Secondly there is `ExchangeRateService` which serves requests coming in from controller and reports back results. `ExchangeRateService` has 2 methods to get latest rate for a currency set and get historic rates for a specific period.  
Historic rates are coming from database, while latest rate information is coming from the provider and if an error occurs, then falls back to database to get last saved information, if any.  
Package also contains DTOs used to wrap information served as JSON through the api as well as a mapper to convert from entity to DTO.  

Tech stack:

* JDK 11
* Spring Boot 2.3
* Maven
* H2 db
* Flyway
* Lombok
* mockwebserver for testing
* Docker

## Testing

For testing of the application the available spring boot provided utilities have been used to write unit and integration tests.  
Unit tests have been written for the controller, service, persistence and third party provider.  
Also an integration test with the help of MockWebServer for mocking 3rd party service request has been written to cover the exposed endpoints and test the application end-2-end.

## Running the application

You can run the application using maven-wrapper as follows:

```
./mvnw spring-boot:run
```
You will need jdk 11 installed locally for this to work.

Alternatively application comes with a Dockerfile for easy deployment. Dockerfile builds and creates a container to run the application based on a multi-stage docker build.
To run the application, give following commands:

```
docker build . -t exchange-rates
docker run -p 8080:8080 exchange-rates
```

Easiest way to test the provided API is through the integrates swagger-ui application available at http://localhost:8080/swagger-ui/index.html

## Further improvements

The obvious improvement that needs to be made to move to production would be to use a persistent medium to store rate information.  
Easiest solution as it is now would be to move to a relational database, but depending on the deployment environment another solution like for ex. ElasticSearch could be used that is easier to scale horizontaly if needed.  
If we stick with the relational database then a performance improvement that should be made is to mark the latest exchange rate entry in the db table at the time of insertion, as the current code uses `MAX(created_at)` to get the most recent entry, which will not be very efficient as the time goes on. 