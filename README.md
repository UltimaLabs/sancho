# Sancho, a satellite tracking client

Sancho can track multiple satellites using the data provided by [SatTrackAPI service](https://github.com/UltimaLabs/sattrackapi). It will fetch next pass data and schedule shell command execution at satellite rise/set time and/or drive a rotator through `rotctld`, a Hamlib TCP rotator control daemon.
It is designed to run unattended as a system service. Sancho has been tested on Raspberry Pi 4 running Raspbian Buster and PC running CentOS 7. The only required input is a textual config file. Please see *Configuration* section for details.

## Built With

* [Spring Boot 2](https://spring.io/projects/spring-boot/)
* [Gradle Build Tool](https://gradle.org/)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

* [JDK 1.8 or higher](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

### Installing

Checkout the code.

```
$ git clone https://github.com/UltimaLabs/sancho.git
```

### Building and running the Spring Boot application

To run the application, execute the following Gradle command:

```
./gradlew bootRun
```

To build the executable jar you can execute the following command:

```
./gradlew bootJar
```

Gradle uses `jgitver`, a plugin which provides a standardized way, via a library, to calculate a project [semver](http://semver.org) compatible version from a git repository and its content. 

The executable jar is located in the build/libs directory and you can run it by executing the following command:

```
java -jar build/libs/sancho-0.1.1-1.jar
```

By default, the application log is output to `stdout`.

## Deploy using Ansible

You can deploy Sancho as a `systemd` service using Ansible. Edit the following files to match your needs:

  - `provisioning/hosts` - Ansible `hosts` file
  - `provisioning/vars/rpisdr.yml` or `provisioning/vars/rpisdr.yml` - sample Ansible variable files
  - `provisioning/roles/deploy-app/templates/application-*.yml.j2` - Sancho config file; refer to it from the `provisioning/vars/*.yml` file
  
After tweaking the configuration, run the shell script located in the `provisioning` folder: `rotrpi-deploy-app.sh` or `rpisdr-deploy-app.sh`. The script will build the executable jar and copy it together with the supporting files to the target host(s). Please note that the script will setup Sancho as a `systemd` service, but it won't manage the dependencies like JRE/JDK and *Hamlib*'s `rotctld`.

Deploy script keeps several old JAR versions on a host. You can control this number through `num_old_jars_to_keep` Ansible variable (defined in `provisioning/vars/*.yml` files). A symbolic link will point to the latest version after a successful deploy. In case of problems with the latest version, you can manually delete the symlink and create a new one, pointing to the last known stable version.   

## Configuration

Local development application is configured by editing the `src/main/resources/application.yml` file. Edit `provisioning/roles/deploy-app/templates/application-*.yml.j2` for Ansible deploy, which will copy the `application.yml` to the same folder containing an executable JAR. Configuration is read at the application startup, so for the changes to become active, you need to restart the application.

Comments in `src/main/resources/application.yml` explain most options. Some deserve detailed explanation:

  - `sancho.rotator.stepSize` - specifies how often the software should update rotator position, in seconds. 0.25 is a sensible default here. More often and you're just wasting CPU cycles. Up to one second is probably ok. More than a second might lead to longer rotator travel and possible signal loss.
  - `sancho.satelliteData[].minElevation` - a minimum elevation for tracking. Passes with elevation below this limit will not be scheduled for tracking. The tracking starts when satellite rises above `minElevation` and stops when it sets below this value.
  - `sancho.satelliteData[].trackingElevationThreshold` - maximum elevation of a pass needs to be above this threshold in order for the pass to be scheduled for tracking. For example, with `minElevation = 20` and `trackingElevationThreshold = 45`, the pass will be tracked when satellite rises above 20 degrees elevation up until it sets below 20 degrees, only if the maximum elevation is equal to or greater than 45 degrees.
  - `sancho.satelliteData[].stepSize` - step size for the data fetched from the SatTrackAPI service, in seconds. This value controls how many data points for a satellite pass the API returns. 0.5 is a sensible default for tracking. If this value is zero, tracking will be disabled, regardless of the `rotatorEnabled` setting. 
  - `sancho.satelliteData[].rotatorEnabled` - is rotator enabled? See also `stepSize`. If rotator is enabled and there's an error communicating with it through `rotctld`, tracking will not be scheduled. Instead, Sancho will wait `schedulerErrorWait` seconds before restarting data fetch/scheduling.

## Rotator notes

Sancho has been tested with Yaesu G-5500 rotator / EA4TX ARS-USB interface. 

### Parking

Immediately after Sancho finishes tracking a satellite, it will fetch next pass data and turn the rotator in the direction of the next pass rise. 
