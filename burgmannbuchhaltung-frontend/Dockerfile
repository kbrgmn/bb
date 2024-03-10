# BUILD
FROM docker.io/gplane/pnpm:8 as buildstage

COPY . /build

WORKDIR /build
RUN echo pnpm version: && pnpm --version && pnpm install

RUN pnpm build

# RUN
FROM docker.io/node:20
COPY --from=buildstage /build/.output/ /app

WORKDIR /app

EXPOSE 3000
ENTRYPOINT node server/index.mjs
