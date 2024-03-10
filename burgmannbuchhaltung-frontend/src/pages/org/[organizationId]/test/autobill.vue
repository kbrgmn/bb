<template>
    <div class="bg-white pt-2 p-1 sm:p-3">
        <p class="text-lg">Abrechnungszeitraum bearbeiten:</p>

        <div class="border shadow p-2 rounded-xl mt-1">
            <p class="font-semibold">Beispiel übernehmen?</p>

            <ul class="grid grid-cols-1 gap-5 gap-y-3 sm:grid-cols-2 lg:grid-cols-3 mt-1" role="list">
                <li v-for="example in examples" :key="example.name" class="col-span-1 divide-y divide-gray-200 rounded-lg border bg-white shadow">
                    <div class="flex w-full items-center justify-between space-x-6 p-3">
                        <div class="flex-1 truncate">
                            <div class="flex items-center space-x-3">
                                <h3 class="truncate text-sm font-medium text-gray-900">{{ example.name }}</h3>
                            </div>
                            <p class="mt-1 truncate text-sm text-gray-500">{{ example.description }}</p>
                        </div>
                    </div>
                    <div>
                        <div class="-mt-px flex divide-x divide-gray-200">
                            <div class="flex w-0 flex-1">
                                <button
                                    class="relative -mr-px inline-flex w-0 flex-1 items-center justify-center gap-x-3 rounded-bl-lg border border-transparent py-2 text-sm font-semibold text-gray-900"
                                    @click="
                                        x = example.data;
                                        updateLocalVariables();
                                    "
                                >
                                    Übernehmen
                                    <Icon aria-hidden="true" class="h-5 w-5 text-gray-500" name="iconoir:submit-document" />
                                </button>
                            </div>
                        </div>
                    </div>
                </li>
            </ul>
        </div>

        <div class="border m-2 p-1 px-2 rounded-xl mt-4">
            <div class="mt-2 border rounded-xl shadow p-2">
                <p class="text-lg font-semibold">Uhrzeit</p>
                <div class="mt-2 flex flex-col sm:flex-row gap-4">
                    <div class="flex items-center gap-0.5">
                        <div class="relative">
                            <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Stunde </label>
                            <input
                                v-model.number="x.hour"
                                class="w-20 block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                placeholder="HH"
                                type="number"
                                min="0"
                                max="23"
                            />
                        </div>
                        :
                        <div class="relative">
                            <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Minute </label>
                            <input
                                v-model.number="x.minute"
                                class="w-20 block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                placeholder="MM"
                                type="number"
                                min="0"
                                max="59"
                            />
                        </div>
                        :
                        <div class="relative">
                            <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Sekunde </label>
                            <input
                                v-model.number="x.second"
                                class="w-20 block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                placeholder="SS"
                                type="number"
                                min="0"
                                max="59"
                            />
                        </div>
                    </div>

                    <span class="isolate inline-flex rounded-md shadow-sm mt-2 sm:mt-0">
                        <button
                            class="relative inline-flex items-center rounded-l-md bg-white px-2 sm:px-3 py-2 text-sm font-semibold text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-10"
                            type="button"
                            @click="
                                x.hour = 0;
                                x.minute = 0;
                                x.second = 0;
                            "
                        >
                            Tagesanfang
                        </button>
                        <button
                            class="relative -ml-px inline-flex items-center bg-white px-2 sm:px-3 py-2 text-sm font-semibold text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-10"
                            type="button"
                            @click="
                                x.hour = 12;
                                x.minute = 0;
                                x.second = 0;
                            "
                        >
                            Tagesmitte
                        </button>
                        <button
                            class="relative -ml-px inline-flex items-center rounded-r-md bg-white px-2 sm:px-3 py-2 text-sm font-semibold text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-10"
                            type="button"
                            @click="
                                x.hour = 23;
                                x.minute = 59;
                                x.second = 59;
                            "
                        >
                            Tagesende
                        </button>
                    </span>
                </div>
            </div>

            <div class="mt-3 flex flex-col 2xl:flex-row gap-3">
                <SelectionGroup :active="!x.dateEvery" subtitle="Zeitpunkte, welche jedes Jahr gleich sind, definieren:" title="Statische Zeitpunkte festlegen">
                    <!-- Day -->
                    <div class="mt-3 border rounded-xl shadow p-2">
                        <p class="text-lg font-semibold">Tag</p>
                        <div class="grid grid-cols-1 gap-y-6 sm:grid-cols-2 lg:grid-cols-3 sm:gap-x-2 lg:gap-x-4 mt-1">
                            <SelectionGroup :active="x.day" subtitle="Ein bestimmter Tag im Monat:" title="Tag im Monat/Zeitraum">
                                <fieldset class="mt-2">
                                    <div class="divide-y divide-gray-300">
                                        <div class="relative flex items-start pb-4 pt-3.5">
                                            <div class="min-w-0 flex-1 text-sm">
                                                <label for="lastDayOfMonthCheckbox">Täglich</label>
                                                <p class="text-xs text-gray-400 w-50">Jeden Tag im Monat</p>
                                            </div>
                                            <div class="ml-3 flex h-6 items-center">
                                                <input
                                                    id="lastDayOfMonthCheckbox"
                                                    :checked="x.day == null"
                                                    class="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                                    name="account"
                                                    type="radio"
                                                    @click="x.day = null"
                                                />
                                            </div>
                                        </div>

                                        <div class="relative flex items-start pb-4 pt-3.5">
                                            <div class="min-w-0 flex-1 text-sm">
                                                <div class="relative mt-2">
                                                    <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Tag </label>
                                                    <input
                                                        v-model="specificDay"
                                                        class="w-24 block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                                        placeholder="DD"
                                                        type="number"
                                                        @input="x.day = specificDay"
                                                    />
                                                </div>

                                                <p class="text-xs text-gray-400 w-50">dh.: jeden {{ specificDay }}. im Monat</p>
                                            </div>
                                            <div class="ml-3 flex h-6 items-center">
                                                <input
                                                    :checked="!x.day || x.day > 0"
                                                    class="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                                    name="account"
                                                    type="radio"
                                                    @click="x.day = specificDay"
                                                />
                                            </div>
                                        </div>

                                        <div class="relative flex items-start pb-4 pt-3.5">
                                            <div class="min-w-0 flex-1 text-sm">
                                                <label for="lastDayOfMonthCheckbox">Letzter Tag des Monats</label>
                                                <p class="text-xs text-gray-400 w-50">z.B. für aktuelles Monat: {{ lastDayCurrentMonth.toLocaleDateString("de-DE") }}</p>
                                            </div>
                                            <div class="ml-3 flex h-6 items-center">
                                                <input
                                                    id="lastDayOfMonthCheckbox"
                                                    :checked="x.day == -1"
                                                    class="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                                    name="account"
                                                    type="radio"
                                                    @click="x.day = -1"
                                                />
                                            </div>
                                        </div>

                                        <div class="relative flex items-start pb-4 pt-3.5">
                                            <div class="min-w-0 flex-1 text-sm">
                                                <label for="lastWorkDayOfMonthCheckbox">Letzter Werktag des Monats </label>
                                                <p class="text-xs text-gray-400 w-50">Samstag/Sonntag ausnehmen</p>
                                            </div>
                                            <div class="ml-3 flex h-6 items-center">
                                                <input
                                                    id="lastWorkDayOfMonthCheckbox"
                                                    :checked="x.day == -2"
                                                    class="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                                    name="account"
                                                    type="radio"
                                                    @click="x.day = -2"
                                                />
                                            </div>
                                        </div>
                                    </div>
                                </fieldset>
                            </SelectionGroup>

                            <SelectionGroup :active="x.days" subtitle="Mehrere bestimmte Tage im Monat:" title="Tage eines Monats">
                                <div class="mt-6 text-sm font-medium text-gray-900">
                                    <form>
                                        <div class="relative flex items-center gap-1">
                                            <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Tag </label>
                                            <input
                                                v-model.number="daysCurrentDayToAdd"
                                                class="w-24 block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                                max="31"
                                                min="1"
                                                placeholder="DD"
                                                type="number"
                                            />

                                            <button
                                                :disabled="
                                                    daysCurrentDayToAdd == null ||
                                                    daysCurrentDayToAdd == '' ||
                                                    daysToAdd.includes(daysCurrentDayToAdd) ||
                                                    daysCurrentDayToAdd < 1 ||
                                                    daysCurrentDayToAdd > 31
                                                "
                                                class="ml-1 flex items-center gap-0.5 rounded bg-indigo-600 disabled:bg-gray-500 px-1.5 py-0.5 text-xs font-semibold text-white shadow-sm disabled:cursor-not-allowed hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                                                type="submit"
                                                @click="
                                                    addDayToDays();
                                                    daysCurrentDayToAdd = null;
                                                "
                                            >
                                                Hinzufügen
                                                <Icon class="h-5 w-5" name="heroicons:plus-circle" />
                                            </button>
                                        </div>
                                    </form>

                                    <ul v-auto-animate class="overflow-y-scroll mt-2 list-disc list-inside">
                                        <li v-for="entry in daysToAdd" class="h-7">
                                            <div class="inline-flex items-center">
                                                <p class="w-5">{{ entry }}.</p>

                                                <button
                                                    class="flex items-center gap-0.5 ml-4 rounded bg-white px-2 py-0.5 text-xs font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                                                    type="button"
                                                    @click="removeDays(entry)"
                                                >
                                                    Löschen
                                                    <Icon class="h-4 w-4" name="heroicons:trash" />
                                                </button>
                                            </div>
                                        </li>
                                        <li v-if="daysToAdd.length == 0">Noch keine Tage festgelegt.</li>
                                    </ul>
                                </div>
                            </SelectionGroup>

                            <SelectionGroup :active="false" subtitle="Wochentage eines Monats" title="Wochentag">
                                <fieldset class="mt-2">
                                    <div class="divide-y divide-gray-300">
                                        <div class="relative flex items-start pb-2 pt-2.5">
                                            <div class="min-w-0 flex-1 text-sm">
                                                <label for="lastDayOfWeekCheckbox">Ende der Woche</label>
                                                <p class="text-xs text-gray-400 w-50">Wöchentlich d. Ende der Woche (Sonntag)</p>
                                            </div>
                                            <div class="ml-3 flex h-6 items-center">
                                                <input
                                                    id="lastDayOfWeekCheckbox"
                                                    :checked="x.lastDayOfWeek === 0"
                                                    class="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                                    name="account"
                                                    type="radio"
                                                    @click="x.lastDayOfWeek = 0"
                                                />
                                            </div>
                                        </div>

                                        <div class="relative flex items-start pb-1 pt-1">
                                            <div class="min-w-0 text-sm">
                                                <div class="flex items-center gap-2">
                                                    Letzter
                                                    <WeekdaySelector />
                                                    des Monats
                                                </div>
                                            </div>
                                            <div class="ml-3 flex h-6 items-center">
                                                <input
                                                    :checked="lastDayOfWeek || x.lastDayOfWeek > 0"
                                                    class="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                                    name="account"
                                                    type="radio"
                                                    @click="x.day = specificDay"
                                                />
                                            </div>
                                        </div>

                                        <div class="relative flex items-start pb-0.5 pt-3">
                                            <div class="min-w-0 flex-1 text-sm">
                                                <div class="flex items-center gap-1">
                                                    Jeder
                                                    <div class="relative">
                                                        <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Tag </label>
                                                        <input
                                                            v-model.number="dateEveryDays"
                                                            class="w-18 block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                                            max="31"
                                                            min="1"
                                                            placeholder="DD"
                                                            type="number"
                                                        />
                                                    </div>
                                                    .
                                                </div>
                                                <div class="flex items-center gap-1">
                                                    <WeekdaySelector />
                                                    des Monats
                                                </div>
                                            </div>
                                            <div class="ml-3 flex h-6 items-center">
                                                <input id="" :checked="false" class="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-600" name="account" type="radio" @click="" />
                                            </div>
                                        </div>

                                        <div class="relative flex items-start pb-2 pt-2.5">
                                            <div class="min-w-0 flex-1 text-sm">
                                                <div class="flex gap-1 items-center">
                                                    Alle
                                                    <div class="relative">
                                                        <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Anzahl</label>
                                                        <input
                                                            v-model.number="dateEveryDays"
                                                            class="w-18 block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                                            max="31"
                                                            min="1"
                                                            placeholder="DD"
                                                            type="number"
                                                        />
                                                    </div>
                                                    Tage, startend
                                                </div>

                                                <div class="flex gap-1 items-center mt-2">
                                                    ab dem
                                                    <div class="relative">
                                                        <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Tag </label>
                                                        <input
                                                            v-model.number="dateEveryDays"
                                                            class="w-18 block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                                            max="31"
                                                            min="1"
                                                            placeholder="DD"
                                                            type="number"
                                                        />
                                                    </div><span class="-ml-0.5">.</span>
                                                    des Monats
                                                </div>
                                            </div>

                                            <div class="ml-3 flex h-6 items-center">
                                                <input id="" :checked="false" class="h-4 w-4 border-gray-300 text-indigo-600 focus:ring-indigo-600" name="account" type="radio" @click="" />
                                            </div>
                                        </div>
                                    </div>
                                </fieldset>
                            </SelectionGroup>
                        </div>
                    </div>

                    <!-- Month -->
                    <div class="mt-3 border rounded-xl shadow p-2">
                        <p class="text-lg font-semibold">Monat</p>
                        <div class="grid grid-cols-1 gap-y-6 sm:grid-cols-3 sm:gap-x-4 mt-1">
                            <SelectionGroup
                                :active="x.month == null && x.months == null"
                                subtitle="Jedes Monat"
                                title="Monatlich"
                                @click="
                                    x.month = null;
                                    x.months = null;
                                "
                            ></SelectionGroup>

                            <SelectionGroup :active="x.month != null && x.months == null" subtitle="Ein bestimmtes Monat im Jahr" title="Bestimmtes Monat">
                                <MonthSelector v-model="x.month"></MonthSelector>
                            </SelectionGroup>

                            <SelectionGroup :active="x.months != null" subtitle="Mehrere bestimmte Monate im Jahr" title="Ausgewählte Monate">
                                <div class="mt-2 text-sm font-medium text-gray-900">
                                    <form>
                                        <div class="relative flex flex-col sm:flex-row items-center gap-1">
                                            <MonthSelector v-model="monthsCurrentMonthToAdd" class="w-full sm:w-auto" />

                                            <button
                                                :disabled="
                                                    monthsCurrentMonthToAdd == null ||
                                                    monthsCurrentMonthToAdd == '' ||
                                                    monthsToAdd.includes(monthsCurrentMonthToAdd) ||
                                                    monthsCurrentMonthToAdd < 1 ||
                                                    monthsCurrentMonthToAdd > 12
                                                "
                                                class="ml-0 w-full sm:w-auto sm:ml-1 sm:-mr-5 flex items-center gap-0.5 rounded bg-indigo-600 disabled:bg-gray-500 px-1 py-0.5 text-xs font-semibold text-white shadow-sm disabled:cursor-not-allowed hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                                                type="submit"
                                                @click="
                                                    addMonthToMonths();
                                                    monthsCurrentMonthToAdd = null;
                                                "
                                            >
                                                Hinzufügen
                                                <Icon class="h-5 w-5" name="heroicons:plus-circle" />
                                            </button>
                                        </div>
                                    </form>

                                    <ul v-auto-animate class="overflow-y-scroll mt-2 h-20 list-disc list-inside">
                                        <li v-for="entry in monthsToAdd" class="h-7">
                                            <div class="inline-flex items-center">
                                                <p class="w-20">{{ monthName(entry) }}</p>

                                                <button
                                                    class="flex items-center gap-0.5 rounded bg-white px-2 py-0.5 text-xs font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
                                                    type="button"
                                                    @click="removeMonth(entry)"
                                                >
                                                    Löschen
                                                    <Icon class="h-4 w-4" name="heroicons:trash" />
                                                </button>
                                            </div>
                                        </li>
                                        <li v-if="monthsToAdd.length == 0">Noch keine Monate festgelegt.</li>
                                    </ul>
                                </div>
                            </SelectionGroup>
                        </div>
                    </div>
                </SelectionGroup>

                <SelectionGroup
                    :active="x.dateEvery"
                    :is-parent="true"
                    class="mt-3 sm:mt-0 overflow-auto sm:overflow-hidden"
                    subtitle="Zeitpunkte, welche sich laufend verändern, definieren:"
                    title="Dynamische Zeitpunkte festlegen"
                >
                    <SelectionGroup :active="x.dayAt" class="mt-3" subtitle="Feste Tagesanzahl statt bestimmter Tage im Jahr:" title="Tage wiederholend">
                        <div class="pb-2">
                            <div class="flex mt-4 gap-1 items-center">
                                Alle
                                <div class="relative">
                                    <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Anzahl</label>
                                    <input
                                        v-model.number="dateEveryDays"
                                        class="w-24 block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                        max="31"
                                        min="1"
                                        placeholder="DD"
                                        type="number"
                                    />
                                </div>
                                Tage,
                            </div>

                            <div class="flex mt-4 gap-1 pt-1 items-center overflow-x-hidden">
                                beginnend mit
                                <div class="relative">
                                    <label class="absolute -top-2 left-2 inline-block bg-white px-1 text-xs font-medium text-gray-900" for="name">Datum </label>
                                    <input
                                        v-model="dateEveryDateString"
                                        class="block border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                        placeholder="dd.mm.yyyy"
                                        type="date"
                                    />
                                </div>
                                .
                            </div>
                        </div>

                        <!--                        <span class="mt-6 text-xs font-medium text-gray-400"
                                                    >z.B. erste Ausführung: {{ addDays(dateEveryDateString, dateEveryDays)?.toLocaleDateString("de-DE") ?? "Kein Datum!" }}</span
                                                >-->
                    </SelectionGroup>
                </SelectionGroup>
            </div>
        </div>

        <!--        <p v-if="x.daysOfWeek">daysOfWeek : {{ x.daysOfWeek }}</p>
                <p v-if="x.lastDayOfWeek">lastDayOfWeek : {{ x.lastDayOfWeek }}</p>
                <p v-if="x.year">year : {{ x.year }}</p>

                <p v-if="x.dateEvery">dateEvery: {{ x.dateEvery }}</p>-->

        <code>{{ x }}</code>
    </div>
</template>

<script lang="ts" setup>
import SelectionGroup from "~/components/autobill/SelectionGroup.vue";
import MonthSelector from "~/components/dates/MonthSelector.vue";
import WeekdaySelector from "~/components/dates/WeekdaySelector.vue";

const showAdvancedSelected = ref(false);
const showAdvanced = computed(() => {
    return showAdvancedSelected || false; // TODO: check if advanced is configured
});

function updateLocalVariables() {
    specificDay.value = x.value.day >= 1 ? x.value.day : 1;

    daysToAdd.length = 0;
    x.value.days?.forEach((entry) => daysToAdd.push(entry));

    monthsToAdd.length = 0;
    x.value.months?.forEach((entry) => monthsToAdd.push(entry));
}

const lastDay: CronSetupData = { hour: 23, minute: 59, second: 59, day: -1 };
const firstDay: CronSetupData = { hour: 0, minute: 0, second: 0, day: 1 };
const xx: CronSetupData = { hour: 23, minute: 59, second: 59, day: -1 };

type Example = {
    name: string;
    description: string;
    data: CronSetupData;
};

const examples: Example[] = [
    {
        name: "Monatsende",
        description: "Abrechnung jeden letzten Tag des Monats",
        data: { hour: 23, minute: 59, second: 59, day: -1 },
    },
    {
        name: "Monatsanfang",
        description: "Abrechnung jeden 1. Tag des Monats",
        data: { hour: 0, minute: 0, second: 0, day: 1 },
    },
    {
        name: "Quartalsende",
        description: "4x jährliche Abrechnung, per Ende des Quartals",
        data: { hour: 23, minute: 59, second: 59, day: -1, months: [3, 6, 9, 12] },
    },
    {
        name: "Quartalsanfang",
        description: "4x jährliche Abrechnung, per Anfang des Quartals",
        data: { hour: 0, minute: 0, second: 0, day: 1, months: [1, 4, 7, 10] },
    },
    {
        name: "Ende der Woche",
        description: "Wöchentlich mit Ende des Sonntags",
        data: { hour: 23, minute: 59, second: 59, lastDayOfWeek: 0 },
    },
];

const x: Ref<CronSetupData> = ref(firstDay);

// const monthSelector = ref(x.value.months ? "months" : x.value.month ? "month" : x.value.months == null ? "monthly" : null);

// -- start day options
const specificDay: Ref<number> = ref(x.value.day && x.value.day > 0 ? x.value.day : 1);
const daysCurrentDayToAdd: Ref<number | null> = ref(null);

function addDayToDays() {
    if (!daysToAdd.includes(daysCurrentDayToAdd.value!!)) {
        daysToAdd.push(daysCurrentDayToAdd.value!!);
    }
    daysToAdd.sort((a, b) => a - b); // do not sort lexicographically
}

function removeDays(entry: number) {
    const idx = daysToAdd.findIndex((v) => v == entry);
    if (idx > -1) {
        daysToAdd.splice(idx, 1);
    }
}

const daysToAdd = reactive(x.value.days ?? []);

// Months
const monthsCurrentMonthToAdd: Ref<number | null> = ref(null);

function addMonthToMonths() {
    if (!monthsToAdd.includes(monthsCurrentMonthToAdd.value!!)) {
        monthsToAdd.push(monthsCurrentMonthToAdd.value!!);
    }
    monthsToAdd.sort((a, b) => a - b); // do not sort lexicographically
}

function removeMonth(entry: number) {
    const idx = monthsToAdd.findIndex((v) => v == entry);
    if (idx > -1) {
        monthsToAdd.splice(idx, 1);
    }
}

const monthsToAdd = reactive(x.value.months ?? []);

// -- end day options

function monthName(i: number): string {
    return monthMapping[i];
}

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

function nowAsYYYYMMDD() {
    let now = new Date();
    const offset = now.getTimezoneOffset();
    now = new Date(now.getTime() - offset * 60 * 1000);
    return now.toISOString().split("T")[0];
}

function getLastDayOfMonth(year: number, month: number) {
    return new Date(year, month + 1, 0);
}

function addDays(dateString: string, days: number): Date | null {
    const resultDate = new Date(dateString);
    resultDate.setDate(resultDate.getDate() + days);

    if (isNaN(resultDate.getTime())) {
        return null;
    }
    return resultDate;
}

const date = new Date();
const lastDayCurrentMonth = getLastDayOfMonth(date.getFullYear(), date.getMonth());

type CronSetupData = {
    hour: number;
    minute: number;
    second: number;
    /**
     * last day: -1
     * last weekday: -2
     *
     * exclusive with dayAt, days
     */
    day?: number | null;
    /**
     * every _first_ days; starting at _second_
     *
     * exclusive with day, days
     */
    dayAt?: [number, number] | null;
    /**
     * on specific days of the month
     *
     * exclusive with dayAt, day
     */
    days?: number[] | null;
    /**
     * every *second*th *first*day of the month
     * e.g. every 5th Sunday of the month
     */
    daysOfWeek?: { first: number; second: number } | null;
    /**
     * 0: every end of week
     * 1: last monday
     * 7: last sunday
     */
    lastDayOfWeek?: number | null;

    /**
     * Month, exclusive with months
     */
    month?: number | null;

    /**
     * Months, exclusive with month
     */
    months?: number[] | null;

    /**
     * _first_ start
     * _second_ end
     */
    year?: { first: number; second: number | null } | null;

    dateEvery?: {
        date: string;
        days: number;
    } | null;
};

/*
 TODO: Unset lastDayOfWeek and etc when day is set and vice-versa
 */
</script>
