<template>
    <ShortcutGroup v-if="control && alt">
        <slot/>
    </ShortcutGroup>
</template>

<script lang="ts" setup>
import {useMagicKeys} from "@vueuse/core";
import ShortcutGroup from "~/components/ShortcutGroup.vue";

const {control, alt} = useMagicKeys()
</script>

<style scoped>
</style>
