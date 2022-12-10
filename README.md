### Starting a mongo

`docker run -d -p 27099:27017 --name local-mongo mongo:5.0.13`

### Starting the ELK stack

`cd elk && docker-compose up` (this doesn't work reliably for me 😕)

### Starting the service

`./gradlew bootRun`