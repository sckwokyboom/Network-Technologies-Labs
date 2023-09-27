FROM openjdk:19

WORKDIR /app-client
ARG SERVER_HOST
ARG SERVER_PORT
ARG FILE_TO_UPLOAD
ENV SERVER_HOST=$SERVER_HOST
ENV SERVER_PORT=$SERVER_PORT
ENV FILE_TO_UPLOAD=$FILE_TO_UPLOAD
COPY "/out/artifacts/ClientFileUploaderDocker.jar" "app-client.jar"
COPY "/out/artifacts/ClientFileUploaderDocker.jar" "file_to_upload"
CMD java -jar app-client.jar -s $FILE_TO_UPLOAD -p $SERVER_PORT -h $SERVER_HOST
