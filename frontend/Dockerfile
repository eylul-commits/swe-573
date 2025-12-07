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

# Build arguments for environment variables
ARG VITE_API_BASE_URL
ARG VITE_STREAM_CHAT_API_KEY
ARG VITE_CLOUDINARY_CLOUD_NAME
ARG VITE_CLOUDINARY_UPLOAD_PRESET

# Set environment variables for build
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_STREAM_CHAT_API_KEY=$VITE_STREAM_CHAT_API_KEY
ENV VITE_CLOUDINARY_CLOUD_NAME=$VITE_CLOUDINARY_CLOUD_NAME
ENV VITE_CLOUDINARY_UPLOAD_PRESET=$VITE_CLOUDINARY_UPLOAD_PRESET

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

