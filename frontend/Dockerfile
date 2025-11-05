# Build stage
FROM node:20-alpine as build-stage

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy project files
COPY . .

# Build the Vue app
RUN npm run build

# Production stage
FROM node:20-alpine as production-stage

# Set working directory
WORKDIR /app

# Install serve globally
RUN npm install -g serve

# Copy built assets from build stage
COPY --from=build-stage /app/dist ./dist

# Expose port 80
EXPOSE 80

# Start serve
CMD ["serve", "-s", "dist", "-l", "80"]

