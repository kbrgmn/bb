<template>
    <n-data-table :key="(row) => row.key" :columns="columns" :data="modelValue" :pagination="pagination" remote />
</template>

<script setup>
import { NInput, NText } from "naive-ui";
import { defineComponent, h, nextTick, ref } from "vue";

const props = defineProps(["modelValue"]);
const emit = defineEmits(["update:modelValue", "editTable", "recalculateActualValues"]);

const formatter = new Intl.NumberFormat("de-AT", {
    style: "currency",
    currency: "EUR",

    // These options are needed to round to whole numbers if that's what you want.
    //minimumFractionDigits: 0, // (this suffices for whole numbers, but will print 2500.10 as $2,500.1)
    //maximumFractionDigits: 0, // (causes 2500.99 to be printed as $2,501)
});

const ShowOrEdit = defineComponent({
    props: {
        value: {
            type: [Number, String],
            required: true,
        },
        isMoney: {
            type: Boolean,
            required: false,
            default: false,
        },
        onUpdateValue: {
            type: [Function, Array],
            required: true,
        },
    },
    setup(props) {
        const isEdit = ref(false);
        const inputRef = ref(null);
        const inputValue = ref(props.value);

        function handleOnClick() {
            isEdit.value = true;
            nextTick(() => {
                inputRef.value.focus();
            });
        }

        function handleChange() {
            console.log("Update " + inputValue.value);

            if (props.isMoney) {
                const p = parseNumberIfPossible(inputValue.value);
                if (!isNaN(p)) {
                    props.onUpdateValue(inputValue.value);
                    isEdit.value = false;
                } else {
                    if (inputValue.value === null) {
                        isEdit.value = false;
                        props.onUpdateValue(null);
                    } else {
                        console.log("not a number:", inputValue.value);
                    }
                }
            } else {
                props.onUpdateValue(inputValue.value.trim());
                isEdit.value = false;
            }
        }

        return () =>
            h(
                "div",
                {
                    onClick: handleOnClick,
                },
                isEdit.value
                    ? h(NInput, {
                          ref: inputRef,
                          value: inputValue.value,
                          onUpdateValue: (v) => {
                              inputValue.value = v;
                          },
                          onChange: handleChange,
                          onBlur: handleChange,
                      })
                    : props.value
                    ? props.isMoney
                        ? formatter.format(props.value)
                        : props.value
                    : "  ",
            );
    },
});

const columns = [
    {
        title: "Name",
        key: "name",
        width: 150,
        render(row, index) {
            return h(ShowOrEdit, {
                value: row.name,
                onUpdateValue(v) {
                    emit("editTable", { index: index, name: "name", value: v.trim() });
                },
            });
        },
    },
    {
        title: "Eingang",
        key: "Eingang",
        width: 100,
        render(row, index) {
            return h(ShowOrEdit, {
                value: row.Eingang,
                isMoney: true,
                onUpdateValue(v) {
                    emit("editTable", { index: index, name: "Eingang", value: parseNumberIfPossible(v) });
                },
            });
        },
    },
    {
        title: "Ausgang",
        key: "Ausgang",
        width: 100,
        render(row, index) {
            return h(ShowOrEdit, {
                value: row.Ausgang,
                isMoney: true,
                onUpdateValue(v) {
                    emit("editTable", { index: index, name: "Ausgang", value: parseNumberIfPossible(v) });
                },
            });
        },
    },
    {
        title: "Kassastand",
        key: "Kassastand",
        width: 100,
        render(row, index) {
            return h(ShowOrEdit, {
                value: row.Kassastand,
                isMoney: true,
                onUpdateValue(v) {
                    emit("editTable", { index: index, name: "Kassastand", value: parseNumberIfPossible(v) });
                },
            });
        },
    },
];

const pagination = {
    pageSize: 15,
};

function parseNumberIfPossible(r) {
    console.log(`Trying to parse number: "${r}", raw:`, r);
    if (r === null || r === undefined) return null;

    if (Number.isNaN(Number.parseFloat(r))) {
        return r.trim();
    }

    return Number.parseFloat(r);
}
</script>
