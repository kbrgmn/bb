<template>
    <div
        :class="{
            'sm:grid sm:grid-cols-3 sm:gap-4 sm:px-0': singleInLine,
            'flex items-center flex-col sm:flex-row gap-2': !singleInLine,
            'px-4 py-3': setPadding,
        }"
    >
        <dt class="text-sm font-semibold leading-6 text-gray-900">{{ title }}</dt>
        <dd class="text-sm leading-6 text-gray-700 sm:col-span-2 w-full" :class="[singleInLine ? 'mt-1 sm:mt-0' : '']">
            <slot />
        </dd>
    </div>
</template>

<script setup lang="ts">
const props = defineProps({
    title: {
        type: String,
        required: true,
    },
    //editable: Boolean,
    singleInLine: {
        type: Boolean,
        required: false,
        default: true,
    },
    setPadding: {
        type: Boolean,
        required: false,
        default: true,
    },
});
</script>

<style scoped></style>
