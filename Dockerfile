ARG VERSION=dev
ARG BUILD_NUMBER=n.a.
ARG GIT_REV=n.a.

# compile and test bambi
FROM amazoncorretto:17-alpine-jdk AS java-builder

ARG VERSION
ARG BUILD_NUMBER
ARG GIT_REV

COPY [".git", "/work/.git"]
COPY ["src", "/work/src"]
COPY ["gradle", "/work/gradle"]
COPY ["build.gradle", "settings.gradle", "gradle.properties", "gradlew", "/work/"]

RUN apk add --no-cache dos2unix

WORKDIR /work

RUN dos2unix gradlew
RUN ./gradlew -i build fatJar -PprojectVersion=${VERSION} -PbuildNumber=${BUILD_NUMBER} --no-daemon

# create final image
FROM amazoncorretto:17-alpine-jdk

ARG VERSION
ARG BUILD_NUMBER
ARG GIT_REV

ENV VERSION=${VERSION}
ENV BUILD_NUMBER=${BUILD_NUMBER}
ENV GIT_REV=${GIT_REV}

RUN /usr/sbin/addgroup -S cip4 && /usr/sbin/adduser -S cip4 -G cip4

COPY --chown=cip4:cip4 --from=java-builder ["/work/build/libs/*-fat-${VERSION}.jar", "/app/jdfutility.jar"]

USER cip4
WORKDIR /home/cip4

EXPOSE 8080

ENTRYPOINT ["java", "-cp","/app/jdfutility.jar", "org.cip4.jdfutility.exe.CheckJDFServer"]