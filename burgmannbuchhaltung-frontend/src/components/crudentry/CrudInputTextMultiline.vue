<template>
    <div class="w-full p-0 h-20">
        <textarea
            :value="modelValue"
            @input="emit('update:modelValue', $event.target.value)"
            :disabled="!editable"
            class="w-full p-0 h-20"
            :class="{
                'border-white': !editable,
                '-mx-1 px-1 -my-0.5 py-0.5': editable,
            }"
        />
    </div>
</template>

<script setup lang="ts">
const props = defineProps({
    modelValue: String,
    editable: Boolean,
});
const emit = defineEmits(["update:modelValue"]);

function updateValue(value: string) {
    console.log('textarea, ', value)
    emit('update:modelValue', value)
}
</script>

<style scoped></style>
