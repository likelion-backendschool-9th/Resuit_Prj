FROM node:22-alpine AS deps

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

EXPOSE 5173

CMD ["npm", "run", "dev"]

# 운영: 빌드 결과물을 next start 로 실행한다.
FROM deps AS production

ARG BACKEND_URL=http://backend:8080
ENV NODE_ENV=production
ENV BACKEND_URL=$BACKEND_URL

RUN npm run build

CMD ["npm", "start"]
