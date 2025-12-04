# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [3.0.0] - 2025-12-04

- Upgraded to Java 21 and Spring Boot 4

## [2.0.0] - 2019-08-29

- Upgraded to Java 11  
Java 8 is no longer supported in this version.


## [1.0.1] - 2019-03-18
### Fixed
- Issue [#14](https://github.com/42BV/heph/issues/14) **ClassCastException when passing a `Collection` of entities in a `Supplier` in a `BuildCommand`**; Collections can now be mapped as well.
- Issue [#13](https://github.com/42BV/heph/issues/13) **NoClassDefFoundError when launching without BeanMapper on the classpath**; The library had a hard dependency on BeanMapper but it was only required to copy entities. Heph can now be used without BeanMapper, except for the `copy` function.

## [1.0.0] - 2019-01-25
### Added
This version makes the usage of Heph much easier by offering auto-generated implementations of the value setter methods within your BuildCommands.
Also, it's no longer required to supply a set of `BuilderConstructors` and implement a `getRepository` method for each builder.

### BREAKING changes
Builder classes now have to be declared as `interfaces` extending `AbstractBuildCommand<Entity, Repository>` and need to be placed in their own source file

To convert your existing builders, perform the following steps:
- Move the `BuildCommand` of your builder to its own source file (if it was an inner class). 
- Make the `BuildCommand` an interface extending `AbstractBuildCommand<Entity, Repository>`. This means you must add the type of your repository.
  - If your entity does *not* have a Spring / BeanSaver-implementing repository, change the second parameter of `AbstractBuildCommand` to `NoOpBeanSaver`.
- Remove all constructors and the `getRepository()` method from your BuildCommand interface
- Make the overridden `findEntity` method `default` and use `getRepository()` to find your entity using the repository.
- Make all other methods in your BuildCommand `default`
  - If any of these methods is named `with<something>` and just sets a value to your `Entity`, you should try removing the body to unleash the true power of Heph
- Rename the imports of `nl._42.heph.LazyEntity` and its derivatives to `nl._42.heph.lazy.LazyEntity`
- Replace all variables (like lists of stored values or callbacks) with calls to `getValue(TAG)` and `putValue(TAG, value)`. 
  - This is required, because Java interfaces can only contain static variables, and we want to store our values within the instance of the buildCommand.
- If no longer needed, remove the `autowired` Repository from your `Builder` class. 
  - The `Builder` class should still implement the `base` method and is supposed to contain your fixtures. 

## [0.1.1] - 2018-05-18
### Fixed
- Fixed the Spring version mismatch, resulting in a mixup of Persistable interfaces, which changed over Spring versions. Persistable is used by LazyEntityId to retrieve the ID of the object. The symptom of the error was that the Persistable.getId() method could not be found.

## [0.1.0] - 2018-05-17
### Added
- First release, containing the harvested classes from Postduif. 
