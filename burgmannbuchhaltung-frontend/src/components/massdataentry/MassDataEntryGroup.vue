<template>
    <div class="grid grid-cols-1 gap-x-8 gap-y-8 pt-10 md:grid-cols-4">
        <div class="px-4 sm:px-0">
            <h2 class="text-base font-semibold leading-7 text-gray-900">{{ title }}</h2>
            <p class="mt-1 text-sm leading-6 text-gray-600">{{ description }}</p>
        </div>

        <form class="bg-white shadow-sm ring-1 ring-gray-900/5 sm:rounded-xl md:col-span-3">
            <div class="px-4 py-6 sm:p-8">
                <div class="grid max-w-2xl grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
                    <slot />
                </div>
            </div>
            <div class="flex items-center justify-end gap-x-6 border-t border-gray-900/10 px-4 py-4 sm:px-8">
                <button class="flex items-center gap-1 rounded-md px-3 py-2 text-sm font-semibold leading-6 text-gray-900 hover:bg-red-600 hover:text-white" type="button">
                    <Icon name="fluent:arrow-reset-20-filled" class="h-5 w-5" />
                    Zurücksetzen
                </button>
                <button
                    class="flex items-center gap-1 rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                    type="submit"
                >
                    <Icon name="material-symbols:save-outline" class="h-5 w-5" />
                    Speichern
                </button>
            </div>
        </form>
    </div>
</template>

<script setup lang="ts">
const props = defineProps({
    title: String,
    description: String,
});
</script>

<style scoped></style>
