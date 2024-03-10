<template>
    <input
        :value="modelValue"
        @input="$emit('update:modelValue', $event.target.value)"
        :disabled="!editable"
        class="w-full p-0"
        :class="{
            'border-white': !editable,
            '-mx-1 px-1 -my-0.5 py-0.5': editable
        }"
    />
</template>

<script setup lang="ts">
const props = defineProps({
    modelValue: String,
    editable: Boolean,
});
const emit = defineEmits(["update:modelValue"]);
</script>

<style scoped></style>
