<template>
    <CrudEntry :title="title" :single-in-line="singleInLine" :set-padding="setPadding">
        <CrudInputTextMultiline :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" :editable="editable"></CrudInputTextMultiline>
    </CrudEntry>
</template>

<script setup lang="ts">
import CrudEntry from "~/components/crudentry/CrudEntry.vue";
import CrudInputTextMultiline from "~/components/crudentry/CrudInputTextMultiline.vue";

const props = defineProps({
    title: {
        type: String,
        required: true,
    },
    modelValue: String,
    editable: Boolean,
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
const emit = defineEmits(["update:modelValue"]);
</script>
