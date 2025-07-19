# Stage 1: Angular build
FROM node:22 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# Dodaj proveru strukture pre build-a
RUN ls -la
RUN npm run build -- --configuration=production --base-href=/
# Proveri da li je build uspeo
RUN ls -la /app/dist/ecommerce-frontend

# Stage 2: Nginx serve
FROM nginx:alpine
# Obriši default nginx sadržaj
RUN rm -rf /usr/share/nginx/html/*
# Kopiraj Angular build
COPY --from=build /app/dist/ecommerce-frontend /usr/share/nginx/html
COPY frontend.nginx.conf /etc/nginx/conf.d/default.conf
# Proveri kopirane fajlove
RUN ls -la /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]