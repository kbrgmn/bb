/** @type {import('tailwindcss/types').Config} */

const defaultTheme = require("tailwindcss/defaultTheme");

module.exports = {
    darkMode: "class",
    content: ["./components/**/*.{js,vue,ts}", "./layouts/**/*.vue", "./pages/**/*.vue", "./plugins/**/*.{js,ts}", "./app.vue", "./assets/**/*.scss", "./assets/**/*.css"],
    theme: {
        extend: {
            fontFamily: {
                sans: ['"Inter var"', ...defaultTheme.fontFamily.sans],
            },
            fontSize: {
                xxs: "0.8rem",
                xs: "0.825rem",
                ssm: "0.9rem",
                sm: "1rem",
                base: "1.1rem",
                lg: "1.25rem",
                xl: "1.375rem",
                "2xl": "1.65rem",
                "3xl": "2.0rem",
                "4xl": "2.5rem",
                "5xl": "3.3rem",
                "6xl": "4.1rem",
                "7xl": "5rem",
                "8xl": "6.5rem",
                "9xl": "8.5rem",
            },
            // maxWidth: {
            //     'max-w-0': 'max-width: 0rem', /* 0px */
            //     'none': 'max-width: none',
            //     'xs': 'max-width: 20rem', /* 320px */
            //     'sm': 'max-width: 24rem', /* 384px */
            //     'md': 'max-width: 28rem', /* 448px */
            //     'lg': 'max-width: 32rem', /* 512px */
            //     'xl': 'max-width: 36rem', /* 576px */
            //     '2xl': 'max-width: 42rem', /* 672px */
            //     '3xl': 'max-width: 48rem', /* 768px */
            //     '4xl': 'max-width: 56rem', /* 896px */
            //     '5xl': 'max-width: 64rem', /* 1024px */
            //     '6xl': 'max-width: 72rem', /* 1152px */
            //     '7xl': 'max-width: 80rem', /* 1280px */
            //     '8xl': 'max-width: 88rem',
            //     'full': 'max-width: 100%',
            //     'min': 'max-width: min-content',
            //     'max': 'max-width: max-content',
            //     'fit': 'max-width: fit-content',
            //     'prose': 'max-width: 65ch',
            //     'screen-sm': 'max-width: 640px',
            //     'screen-md': 'max-width: 768px',
            //     'screen-lg': 'max-width: 1024px',
            //     'screen-xl': 'max-width: 1280px',
            //     'screen-2xl': 'max-width: 1536px',
            // }
        },
    },
    variants: {
        extend: {},
    },
    plugins: [require("@tailwindcss/forms"), require("@tailwindcss/typography"), require("@tailwindcss/aspect-ratio")],
};
