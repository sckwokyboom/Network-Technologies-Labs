FROM openjdk:19
WORKDIR /app-server
ARG UPLOAD_PATH
ARG SERVER_PORT
ENV UPLOAD_PATH=$UPLOAD_PATH
ENV SERVER_PORT=$SERVER_PORT
COPY "/out/artifacts/ServerFileUploaderDocker.jar" "app-server.jar"

CMD java -jar app-server.jar -p $SERVER_PORT -d $UPLOAD_PATH

