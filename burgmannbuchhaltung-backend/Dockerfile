FROM docker.io/gradle:jdk17-alpine as buildstage

COPY ./ /work
WORKDIR /work
RUN gradle clean installDist

FROM docker.io/eclipse-temurin:20-jre-alpine
RUN apk update && apk add poppler-utils unpaper tesseract-ocr tesseract-ocr-data-deu

COPY --from=buildstage /work/build/install/ /
WORKDIR /burgmannbuchhaltung-backend

EXPOSE 8080
ENTRYPOINT /burgmannbuchhaltung-backend/bin/burgmannbuchhaltung-backend
