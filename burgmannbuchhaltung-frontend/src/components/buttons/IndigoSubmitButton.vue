<template>
    <SubmitButton :icon="props.icon" :loading="props.loading" class="bg-indigo-600 text-white hover:bg-indigo-800 focus:ring-indigo-500 border-indigo-500" @click="$emit('update:loading', true)">
        <slot/>
    </SubmitButton>
</template>

<script setup>
import SubmitButton from "~/components/buttons/SubmitButton.vue";

const props = defineProps([
    "loading",
    "icon"
])
defineEmits(['update:loading'])

const icon = ref(props.icon)
</script>

<style scoped>

</style>
