<template>
    Guten
    <span v-if="now.getHours() < 11">Morgen</span>
    <span v-else-if="now.getHours() > 18">Abend</span>
    <span v-else>Tag</span>
</template>

<script setup lang="ts">
import {useNow} from '@vueuse/core'

const now = useNow()
</script>

<style scoped>

</style>
