<template>
    <div class="">
        <!-- colspan -->
        <label class="block text-sm font-semibold leading-6 text-gray-900" :for="labeledId">{{ title }}<span v-if="required" class="text-red-600">*</span> </label>
        <div class="flex gap-3 relative mt-0.5 rounded-md">
            <div v-if="inlineIcon !== ''" class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                <Icon class="h-5 w-5 text-gray-400" aria-hidden="true" :name="inlineIcon" />
            </div>
            <slot />
        </div>
        <p v-if="description !== ''" class="mt-2 text-sm leading-6 text-gray-600">{{ description }}</p>
    </div>
</template>

<script setup lang="ts">
const props = defineProps({
    title: {
        type: String,
        required: true,
    },
    description: {
        type: String,
        required: false,
        default: "",
    },
    labeledId: {
        type: String,
        required: true,
    },
    inlineIcon: {
        type: String,
        required: false,
        default: "",
    },
    required: {
        type: Boolean,
        required: false,
        default: false,
    },
});
</script>

<style scoped></style>
