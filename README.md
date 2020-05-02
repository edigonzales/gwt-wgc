# gwt-wgc

## Development

First Terminal:
```
mvn clean spring-boot:run
```

Second Terminal:
```
mvn gwt:generate-module gwt:codeserver
```

Or simple devmode (which worked better for java.xml.bind on client side):
```
mvn gwt:generate-module gwt:devmode 
``` 

Build fat jar and docker image:
```
BUILD_NUMBER=9999 mvn package
```

## Run
```
docker run -p 8080:8080 sogis/wgc
```
