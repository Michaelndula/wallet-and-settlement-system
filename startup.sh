#!/bin/bash

# ==============================================================================
# Wallet & Settlement System - Automated Startup Script
# ==============================================================================
# This script automates the initial setup process for the project.
# It performs the following steps:
# 1. Checks for required dependencies (Docker).
# 2. Builds the Spring Boot application using Maven.
# 3. Builds and starts all services using Docker Compose.
# ==============================================================================

# --- Color Codes for Output ---
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# --- Step 1: Prerequisite Checks ---
echo -e "${BLUE}Step 1: Checking for required dependencies...${NC}"

if ! command -v docker &> /dev/null
then
    echo -e "${RED}Error: Docker could not be found.${NC}"
    echo -e "${YELLOW}Please install Docker before running this script.${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null
then
    echo -e "${RED}Error: docker-compose could not be found.${NC}"
    echo -e "${YELLOW}Please ensure Docker Compose is installed and available in your PATH.${NC}"
    exit 1
fi

echo -e "${GREEN} Docker and Docker Compose are installed.${NC}"
echo ""


# --- Step 2: Build the Backend Application ---
# Although the multi-stage Dockerfile can build the app, running it here first
# provides clearer, faster feedback if there's a Java compilation error.
echo -e "${BLUE}Step 2: Building the Java backend with Maven...${NC}"
echo -e "${YELLOW}(This may take a few minutes on the first run as Maven downloads dependencies)${NC}"

# We use ./mvnw to ensure we're using the project's specified Maven version
if [ -f "./mvnw" ]; then
    ./mvnw clean package -DskipTests
else
    # Fallback to system maven if wrapper is not present
    mvn clean package -DskipTests
fi


# Check if the Maven build was successful
if [ $? -ne 0 ]; then
    echo -e "${RED}----------------------------------------${NC}"
    echo -e "${RED}Maven build failed! Please check the errors above.${NC}"
    echo -e "${RED}Aborting startup process.${NC}"
    echo -e "${RED}----------------------------------------${NC}"
    exit 1
fi

echo -e "${GREEN}✔ Backend application built successfully! The .jar file is ready.${NC}"
echo ""


# --- Step 3: Build and Start Docker Containers ---
echo -e "${BLUE}Step 3: Building Docker images and starting containers...${NC}"
echo -e "${YELLOW}(This can also take a few minutes, especially the first time)${NC}"

docker-compose up -d --build

# Check if docker-compose was successful
if [ $? -ne 0 ]; then
    echo -e "${RED}----------------------------------------${NC}"
    echo -e "${RED}Docker Compose failed to start! Please check the errors above.${NC}"
    echo -e "${YELLOW}You can try running 'docker-compose up --build' manually for more detailed logs.${NC}"
    echo -e "${RED}----------------------------------------${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN} Setup Complete! All services are running.${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "You can now access the services at:"
echo -e "-  Frontend UI:       ${YELLOW}http://localhost:80${NC}"
echo -e "- Backend API:       ${YELLOW}http://localhost:8080${NC}"
echo -e "- RabbitMQ Admin:    ${YELLOW}http://localhost:15672${NC} (user: guest, pass: guest)"
echo ""
echo -e "To view live logs for all services, run: ${YELLOW}docker-compose logs -f${NC}"
echo -e "To stop all services, run: ${YELLOW}docker-compose down${NC}"
echo ""
