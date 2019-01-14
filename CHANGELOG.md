# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Unreleased
- Made the BuilderConstructors optional. Builders no longer have to override the method `constructors()` 
and BuildCommands no longer need 2 constructors (with Entity and Supplier<Entity>).

## [0.1.1] - 2018-05-18
### Fixed
- Fixed the Spring version mismatch, resulting in a mixup of Persistable interfaces, which changed over Spring versions. Persistable is used by LazyEntityId to retrieve the ID of the object. The symptom of the error was that the Persistable.getId() method could not be found.

## [0.1.0] - 2018-05-17
### Added
- First release, containing the harvested classes from Postduif. 