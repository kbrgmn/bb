<template>
    <select required
        v-model="model"
        class="mt-2 mb-2 block rounded-md border-0 py-1.5 pl-2 pr-8 text-gray-900 ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-indigo-600 sm:text-sm sm:leading-6"
    >
        <option v-for="day in Object.keys(dayMapping)" :key="day" :value="day">{{ dayMapping[day] }}</option>
    </select>
</template>

<script lang="ts" setup>
const model = defineModel();

const dayMapping = {
    1: "Montag",
    2: "Dienstag",
    3: "Mittwoch",
    4: "Donnerstag",
    5: "Freitag",
    6: "Samstag",
    7: "Sonntag",
};
</script>
