# Use Node.js as base image
FROM node:20

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

RUN npm install -g @angular/cli@19

# Install dependencies
RUN npm install

# Copy project files
COPY . .

# Expose port for Angular
EXPOSE 4200

# Start Angular
CMD ["ng" , "serve", "--host", "0.0.0.0"]
