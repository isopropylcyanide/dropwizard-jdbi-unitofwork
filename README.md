## Dropwizard Jdbi - Unit of Work Support

![Travis (.org)](https://img.shields.io/travis/isopropylcyanide/dropwizard-jdbi-unitofwork)
![Codecov](https://img.shields.io/codecov/c/github/isopropylcyanide/dropwizard-jdbi-unitofwork?color=green)
![GitHub](https://img.shields.io/github/license/isopropylcyanide/dropwizard-jdbi-unitofwork?color=blue)
![Maven Central](https://img.shields.io/maven-central/v/com.github.isopropylcyanide/dropwizard-jdbi-unitofwork)

Provides `@JdbiUnitOfWork` annotation for a Jdbi backed Dropwizard backend for wrapping resource methods in a
transaction context

- [`Dropwizard`](https://github.com/dropwizard/dropwizard) provides a very
  slick [`@UnitOfWork`](https://www.dropwizard.io/en/latest/manual/hibernate.html) annotation that wraps a transaction
  context around resource methods annotated with this annotation. This is very useful for wrapping multiple calls in a
  single database transaction all of which will succeed or roll back atomically.

- However this support is only available for `Hibernate`. For all the goodness [`Jdbi`](http://jdbi.org/jdbi2/) brings,
  we had to bring the transactionality on our own. This module provides support for `JdbiUnitOfWork` with a `Jdbi`
  backend

## Maven Artifacts

This project is available on Maven Central. To add it to your project you can add the following dependency to your
`pom.xml`:

    <dependency>
        <groupId>com.github.isopropylcyanide</groupId>
        <artifactId>dropwizard-jdbi-unitofwork</artifactId>
        <version>1.2</version>
     </dependency>

## Features

- `transactionality` across multiple datasources when called from a request thread
- `transactionality` across multiple datasources across `multiple threads`
- `excluding`, selectively, certain set of URI's from transaction contexts, such as `ELB`, `Health Checks` etc
- `Http GET` methods are excluded from transaction by default.
- `Http POST` methods are wrapped around in a transaction only when annotated with `@JdbiUnitOfWork`

## Usage (tl;dr)

## Usage (With Guice modules)

- Add the dependency to your `pom.xml`

- Decide which implementation of `JdbiHandleManager` is suitable for your use case.

  ```java
  JdbiHandleManager handleManager = new RequestScopedJdbiHandleManager(dbi);  // most common
  or 
  JdbiHandleManager handleManager = new LinkedRequestScopedJdbiHandleManager(dbi);
  ```

  If you are using Guice, you can bind the instance
  ```
  bind(JdbiHandleManager.class).toInstance(new RequestScopedJdbiHandleManager(dbi));
  ```

<br>

- Provide the list of package where the SQL Objects / DAO (to be attached) are located. Classes with Jdbi
  annotations `@SqlQuery` or `@SqlUpdate` or `@SqlBatch` or `@SqlCall` will be picked automatically.

  <br>

  A helper class `JdbiUnitOfWorkProvider`**` is provided to generate the proxies. You can also register the classes one
  by one.

  ```java
  JdbiUnitOfWorkProvider provider = new JdbiUnitOfWorkProvider(handleManager);
  
  // class level
  SampleDao dao = (SampleDao) provider.getWrappedInstanceForDaoClass(SampleDao.class);
  // use the proxies and pass it as they were normal instances
  // ...new SampleResource(dao)
  
  // package level
  List<String> daoPackages = Lists.newArrayList("<fq-package-name>", "fq-package-name", ...);
  Map<? extends Class, Object> proxies = unitOfWorkProvider.getWrappedInstanceForDaoPackage(daoPackages);
  // use the proxies and pass it as they were normal instances
  // ...new SampleResource((SampleDao)proxies.get(SampleDao.class))
  ```

<br>

- Finally, we need to register the event listener with the Jersey Environment
  ```
  Set<String> excludePaths = new HashSet<>()
  environment.jersey().register(new JdbiUnitOfWorkApplicationEventListener(handleManager, excludePaths));
  ```
  In case you'd like to exclude certain URI paths from being monitored, you can pass them into exclude paths;
  ```
  environment.jersey().register(new JdbiUnitOfWorkApplicationEventListener(handleManager, new HashSet<>()));
  ```

<br>

- Start annotating resource methods with `@JdbiUnitOfWork` and you're good to go.
    ```java
    @POST
    @Path("/")
    @JdbiUnitOfWork
    public RequestResponse createRequest() {
          ..do stateful work (across multiple Dao's)
          return response 
    }
    ```

## Design

- This library relies on `Jersey Monitoring Events` to bind request lifecycle with a transaction aspect
- At the backend, all `Jdbi` objects such as `Dao` or `SqlObjects` are proxied behind a `JdbiHandleManager`
- `JdbiHandleManager` contract specifies the `get` and `clear` of the actual handles to the calling thread.

![image](https://user-images.githubusercontent.com/12872673/80345369-a563d580-8886-11ea-9fd9-3b659ac1d75b.png)

## Support

Please file bug reports and feature requests
in [GitHub issues](https://github.com/isopropylcyanide/dropwizard-jdbi-unitofwork/issues).

## License

Copyright (c) 2020-2022 Aman Garg

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the LICENSE file in this repository for the full license text.
