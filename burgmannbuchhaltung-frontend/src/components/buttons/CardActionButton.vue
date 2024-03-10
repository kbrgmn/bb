<template>
    <button
        :disabled="loading"
        class="group relative inline-flex w-0 flex-1 items-center justify-center gap-x-1.5 border border-transparent py-1 text-sm font-semibold "
        @click="handleClick"
    >
        <component :is="icon" v-if="!loading" class="h-5 w-5 text-gray-400 group-hover:text-gray-200"/>
        <InlineLoadingCircle v-else-if="loading" class="h-5 pr-1 text-gray-400 group-hover:text-gray-200"/>
        <span v-if="!loading">{{ props.displayText }}</span>
        <span v-else>{{ props.actionText }}</span>
    </button>
</template>

<script setup>
import InlineLoadingCircle from "~/components/loading/InlineLoadingCircle.vue";

const props = defineProps(["icon", "displayText", "actionText"])
const emit = defineEmits(["click"])

const icon = ref(props.icon)

const loading = ref(false)

function handleClick() {
    loading.value = true
    emit('click')
}
</script>

<style scoped>

</style>
