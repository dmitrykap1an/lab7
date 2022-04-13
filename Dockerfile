FROM alpine:latest

RUN apk add --no-cache \
  gradle \
  openjdk17 \
  ;

WORKDIR /app
COPY ./ ./
ENTRYPOINT ["sh"]
