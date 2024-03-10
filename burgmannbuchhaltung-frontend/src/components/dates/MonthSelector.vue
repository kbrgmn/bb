<template>
    <select required
        v-model="model"
        class="mt-2 mb-2 block rounded-md border-0 py-1.5 pl-3 pr-10 text-gray-900 ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-indigo-600 sm:text-sm sm:leading-6"
    >
        <option v-for="month in Object.keys(monthMapping)" :key="month" :value="month">{{ monthMapping[month] }}</option>
    </select>
</template>

<script lang="ts" setup>
const model = defineModel();

const monthMapping = {
    1: "Jänner",
    2: "Februar",
    3: "März",
    4: "April",
    5: "Mai",
    6: "Juni",
    7: "Juli",
    8: "August",
    9: "September",
    10: "Oktober",
    11: "November",
    12: "Dezember",
};
</script>
