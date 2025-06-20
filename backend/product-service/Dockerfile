# Koristimo JDK 17 baziran na Alpine
FROM openjdk:17-alpine

# Instaliramo potrebne pakete (bash, unzip)
RUN apk add --no-cache bash unzip

# Postavljamo radni direktorijum
WORKDIR /app

# Kopiramo ZIP paket
COPY target/universal/product-service-1.0-SNAPSHOT.zip /app/product-service.zip

# Raspačavamo Play aplikaciju
RUN unzip /app/product-service.zip && rm /app/product-service.zip

# Postavljamo ENV varijable za bazu
ENV DB_URL=jdbc:postgresql://postgres:5432/ecommerce-db
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=1111

# Premeštamo folder sa tačnim imenom
RUN mv /app/product-service-1.0-SNAPSHOT /app/product-service

# Dajemo dozvolu za pokretanje aplikacije
RUN chmod +x /app/product-service/bin/product-service

# Postavljamo Bash kao podrazumevani shell i pokrećemo Play aplikaciju
CMD ["/bin/bash", "-c", "/app/product-service/bin/product-service"]