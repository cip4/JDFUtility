# compile and test war
FROM maven:3-openjdk-8 as war-builder

ARG BUILD_NUMBER=n.a.

COPY [".git", "/work/.git"]
COPY ["src", "/work/src"]
COPY ["pom-war.xml", "/work/"]

WORKDIR /work

RUN mvn -f pom-war.xml clean install -Dmaven.test.skip=true -Dbuild=$BUILD_NUMBER

# create final image
FROM tomcat:8-jdk8-slim

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=war-builder ["/work/target/JDFToolbox.war", "/usr/local/tomcat/webapps/ROOT.war"]