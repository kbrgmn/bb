<template>
    <div>
        <NuxtLink
            to="/organizations/create"
            type="button"
            class="mt-2 w-fit flex items-center gap-1.5 rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
            @click="emitCloseAction"
        >
            <Icon name="heroicons:folder-plus" class="h-5 w-5" />
            Neue Organisation hinzufügen
        </NuxtLink>
        <ul class="divide-y divide-gray-100 max-h-96 px-2 overflow-y-scroll" role="list">
            <li v-for="organization in organizations" :key="organization.id" class="flex flex-col sm:flex-row justify-between gap-x-6 py-5">
                <div class="flex min-w-0 gap-x-4">
                    <img v-if="organization.image" :src="organization.image" alt="" class="h-12 w-12 flex-none rounded-md bg-gray-50" />
                    <div class="min-w-0 flex-auto">
                        <p class="text-sm font-semibold leading-6 text-gray-900 w-56 overflow-x-scroll">
                            {{ organization.name }}
                        </p>
                        <div class="mt-1 truncate text-xs leading-5 text-gray-500">
                            <p v-if="organization.role === 'admin'">Administrator</p>
                            <p v-else-if="organization.role === 'accountant'">Buchhalter</p>
                            <p v-else>Benutzerdefinierte Rolle</p>
                        </div>
                    </div>
                </div>
                <div class="flex flex-col mt-3 sm:mt-0 sm:items-end">
                    <button
                        class="sm:mr-1.5 rounded-md my-auto bg-white px-2.5 py-1.5 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:block"
                        @click="
                            emitCloseAction();
                            selectOrganization(organization.id);
                        "
                    >
                        Organisation auswählen<span class="sr-only">, {{ organization.name }}</span>
                    </button>
                </div>
            </li>
        </ul>
    </div>
</template>

<script setup lang="ts">
import { selectOrganization, getOrganizations } from "~/composables/organizations";

const emit = defineEmits(["closeAction"]);

function emitCloseAction() {
    emit("closeAction");
}

const organizations = await getOrganizations();
</script>

<style scoped></style>
