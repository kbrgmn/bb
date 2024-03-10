<script lang="ts" setup>
import { InitApp } from "~/utils/initApp";

InitApp();
const locale = useState<string>("locale.i18n");


import '@unocss/reset/tailwind-compat.css'
import 'uno.css'
</script>

<template>
    <Html :lang="locale" class="">
        <!-- dark:bg-gray-900 dark:text-gray-200 -->
        <Body class="bg-gray-50 text-gray-800 antialiased transition-colors duration-300">
            <NuxtLayout class="">
                <NuxtLoadingIndicator />
<!--                <NuxtErrorBoundary>-->
                    <NuxtPage class="h-full" />

<!--                    <template #error="{ error, clearError }">-->
<!--                        <p>Es ist ein Fehler aufgetreten: {{ error }}</p>-->

<!--                        <button @click="clearError" class="rounded-md bg-white border p-1 mt-2">Fehler zurücksetzen</button>-->
<!--                    </template>-->
<!--                </NuxtErrorBoundary>-->
            </NuxtLayout>
        </Body>
    </Html>
</template>

<style lang="postcss">
/*body {
    @apply bg-gray-50 dark:bg-gray-800;
}

.global-text {
    @apply text-gray-900 dark:text-gray-50;
}*/
</style>
