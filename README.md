# Getting Started

### How to build project & run tests

```shell
./gradlew clean build
```

### Accessing Swagger documentation

* [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


### Accessing the H2 Console
Application uses in-memory H2 database which is seeded with some data [HERE](src/main/java/com/tipico/dataseed/CampaignDataSeeder.java)

Console can be accessed under [http://localhost:8080/h2-console](http://localhost:8080/h2-console), using following configuration:
* JDBC URL: 'jdbc:h2:mem:testdb'
* User Name: 'sa'
* Password: ''
