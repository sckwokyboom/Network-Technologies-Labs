FROM openjdk:19
WORKDIR /app-server
ARG UPLOAD_DIR_PATH
ARG SERVER_PORT
ENV UPLOAD_DIR_PATH=$UPLOAD_DIR_PATH
ENV SERVER_PORT=$SERVER_PORT
COPY "/out/artifacts/ServerFileUploaderDocker.jar" "app-server.jar"

CMD java -jar app-server.jar -p $SERVER_PORT -ud $UPLOAD_DIR_PATH

