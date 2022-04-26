FROM alpine:latest

RUN apk add --no-cache \
  gradle \
  openjdk17 \
  ;

WORKDIR /app
COPY ./ ./

RUN gradle shadowJar
RUN mkdir -p /home/newton/IdeaProjects/lab6-master/server/src/main/kotlin/server/
RUN echo 'user=s334585' > /home/newton/IdeaProjects/lab6-master/server/src/main/kotlin/server/databaseInfo.properties
RUN echo 'password=gey514' >> /home/newton/IdeaProjects/lab6-master/server/src/main/kotlin/server/databaseInfo.properties
RUN cat /home/newton/IdeaProjects/lab6-master/server/src/main/kotlin/server/databaseInfo.properties


ENTRYPOINT ["sh"]
