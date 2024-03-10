export default defineNuxtConfig({
    srcDir: "src",
    modules: [
        "@vueuse/nuxt",
        // "@nuxtjs/tailwindcss",
        // pinia plugin - https://pinia.esm.dev
        "@pinia/nuxt",
        ['@unocss/nuxt', { autoImport: false }],
        "@nuxtjs/i18n",
        "@nuxtjs/color-mode",
        // https://github.com/huntersofbook/huntersofbook/tree/main/packages/naive-ui-nuxt
        "@bg-dev/nuxt-naiveui",
        "@nuxt/content",
        "@sidebase/nuxt-auth",
        "nuxt-icon",
        '@nuxt/image',
        '@formkit/auto-animate/nuxt'
    ],
    build: {
        transpile: ["@headlessui/vue"],
    },

    auth: {
        baseURL: "/r/auth",

        provider: {
            type: "local",

            pages: {
                login: "/login",
            },
        },

        globalAppMiddleware: {
            isEnabled: true,
        },
    },

    /*    unocss: {
        uno: false,
        preflight: false,
        icons: true,
        presets: [
// @ts-ignore
            presetIcons({
                scale: 1.2,
                extraProperties: {
                    display: 'inline-block',
                },
            }),
        ]
    },*/

    typescript: {
        tsConfig: {
            compilerOptions: {
                strict: true,
                types: ["@pinia/nuxt", "./type.d.ts"],
            },
        },
    },
    colorMode: {
        classSuffix: "",
        fallback: "light",
        storageKey: "color-mode",
    },

    tailwindcss: {
        configPath: "./tailwind.config.ts",
    },

    vite: {
        logLevel: "info",
    },
    css: ["@/assets/css/global.css"],

    nitro: {
        compressPublicAssets: true,
        devProxy: {
            "/r/": "http://localhost:8080/r",
        },
        /*
        routeRules: {
            "/r/**": { proxy: "http://localhost:8080/r/**" }
        }*/
    },

    ssr: false,

    devtools: {
        enabled: true,
    },
});
