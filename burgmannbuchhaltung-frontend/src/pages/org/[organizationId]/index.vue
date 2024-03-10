<template></template>

<script setup lang="ts">
import { useOrganization } from "~/composables/organizations";

const orgId = useOrganization();

navigateTo(`/org/${orgId.value}/dashboard`);
</script>
