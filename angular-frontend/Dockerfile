# Stage 1: Angular build
FROM node:22 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration=production

# Stage 2: Nginx serve
FROM nginx:alpine
COPY --from=build /app/dist/ecommerce-frontend /usr/share/nginx/html
COPY frontend.nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 4200
CMD ["nginx", "-g", "daemon off;"]
