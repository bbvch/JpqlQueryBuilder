# JPQL Query Builder for unmanaged POJOs

This project contains sample code to the Blog post _Why not query POJOs with JPQL?_ on bbv site. The post outlines an idea, how the JPQL `SELECT NEW(...)` feature for querying unmanaged POJOs can be made more manageable and maintainable using byte-code generation.

The project is build by Maven and contains some extensions not described in the Blog post:

* Cache of enhanced classes to avoid Meta space pollution
* Separation of query builder class and byte-code enhancer

Some notes on the testing part of the project:

* The tests are executed against H2 database in in-memory mode.
* Custom JUnit rule take care of setting up required JPA environment.
* The persistence unit definition forces creation of database schema (but only once, before the first test is executed).
* Since only test code actually requires persistence unit and database, according dependencies are defined in test scope.
