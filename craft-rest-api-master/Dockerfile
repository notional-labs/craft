# Get Node image
FROM node:16

# Setup env
ENV NODE_ENV=production
WORKDIR /

# Setup working env
COPY package*.json ./
COPY ./src ./src
COPY ./tsconfig.json ./

# Node dependencies
RUN npm install -g typescript
RUN npm install --production

RUN tsc

# Start service
ENTRYPOINT ["node", "dist/index.js"]
