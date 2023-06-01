# Spring Boot Redis Cache


Context:

  - [**Getting Started**](#getting-started)
  - [**Maven Dependencies**](#maven-dependencies)
  - [**Redis Configuration**](#redis-configuration)
  - [**Spring Service**](#spring-service)
  - [**Docker & Docker Compose**](#docker--docker-compose)
  - [**PostgreSQL**](#access-postgresql)
  - [**Use Redisinsight**](#use-redisinsight)
  - [**Build & Run Application**](#build--run-application)
  - [**Endpoints with Swagger**](#endpoints-with-swagger)
  - [**Proof of caching**](#proof-point-for-the-caching)
  - [**Verify cached in redis**](#verify-the-data-is-now-cached-in-redis)
  - [**Demo**](#demo)

Links:

  - [**Spring Boot Cache Abstraction**](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/cache.html)
  - [**Stackoverflow Null Pointer Fix**](https://stackoverflow.com/questions/33083206/cacheevict-with-key-id-throws-nullpointerexception)
  - [**Baeldung Spring Boot Cache with Redis**](https://www.baeldung.com/spring-boot-redis-cache)
  - [**Spring Boot and Redis-SpEL/Caching**](https://ozymaxx.github.io/blog/2020/05/11/redis-springboot-2-en/)
  - [**Spring Caching with Redis**](https://medium.com/javarevisited/spring-caching-with-redis-dc1e3bdcc552)
  - [**Redisinsight**](https://redis.com/redis-enterprise/redis-insight/)
  - [**OpenAPI 3**](https://springdoc.org/v2/)
  - [**Using Redisinsights**](https://docs.redis.com/latest/ri/using-redisinsight/)

## Getting Started

In this project, I used Redis for caching with Spring Boot.  There are multiple docker containers:  postgres, redis, 
redisinsight, and the spring boot application.
When you send any request to get all customers or customer by id, you will wait 3 seconds if Redis has no related data.


## Maven Dependencies

I removed jedis since lettuce is also being used.
```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

```

## Redis Configuration

```java
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@Slf4j
@EnableCaching
public class RedisConfig {


  @Value("${spring.redis.host}")
  private String redisHost;

  @Value("${spring.redis.port}")
  private int redisPort;

  @Value("${spring.cache.redis.time-to-live}")
  private int cacheTtl;

  @Value("${spring.cache.redis.cache-null-values}")
  private boolean cacheNull;


  @Bean
  public RedisTemplate<String, Serializable> redisCacheTemplate(LettuceConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Serializable> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setConnectionFactory(redisConnectionFactory);
    log.info("redis host " + redisHost);
    log.info("redis port " + String.valueOf(redisPort));
    return template;
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
    RedisCacheConfiguration redisCacheConfiguration = config
            .entryTtl(Duration.ofMinutes(cacheTtl))
            .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    if (cacheNull) {
      redisCacheConfiguration.getAllowCacheNullValues();
    } else {
      redisCacheConfiguration.disableCachingNullValues();
    }
    RedisCacheManager redisCacheManager = RedisCacheManager.builder(factory).cacheDefaults(redisCacheConfiguration)
            .build();
    return redisCacheManager;
  }
}
```


## Spring Service

Spring Boot Customer Service Implementation will be like below class.
I used Spring Boot Cache @Annotations for caching.

These are:

* `@Cacheable`
* `@CacheEvict`
* `@Caching`
* `@CachceConfig`
	
Updated this code on the CacheEvict as it did not work. [Stackoverflow link](https://stackoverflow.com/questions/33083206/cacheevict-with-key-id-throws-nullpointerexception)
```java

@Service
@CacheConfig(cacheNames = "customerCache")
public class CustomerServiceImpl implements CustomerService {

  @Autowired
  private CustomerRepository customerRepository;

  @Cacheable(cacheNames = "customers", key = "#id")
  @Override
  public List<Customer> getAll() {
    waitSomeTime();
    return this.customerRepository.findAll();
  }

  @CacheEvict(cacheNames = "customers", key = "#id", condition = "#id!=null")
  @Override
  public Customer add(Customer customer) {
    return this.customerRepository.save(customer);
  }

  //  this causes all the entries to be deleted if any entries are updated
  // @CacheEvict(cacheNames = "customers", allEntries = true)
  //   this works but is kind of complex.  Here customer is the java class object (not customers)
  @CacheEvict(cacheNames = "customers", key = "#customer?.id", condition = "#customer?.id!=null")
  //  this seems logical, but it doesn't delete the redis cached record
  // @CacheEvict(cacheNames = "customers", key = "#id", condition = "#id!=null")
  @Override
  public Customer update(Customer customer) {
    Optional<Customer> optCustomer = this.customerRepository.findById(customer.getId());
    if (!optCustomer.isPresent())
      return null;
    Customer repCustomer = optCustomer.get();
    repCustomer.setName(customer.getName());
    repCustomer.setContactName(customer.getContactName());
    repCustomer.setAddress(customer.getAddress());
    repCustomer.setCity(customer.getCity());
    repCustomer.setPostalCode(customer.getPostalCode());
    repCustomer.setCountry(customer.getCountry());
    return this.customerRepository.save(repCustomer);
  }

  @Caching(evict = {@CacheEvict(cacheNames = "customers", key = "#id", condition = "#id!=null")})
  @Override
  public void delete(long id) {
    if (this.customerRepository.existsById(id)) {
      this.customerRepository.deleteById(id);
    }
  }

  @Cacheable(cacheNames = "customers", key = "#id", unless = "#result == null")
  @Override
  public Customer getCustomerById(long id) {
    waitSomeTime();
    return this.customerRepository.findById(id).orElse(null);
  }
}
```

## Docker & Docker Compose


*Dockerfile*

```
FROM maven:3.8.6-openjdk-18 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package -DskipTests

FROM openjdk:18
ENV DEBIAN_FRONTEND noninteractive
COPY --from=build /usr/src/app/target/spring-boot-redis-cache-0.0.1-SNAPSHOT.jar /usr/app/spring-boot-redis-cache-0.0.1-SNAPSHOT.jar
COPY --from=build /usr/src/app/src/main/resources/runApplication.sh /usr/app/runApplication.sh
EXPOSE 8080
ENTRYPOINT ["/usr/app/runApplication.sh"]
```

*Docker compose file*


*docker-compose.yml*

```yml
version: '3.9'

services:
  db:
    image: postgres
    container_name: db
    ports:
      - '5432:5432'
    environment:
      - POSTGRES_DB=postgres
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=ekoloji
  cache:
    image: redis/redis-stack:latest
    container_name: cache
    ports:
      - '6379:6379'
    environment:
      - ALLOW_EMPTY_PASSWORD=yes
      - REDIS_DISABLE_COMMANDS=FLUSHDB,FLUSHALL
  spring-cache:
    image: spring-cache
    container_name: spring-cache
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/postgres
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=ekoloji
      - SPRING_REDIS_HOST=cache
      - SPRING_REDIS_PORT=6379
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - '8080:8080'
    depends_on:
      - db
      - cache
  insight:
    image: "redislabs/redisinsight:latest"
    container_name: insight
    ports:
      - "8001:8001"
    volumes:
      - ./redisinsight:/db
    depends_on:
      - cache
  ```

## Build & Run Application

* Build Java Jar.

```shell
 $ source scripts/setEnv.sh
 $ mvn clean package
```

*  Docker Compose Build and Run

```shell
$ docker-compose build --no-cache
$ docker-compose up -d

```

## Use redisinsight
bring up redisinsight using [link to redisinsight](https://localhost:8001)
This [redisinsight documentation](https://docs.redis.com/latest/ri/using-redisinsight/) is helpful  

![redisinsight](assets/redisinsightConnection.png)

When adding the database use *cache* and not *localhost* as the server name as it will resolve with the docker network

## Access postgreSQL
```bash
docker exec -it db bash
psql -U postgres -W
# TIP:  find the password for the postgres database in the docker-compose file
```
![psql](assets/psql.png)
* see simple psql interaction above

## Insert some test records to get started
```shell
cd scripts
./putCustomer.sh
```
* NOTE:  these inserted records will go into postgres but will not be cached to redis as writes are not cached

## Endpoints with Swagger

Bring up the swagger interface as directed below
You can see the endpoint in `http://localhost:8080/swagger-ui.html` page.

## Proof point for the caching

![Endpoints](assets/selectCustomersGetwithID.png)
* Click on "customer-controller"
* Click on "GET" for the second customer API call (circled above) that gets customers by ID. 
* Click on *Try it out* (also circled)

![ClickExecute](assets/ClickExecute.png)
* Enter *1* for the id and click the large blue Execute button

![results](assets/resultsOfEnter.png)
* These results should occur

### Verify the data is now cached in redis
* go back to redisinsight browser
* select correct database connection

![browse](assets/redisinsightBrowse.png)
* Click on *Browser* on the left column below *BROWSE*
* Click on the record called customers::1
  * Should see the record with a TTL and all the columns as shown

This demonstrates the cache is working.

Can also use the API script to see the output speed difference more easily  
(there is a purposeful delay in get from postgres for demonstration purposes) 
```bash
cd scripts
./getByCustID.sh
```

### Actuator health and metrics
[Spring Boot Actuator](https://www.baeldung.com/spring-boot-actuators) is turned on in the [pom.xml](pom.xml)
Test is out using the [health actuator endpoint](http://localhost:8080/actuator/health) and other documented above
## Demo

<div align="center">
  <a href="https://www.youtube.com/watch?v=4yr4JLRK6MM"><img src="https://img.youtube.com/vi/4yr4JLRK6MM/0.jpg" alt="Spring Boot + Redis + PostgreSQL Caching"></a>
</div>

