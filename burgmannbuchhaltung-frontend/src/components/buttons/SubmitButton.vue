<template>
    <button
        :disabled="props.loading"
        :value="props.loading"
        class="mt-2 items-center font-semibold inline-flex
               justify-center rounded-xl border px-4 py-2 text-sm shadow-sm
               focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-100"
        @click="handleClick"
    >
        <component :is="icon" v-if="!props.loading && icon" class="h-5 pr-2"/>
        <InlineLoadingCircle v-else-if="props.loading" class="h-5 pr-2"/>
        <slot/>
    </button>
</template>

<script setup>
import InlineLoadingCircle from "~/components/loading/InlineLoadingCircle.vue";

const props = defineProps([
    "loading",
    "icon"
])
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
