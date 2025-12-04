[![Java CI with Maven](https://github.com/42BV/heph/actions/workflows/maven.yml/badge.svg)](https://github.com/42BV/heph/actions/workflows/maven.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/f08863da179d410c977bbf3e143b0b1a)](https://app.codacy.com/gh/42BV/heph/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![codecov](https://codecov.io/gh/42BV/heph/graph/badge.svg?token=VMj23kHCls)](https://codecov.io/gh/42BV/heph)
[![Maven Central](https://img.shields.io/maven-central/v/nl.42/heph.svg?color=green)](https://central.sonatype.com/artifact/nl.42/heph)
[![Javadocs](https://www.javadoc.io/badge2/nl.42/heph/javadoc.svg)](https://www.javadoc.io/doc/nl.42/heph)
[![Apache 2](http://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Heph

For friends. Full name: Hephaestus, master blacksmith for the Greek Gods. Since Heph used to be a builder too, what better name for a library that is about Builders than Heph.

# Maven

In order to use Heph to power your testsuite, simply add the following Maven dependency:

```xml
<dependency>
    <groupId>nl.42</groupId>
    <artifactId>heph</artifactId>
    <version>3.0.0</version>
</dependency>
```

# Usage
Heph aims to make building entities for use within your unit tests easier.
It does so by allowing you to construct *fixtures*.
Fixtures are predefined objects, which can be re-used multiple times within a single test - without worrying about database constraint violations.

## Defining a BuildCommand
When you want to build (a fixture of) your entity, you'll likely want to adjust some of its fields.
For example, let's say we have a class Person with a first name and a surname. 
You'll likely want to adjust these for the various Person instances you are going to build.

Example entity:
```java
package nl._42.heph.example;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.data.domain.Persistable;

@Entity
public class Person implements Persistable<Long> {

    @Id
    private Long id;

    private String firstName;

    private String surname;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}
```

For the Person entity shown above, you can create a `BuildCommand` just by extending the `AbstractBuildCommand` interface:

```java
package nl._42.heph.example;

import nl._42.heph.AbstractBuildCommand;

public interface PersonBuildCommand extends AbstractBuildCommand<Person, PersonRepository> {

    @Override
    default Person findEntity(Person entity) {
        return getRepository().findByFirstName(entity.getFirstName());
    }

    PersonBuildCommand withFirstName(String firstName);

    PersonBuildCommand withSurname(String surname);
}
```

You'll see a few things here:
- The overridden `findEntity` method. This allows Heph to determine when to re-use your fixture.
In this example, if the `repository` already contains a Person with the same first name, no new person will be saved to the database.
  - It is required to override this method in your `BuildCommand` implementation
  - If you always want to create a new instance, return `null` here.
- Two methods for setting the first name and the surname of the Person
  - These methods don't have to be implemented - Heph will set the firstName and surname fields by itself!
  
## Defining a Builder (and fixtures)
Now that you've created the logic to adjust the values of your entity, it's time to create some fixtures!
To do so, create a class extending from `AbstractBuilder`. This allows you to use your `BuildCommand` to make some nice-looking fixtures.

```java
package nl._42.heph.example;

import nl._42.heph.AbstractBuilder;

public class PersonFixtures extends AbstractBuilder<Person, PersonBuildCommand> {

    @Override
    public PersonBuildCommand base() {
        return blank()
                .withFirstName("No first name")
                .withSurname("No surname");
    }

    public Person hephaestus() {
        return base()
                .withFirstName("Ήφαιστος")
                .withSurname(null)
                .create();
    }
}
```

You'll again see some interesting things in this example:
- The `base` method has been overridden. This method allows you to specify the base instance to start building fixtures from.
  - If you set a value in `base` but don't change it in your fixture, the base value will remain. It's a good practice to set commonly used values within this method.
- A fixture of Hephaestus has been created!
  - The first name has been set to the Greek spelling of Hephaestus.
  - As Greek Gods only have one name, the surname is set to `null`.

Some other methods are used here as well:
- `blank()`: Creates an empty instance of `Person`
- `create()`: Indicates that you've finished building this person, saves it to the database and returns it


## Advanced use cases
The example above illustrates basic usage of Heph. However, you may sometimes want to construct more advanced data structures.

There are a few annotations and techniques used to help you doing so. Some of these are (briefly) explained below.
For the full reference, click the Javadoc button and see the reference of `AbstractBuildCommand` and `AbstractBuilder`.

### Supplying nested entities (`@ManyToOne`, `@OneToMany`, etc.)
Let's say, you want to set another entity within your Person. Each person has an `Address` which is mapped via `@ManyToOne`.
To set an Address for a Person, you can specify either of the following methods in your `BuildCommand` (or both):

```java
PersonBuildCommand withAddress(Address address);
```

```java
PersonBuildCommand withAddress(Supplier<Address> addressReference);
```

The first example will directly set the field `address` in the `Person` to the provided Address instance.
However, when creating a fixture more than once, this has the downside that Java will instantiate the passed Address object multiple times.

To prevent this, construct your `BuildCommand` using the second example, in which the Address is `supplied`.
In that case, it will only be resolved if the Person is created for the first time.

### Supplying a nested entity (not mapped, only ID field available)
In some cases a nested entity might not be mapped, but only the database ID is stored in your main entity class.
Let's say, that our `Person` contains a field `addressId`, which stores the database ID of an `Address` entity.

In that case, use `@EntityId` and `@EntityField` annotations in your buildCommand to let Heph extract the ID of your entity (using the `Persistable` interface of Spring)
and have it map the Address to the field `addressId`.

```java
@EntityId
@EntityField("addressId")
PersonBuildCommand withAddress(Supplier<Address> addressReference);
```

### Determining when a nested entity field gets resolved
The last example is a bit more complicated.

Let's say we have a `Person`, which has an `Address` in a field called `address`.
To identify if an instance of this `Person` already exists, we look for the value of its `address` (`Address` has a proper `equals()` implementation).

This implies that the person's `Address` fixture is already created when calling the `findEntity` method of our `PersonBuildCommand`.
But: creating the `Address` fixture involves checking if the `Address` already exists in our database and then creating it.

As this costs quite some time and is not needed in most cases, Heph defers creating nested entities as long as possible.

For example, the `Address` passed in the `Supplier` in the `BuildCommand` is by default created when the `Person` fixture is saved to the database.
This is called a *before create resolve*.

If we want the address to be created before `findEntity` is called on the `PersonBuildCommand`, we need to do a *before find resolve*. 
This can be done by adding the `@Resolve(ResolveStrategy.BEFORE_FIND)` annotation to our `BuildCommand`. 

See the example below:

```java
package nl._42.heph.example;

import java.util.function.Supplier;

import nl._42.heph.AbstractBuildCommand;
import nl._42.heph.lazy.Resolve;
import nl._42.heph.lazy.ResolveStrategy;

public interface PersonBuildCommand extends AbstractBuildCommand<Person, PersonRepository> {

    @Override
    default Person findEntity(Person entity) {
        return getRepository().findByAddress(entity.getAddress());
    }

    PersonBuildCommand withFirstName(String firstName);

    PersonBuildCommand withSurname(String surname);
    
    @Resolve(ResolveStrategy.BEFORE_FIND)
    PersonBuildCommand withAddress(Supplier<Address> address);
}
```

