# Build stage
FROM maven:3.8.5-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage: Tomcat 10.1 (hỗ trợ Jakarta EE 10)
FROM tomcat:10.1-jdk11-corretto
RUN rm -rf /usr/local/tomcat/webapps/*

# Tắt cổng shutdown 8005 để Render không quét nhầm cổng kiểm tra sức khỏe
RUN sed -i 's/port="8005"/port="-1"/' /usr/local/tomcat/conf/server.xml

COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

ENV PORT=8080
EXPOSE 8080
CMD ["catalina.sh", "run"]
