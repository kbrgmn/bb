<template>
    <button
        :disabled="loading"
        class=""
        @click="handleClick"
    >
        <!-- Icon left -->
        <!--suppress TypeScriptValidateTypes -->
        <component :is="icon" v-if="!loading && !iconRight" class="h-5 w-5 mr-1"/>
        <InlineLoadingCircle v-else-if="loading && !iconRight" class="h-5 w-5 mr-1"/>

        <span class="pl-0.5 pr-0.5">{{ !loading ? props.displayText : props.actionText ?? props.displayText }}</span>

        <!-- Icon right -->
        <!--suppress TypeScriptValidateTypes -->
        <component :is="icon" v-if="!loading && iconRight" class="h-5 w-5 ml-1"/>
        <InlineLoadingCircle v-else-if="loading && iconRight" class="h-5 w-5 ml-1"/>
    </button>
</template>

<script lang="ts" setup>
import InlineLoadingCircle from "~/components/loading/InlineLoadingCircle.vue";

const emit = defineEmits(["click"])
const props = defineProps({
    icon: {
        type: null,
        required: true
    },
    displayText: {
        type: String,
        required: true
    },
    actionText: {
        type: String,
        required: false,
        default: null
    },
    iconRight: {
        type: Boolean,
        default: false
    }
})

const icon = ref(props.icon)

const loading = ref(false)

function handleClick() {
    loading.value = true
    emit('click')
}
</script>

<style scoped>

</style>
