<template>
    <button
        class="mt-2 items-center font-semibold inline-flex
               justify-center rounded-xl border px-4 py-2 text-sm shadow-sm
               focus:outline-none focus:ring-2 focus:ring-offset-2"
    >
        <component :is="icon" v-if="!props.loading" class="h-5 pr-2"/>
        <InlineLoadingCircle v-else-if="props.loading" class="h-5 pr-2"/>
        <slot/>
    </button>
</template>

<script lang="ts" setup>
const props = defineProps([
    "loading",
    "icon"
])
</script>

<style scoped>

</style>
