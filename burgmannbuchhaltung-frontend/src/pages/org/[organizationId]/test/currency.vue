<template>
    <div>
        <!--        <p>123456789: {{ formatMoney(parseMoney("123456789")) }}</p>-->
        <!--        <p>1234567,89: {{ formatMoney(parseMoney("1234567,89")) }}</p>-->
        <!--        <p>123.456,78: {{ formatMoney(parseMoney("123.456,78")) }}</p>-->
        <!--        <p>123.456,789: {{ formatMoney(parseMoney("123.456,789")) }}</p>-->
        <!--        <p>123,456,789: {{ formatMoney(parseMoney("123,456,789")) }}</p>-->
        <!--        <p>123.457,8: {{ formatMoney(parseMoney("123.457,8")) }}</p>-->
        <!--        <p>123.457,70: {{ formatMoney(parseMoney("123.457,70")) }}</p>-->
        <!--        <p>123.45.7,89: {{ formatMoney(parseMoney("123.45.7,89")) }}</p>-->
        <!--        <p>1.2,89: {{ formatMoney(parseMoney("1.2,89")) }}</p>-->
        <!--        <p>123.4578,90: {{ formatMoney(parseMoney("123.4578,90")) }}</p>-->
        <!--        <p>Value: {{ res }}</p>-->
        <!--        <input v-model="text" pattern="[\d\.,]+" type="text" @keydown="(evt) => {if (-->
        <!--                   !evt.ctrlKey-->
        <!--                   && !evt.altKey-->
        <!--                   && !evt.shiftKey-->
        <!--                   && evt.key.length == 1-->
        <!--                   && !'0123456789,.'.includes(evt.key)-->
        <!--                   )-->
        <!--               {-->
        <!--                   console.log('blocked: ' + evt.key); evt.preventDefault() }-->
        <!--               }">-->

        <AmountEntry v-model="text" :currency="currency"></AmountEntry>
        <AmountEntry v-model="text" :currency="currency" :display-short-currency-prefix="false" :display-currency-suffix="false"></AmountEntry>
        <input v-model="currency" />
    </div>
</template>

<script lang="ts" setup>
import AmountEntry from "~/components/accounting/AmountEntry.vue";

const text = ref("");
const currency = ref("");

const res = computed(() => {
    return text.value;
});
</script>

<style scoped></style>
