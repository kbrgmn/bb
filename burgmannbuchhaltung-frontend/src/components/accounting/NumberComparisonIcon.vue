<template>
    <div class="mt-2 py-1.5">
        <Icon v-if="diff === 1" class="h-7 w-7 text-yellow-500" name="mdi:code-greater-than"/>
        <Icon v-else-if="diff === 0" class="h-7 w-7 text-green-500" name="mdi:code-equal"/>
        <Icon v-else-if="diff === -1" class="h-7 w-7 text-yellow-500" name="mdi:code-less-than"/>
        <!--<Icon v-else name="mdi:code-not-equal" class="h-6 w-6 text-yellow-500"/>-->
    </div>
</template>

<script lang="ts" setup>
import BigNumber from "bignumber.js";

const props = defineProps({number1: BigNumber, number2: BigNumber})

const diff = computed(() => {
    if (props.number1 && props.number2)
        return props.number1.comparedTo(props.number2)

    return null
})
</script>
