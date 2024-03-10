<template>
    <div :class="[active ? 'border-indigo-600 ring-2 ring-indigo-600' : 'border-gray-300']" class="relative flex rounded-lg border bg-white p-4 shadow-sm focus:outline-none">
        <div class="flex flex-1 h-full">
            <div class="flex flex-col h-full w-full">
                <span class="block text-sm font-medium text-gray-900">{{ title }}</span>
                <span class="mt-1 mb-0.5 flex items-center text-sm text-gray-500">{{ subtitle }}</span>

                <div class="">
                    <slot />
                </div>

                <!--                <div class="mt-6 text-sm font-medium text-gray-900">
                                    <slot/>
                                </div>-->

                <div class="mt-auto">
                    <button
                        v-if="!active && !isParent"
                        class="w-full rounded-md bg-white px-2.5 py-1.5 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                        type="button"
                        @click="callApply"
                    >
                        Auswählen
                    </button>
                </div>
            </div>
        </div>

        <CheckCircleIcon :class="[!active ? 'invisible' : '']" aria-hidden="true" class="absolute right-4 h-5 w-5 text-indigo-600" />
    </div>
</template>

<script lang="ts" setup>
import { CheckCircleIcon } from "@heroicons/vue/20/solid";

function callApply() {
    props.apply();
}

const props = defineProps({
    active: {
        type: Boolean,
        required: true,
    },
    title: {
        type: String,
        required: true,
    },
    subtitle: {
        type: String,
        required: true,
    },
    isParent: {
        type: Boolean,
        required: false,
        default: false,
    },
    apply: {
        type: Function,
        required: false,
        default: () => {},
    },
});
</script>

<style scoped></style>
