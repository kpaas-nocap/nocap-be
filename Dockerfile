FROM eclipse-temurin:17 AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .   
COPY build.gradle .      
RUN chmod +x gradlew
COPY src src
RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy

# 크롬 의존성 설치
RUN apt-get update && apt-get install -y \
    gnupg curl ca-certificates apt-transport-https \
    fonts-liberation libnss3 libasound2 libx11-6 libxss1 libxrandr2 libxdamage1 libxcb1 libxcomposite1 libxfixes3 \
    libgbm1 libgtk-3-0 xvfb unzip && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google.gpg && \
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google.gpg] http://dl.google.com/linux/chrome/deb/ stable main" \
    > /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && apt-get install -y google-chrome-stable && \
    rm -rf /var/lib/apt/lists/*

ENV CHROME_BIN=/usr/bin/google-chrome

WORKDIR /app
COPY --from=build /app/build/libs/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
