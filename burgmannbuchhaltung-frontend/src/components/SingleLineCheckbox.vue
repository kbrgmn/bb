<template>
    <div>
        <div class="h-8 items-center  pl-4 pr-3">
            <label class="grid grid-rows-1 grid-cols-8 gap-2 ">
                <input :id="`check-${inputId}`" ref="checkbox" :checked="inputChecked" class="h-6 w-6 col-span-1 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600" type="checkbox"/>
                <span class="ml-1 col-span-7 leading-tight">
                    <slot/>
                </span>
            </label>
        </div>
    </div>
</template>

<script setup>
defineProps(["inputChecked", "inputId"])

const checkbox = ref(null)

function toggleChecked() {
    checkbox.value = !checkbox.value
}
</script>

<style scoped>

</style>
