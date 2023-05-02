FROM mcr.microsoft.com/devcontainers/java:0-17

ENV ANDROID_HOME=${HOME}/android_sdk
WORKDIR /tmp
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip &&\
    unzip commandlinetools-linux-9477386_latest.zip &&\
    mkdir -p ${ANDROID_HOME}/cmdline-tools/latest &&\
    mv cmdline-tools/* ${ANDROID_HOME}/cmdline-tools/latest &&\
    chmod -R ugo+w ${ANDROID_HOME} &&\
    rm -fr cmdline-tools commandlinetools-linux-9477386_latest.zip &&\
    yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin

