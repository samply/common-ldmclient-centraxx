UNMERGED BRANCH -CHANGES FROM OTHER SAMPLY TEAMS MISSING

# Samply Common LDM Client Centraxx

Samply Common LDM Client Centraxx is a library for the communication with **L**ocal **D**ata**m**anagement systems.
It extends the abstract Samply Common LDM Client.

## Features

Provides basic operations to communicate with Centraxx.

## Build

In order to build this project, you need to configure maven properly and use the maven profile that
fits to your project.

``` 
mvn clean package
```

## Configuration

Samply Common LDM Client Centraxx is configured during runtime by providing the necessary paramaters in the constructor.

For usage examples, see the test classes.

## Maven artifact

To use the module, include the following artifact in your project.

``` 
<dependency>
    <groupId>de.samply</groupId>
    <artifactId>common-ldmclient.centraxx</artifactId>
    <version>3.0.1</version>
</dependency>
``` 

