<template>
    <div class="mt-2">
        <OrganizationSelectionList></OrganizationSelectionList>
    </div>
</template>

<script setup lang="ts">
import OrganizationSelectionList from "~/components/organizations/OrganizationSelectionList.vue";

definePageMeta({
    title: "Organisation auswählen",
    layout: "menunonavs"
})
</script>

<style scoped>

</style>
