# TCP-File-Uploader
This application consists of two independent modules (subprojects): `client` and `server`. Each module contains an individual `build.gradle.kts` script and a `Dockerfile` for building the docker-image.

Using `docker-compose.yml` in the root of the project, you can configure the launch of multiple TCP servers (or clients) with different parameters.
Note that it's a bad solution to configure clients via a `docker-compose` file, because this is very inflexible and has unclear practical meaning. But for educational purposes and testing the performance of laboratory work, it is quite convenient and interesting. 

It is best if each client Docker-image is built separately, by working directly with the `Dockerfile` in the client module.

## Build
To build all the necessary JAR files, run the command 

_On Linux:_
```
./gradlew build
```
_On Windows:_
```
./gradlew.bat build
```
This will result in individual JARs for Docker in `/out/artifacts/` for each module. These JARs are necessary for successfully building Docker-images and then running them in containers.

Also, this command will result in JARs for user use in the root directory of the project: `/out/artifacts/`.

## Build Docker-images and run containers
_Docker must be installed and running on your system!_

To build Docker-images and run containers, you need to run the following command (while in the project root):

```
docker-compose build
```

You can specify all the necessary parameters for each image directly in the `docker-compose.yml` file, but you can also do this by adding options to the previous command:

```
docker-compose build --build-arg UPLOAD_PATH=your/path/to/uploads/dir --build-arg SERVER_PORT=your_port
```
This will allow you to pass custom parameters when building Docker-images of servers.

To run all containers you need to enter the command:
```angular2html
docker-compose up
```
## Tips
To avoid finding out the server IP address for subsequent connection to it through the client container, you can specify `container_name` as the host address in `docker-compose.yml`. Docker itself will substitute all the necessary data during the build.