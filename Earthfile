git-meta:
    FROM alpine/git:latest
    WORKDIR /meow
    COPY .git .git

    RUN mkdir /meta && \
        git rev-parse -q --verify HEAD > /meta/REVISION && \
        git log --format='%aN <%aE>' \
          | sort -u \
          | tr '\n' ',' \
          | sed 's/,$//' \
          | sed 's/,/, /' > /meta/AUTHORS
    SAVE ARTIFACT /meta /meta

#
# Compile the project through maven and create a jar
#
compile-jar:
    # FROM maven:3.8.1-jdk-11
    FROM earthly/dind:alpine

    RUN apk \
        --no-cache add openjdk11 \
        --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community

    # Install Maven
    ARG MAVEN_VERSION="3.8.1"
    ARG MAVEN_HOME="/usr/lib/mvn"

    ENV MAVEN_HOME ${MAVEN_HOME}
    ENV PATH $MAVEN_HOME/bin:$PATH
    RUN wget http://archive.apache.org/dist/maven/maven-3/3.8.1/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
      tar -zxf apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
      rm apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
      mv apache-maven-${MAVEN_VERSION} /usr/lib/mvn

    # Cache pom.xml and all required dependencies
    COPY ./pom.xml pom.xml
    RUN mvn -B -ntp dependency:go-offline

    # Copy source files over
    COPY ./src ./src
    # TODO: When push is called, this is running twice
    WITH DOCKER \
        --pull quay.io/keycloak/keycloak:17.0.0-legacy \
        --pull quay.io/minio/minio:latest
        # ISSUE: Unfortunately -o is currently NOT possible, as we use surefire-plugin and that download dynamically!
        RUN mvn -B package
    END
    SAVE ARTIFACT target/*.jar /app.jar

    # Run meta directory
    RUN mkdir /meta && \
        mvn help:evaluate -Dexpression=project.name -q -DforceStdout > /meta/NAME && \
        mvn help:evaluate -Dexpression=project.description -q -DforceStdout > /meta/DESCRIPTION && \
        mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout > /meta/GROUP_ID && \
        mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout > /meta/ARTIFACT_ID && \
        mvn help:evaluate -Dexpression=project.version -q -DforceStdout > /meta/VERSION && \
        mvn help:evaluate -Dexpression=project.licenses[0].name -q -DforceStdout > /meta/LICENSE && \
        mvn help:evaluate -Dexpression=project.scm.url -q -DforceStdout > /meta/URL_REPO && \
        mvn help:evaluate -Dexpression=project.url -q -DforceStdout > /meta/URL_DOCS && \
        mvn help:evaluate -Dexpression=project.organization.name -q -DforceStdout > /meta/VENDOR && \
        mvn help:evaluate -Dexpression=project.organization.url -q -DforceStdout > /meta/URL_WEBSITE

    SAVE ARTIFACT meta /compile/meta

build-image-java:
    FROM openjdk:11

    ARG APP_IMG_GROUP_ID="tmp"
    ARG APP_IMG_ARTIFACT_ID="tmp"
    ARG APP_IMG_TITLE="unspecified"
    ARG APP_IMG_DESCRIPTION="unspecified"
    ARG APP_IMG_VERSION="0.0.0-unspecified"
    ARG APP_IMG_LICENSE="proprietary"
    ARG APP_IMG_AUTHORS="unspecified"
    ARG APP_IMG_VENDOR="unspecified"

    ARG APP_IMG_REV="unspecified"
    ARG APP_IMG_CREATED="9999-12-31T23:59:59Z"

    ARG APP_IMG_URL="https://example.com"
    ARG APP_IMG_VCS="https://example.com"
    ARG APP_IMG_DOCS="https://example.com"

    COPY +compile-jar/app.jar /storage/app.jar
    ENTRYPOINT ["java", "-jar", "/storage/app.jar"]
    CMD []

    # Meta Data
    LABEL org.opencontainers.image.title="${APP_IMG_TITLE}"
    LABEL org.opencontainers.image.description="${APP_IMG_DESCRIPTION}"
    LABEL org.opencontainers.image.vendor="${APP_IMG_VENDOR}"
    LABEL org.opencontainers.image.authors="${APP_IMG_AUTHORS}"

    LABEL org.opencontainers.image.version="${APP_IMG_VERSION}"
    LABEL org.opencontainers.image.licenses="${APP_IMG_LICENSE}"

    LABEL org.opencontainers.image.url="${APP_IMG_URL}"
    LABEL org.opencontainers.image.documentation="${APP_IMG_DOCS}"
    LABEL org.opencontainers.image.source="${APP_IMG_VCS}"
    LABEL org.opencontainers.image.created="${APP_IMG_CREATED}"
    LABEL org.opencontainers.image.revision="${APP_IMG_REV}"

   SAVE IMAGE --push ghcr.io/bjoernakamanf/${APP_IMG_GROUP_ID}/${APP_IMG_ARTIFACT_ID}:${APP_IMG_VERSION}

main:
    FROM busybox
    COPY +compile-jar/compile/meta /meta-jar
    COPY +git-meta/meta /meta-git
    BUILD \
        --build-arg APP_IMG_TITLE="$(cat /meta-jar/NAME)" \
        --build-arg APP_IMG_DESCRIPTION="$(cat /meta-jar/DESCRIPTION)" \
        --build-arg APP_IMG_VENDOR="$(cat /meta-jar/VENDOR)" \
        --build-arg APP_IMG_AUTHORS="$(cat /meta-git/AUTHORS)" \
        --build-arg APP_IMG_GROUP_ID="$(cat /meta-jar/GROUP_ID)" \
        --build-arg APP_IMG_ARTIFACT_ID="$(cat /meta-jar/ARTIFACT_ID)" \
        --build-arg APP_IMG_VERSION="$(cat /meta-jar/VERSION)" \
        --build-arg APP_IMG_LICENSE="$(cat /meta-jar/LICENSE)" \
        --build-arg APP_IMG_URL="$(cat /meta-jar/URL_WEBSITE)" \
        --build-arg APP_IMG_DOCS="$(cat /meta-jar/URL_DOCS)" \
        --build-arg APP_IMG_VCS="$(cat /meta-jar/URL_REPO)" \
        --build-arg APP_IMG_CREATED="$(date -u +"%Y-%m-%dT%H:%M:%SZ")" \
        --build-arg APP_IMG_REV="$(cat /meta-git/REVISION)" \
        +build-image-java