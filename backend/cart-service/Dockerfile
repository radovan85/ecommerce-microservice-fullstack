FROM tomcat:10.1-jdk17

WORKDIR /app

COPY target/cart-service.war /app/app.war

RUN mv /app/app.war /usr/local/tomcat/webapps/ROOT.war

# Usklađene ENV varijable
ENV DB_URL=jdbc:postgresql://postgres:5432/ecommerce-db
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=1111

CMD ["catalina.sh", "run"]