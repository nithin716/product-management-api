services:

  # =========================================================
  # MySQL Database
  # =========================================================

  mysql:
    image: mysql:8.0
    container_name: product-api-mysql

    environment:
      MYSQL_DATABASE: product_db
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}

    ports:
      - "3307:3306"

    volumes:
      - mysql_data:/var/lib/mysql

    healthcheck:
      test:
        [
          "CMD",
          "mysqladmin",
          "ping",
          "-h",
          "localhost",
          "-uroot",
          "-p${MYSQL_ROOT_PASSWORD}"
        ]
      interval: 10s
      timeout: 5s
      retries: 10


  # =========================================================
  # Spring Boot Application
  # =========================================================

  product-api:
    build:
      context: .
      dockerfile: Dockerfile

    container_name: product-api

    ports:
      - "8080:8080"

    environment:
      DB_URL: jdbc:mysql://mysql:3306/product_db
      DB_USERNAME: root
      DB_PASSWORD: ${MYSQL_ROOT_PASSWORD}

      JWT_SECRET: ${JWT_SECRET}

      JWT_ACCESS_EXPIRATION: 900000
      JWT_REFRESH_EXPIRATION: 604800000

    depends_on:
      mysql:
        condition: service_healthy


# =========================================================
# Persistent MySQL Storage
# =========================================================

volumes:
  mysql_data: