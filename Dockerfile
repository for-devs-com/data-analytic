# Primera etapa: Construcción del proyecto utilizando Maven y JDK 17
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Establece el directorio de trabajo, esto dentro del contenedor
WORKDIR /app

# Copia el archivo pom.xml del proyecto principal y los módulos
COPY pom.xml .
COPY modules/query-bridge/pom.xml modules/query-bridge/pom.xml
COPY modules/data-bridge/pom.xml modules/data-bridge/pom.xml
COPY modules/shared-library/pom.xml modules/shared-library/pom.xml

# Descarga las dependencias sin compilar el código aún
RUN mvn dependency:go-offline

# Copia  el código fuente del proyecto
COPY . .

# Construye  el proyecto (monorepo) con Maven
RUN mvn clean install

# Segunda etapa: Ejecutar el JAR generado en una imagen ligera
FROM eclipse-temurin:21-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR generado en la primera fase al directorio de trabajo
COPY --from=build /app/modules/query-bridge/target/query-bridge-1.0.0.jar /app/query-bridge.jar

# Expone el puerto en el que correrá la aplicación
EXPOSE 8081

# Comando para ejecutar la aplicación Spring Boot
CMD ["java", "-jar", "/app/query-bridge.jar"]
