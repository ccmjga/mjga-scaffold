FROM node:24.16.0-alpine3.23@sha256:2bdb65ed1dab192432bc31c95f94155ca5ad7fc1392fb7eb7526ab682fa5bf14 AS builder
WORKDIR /workspace
COPY package.json package-lock.json ./
COPY apps/web/package.json apps/web/package.json
COPY packages/api-client/package.json packages/api-client/package.json
RUN --mount=type=cache,target=/root/.npm npm ci
COPY contracts contracts
COPY packages/api-client packages/api-client
COPY apps/web apps/web
RUN npm run generate:client && npm run build --workspace @mjga/api-client && npm run build --workspace @mjga/web

FROM caddy:2.10.2-alpine@sha256:4c6e91c6ed0e2fa03efd5b44747b625fec79bc9cd06ac5235a779726618e530d
COPY --from=builder --chown=10001:10001 /workspace/apps/web/dist /srv
COPY --chown=10001:10001 apps/web/Caddyfile /etc/caddy/Caddyfile
COPY --chown=10001:10001 apps/web/docker-entrypoint.sh /usr/local/bin/mjga-web
RUN chmod 0755 /usr/local/bin/mjga-web
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --retries=10 CMD wget -q -O - http://127.0.0.1:8080/healthz || exit 1
ENTRYPOINT ["/usr/local/bin/mjga-web"]
