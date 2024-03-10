<template>
    <div v-for="item in navigationLinks.value" :key="item.name" :class="[notLoggedIn ? 'hidden' : '']">
        <NuxtLink
            v-if="item.children === undefined || item.children === null"
            :to="item.href"
            class="text-gray-600 hover:bg-gray-50 hover:text-gray-900 group flex items-center rounded-md px-2 py-2 text-sm font-medium"
        >
            <Icon v-if="item.icon" :class="[item.current ? 'text-gray-500' : 'text-gray-400 group-hover:text-gray-500', 'mr-3 h-6 w-6 flex-shrink-0']" :name="item.icon" aria-hidden="true" />
            {{ item.name }}
        </NuxtLink>

        <Disclosure v-else v-slot="{ open }" as="div" class="mt-2 flex flex-col">
            <DisclosureButton class="text-gray-600 bg-white hover:bg-gray-50 hover:text-gray-900 group flex items-center rounded-md px-2 py-2 text-sm font-medium">
                <Icon v-if="item.icon" :class="[item.current ? 'text-gray-500' : 'text-gray-400 group-hover:text-gray-500', 'mr-3 h-6 w-6 flex-shrink-0']" :name="item.icon" aria-hidden="true" />
                {{ item.name }}
            </DisclosureButton>
            <DisclosurePanel class="border-l-2 px-4 text-sm text-gray-500">
                <NavigationLinks :navigation-links="ref(item.children)" :not-logged-in="notLoggedIn" />
                <!-- ref(children) hack: need reactive parameter -->
            </DisclosurePanel>
        </Disclosure>
    </div>
</template>

<script lang="ts" setup>
import { useNavigation } from "~/composables/navigation";
import { Disclosure, DisclosureButton, DisclosurePanel } from "@headlessui/vue";

const props = defineProps({
    notLoggedIn: {
        type: Boolean,
        required: true,
    },
    navigationLinks: {
        required: false,
        default: useNavigation(),
    },
});
</script>

<style scoped>
.router-link-active {
    @apply bg-gray-100 text-gray-900;
}
</style>
