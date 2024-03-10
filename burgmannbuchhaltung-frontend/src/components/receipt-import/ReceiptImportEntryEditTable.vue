<template>
    <n-data-table
        remote
        :columns="columns"
        :data="modelValue"
        :loading="loading"
        :pagination="paginationReactive"
        :single-line="false"
    />
</template>

<script setup lang="ts">
import {NInput} from 'naive-ui'
import {defineComponent, h, nextTick, reactive, ref} from 'vue'

defineProps({
    modelValue: {
        type: Array,
        required: true
    },
    loading: {
        type: null,
        required: true
    }
})
const emit = defineEmits(['update:modelValue', 'editTable'])

const formatter = new Intl.NumberFormat('de-AT', {
    style: 'currency',
    currency: 'EUR',

    // These options are needed to round to whole numbers if that's what you want.
    //minimumFractionDigits: 0, // (this suffices for whole numbers, but will print 2500.10 as $2,500.1)
    //maximumFractionDigits: 0, // (causes 2500.99 to be printed as $2,501)
});

const ShowOrEdit = defineComponent({
    props: {
        value: [Number, String],
        isMoney: {
            type: Boolean,
            required: false,
            default: false
        },
        isNumber: {
            type: Boolean,
            required: false,
            default: false
        },
        onUpdateValue: {
            type: null,
            required: true,
        }
    },
    setup(props) {
        const isEdit = ref(false)
        const inputRef = ref(null)
        const inputValue = ref(props.value)

        function handleOnClick() {
            isEdit.value = true
            nextTick(() => {
                // noinspection TypeScriptUnresolvedReference
                inputRef.value.focus()
            })
        }

        function handleChange() {
            console.log("Update " + inputValue.value)

            if (props.isNumber || props.isMoney) {
                const p = parseNumberIfPossible(inputValue.value)
                if (!isNaN(p)) {
                    props.onUpdateValue(inputValue.value)
                    isEdit.value = false
                }
            } else {
                // noinspection TypeScriptUnresolvedReference
                props.onUpdateValue(inputValue.value.trim())
                isEdit.value = false
            }
        }

        return () =>
            h('div',
                {
                    onClick: handleOnClick
                },
                isEdit.value
                    ? h(NInput, {
                        ref: inputRef,
                        value: "" + inputValue.value,
                        onUpdateValue: (v) => {
                            inputValue.value = v
                        },
                        onChange: handleChange,
                        onBlur: handleChange
                    })
                    : (props.value ? (props.isMoney ? formatter.format(props.value) : props.value) : '  ')
            )
    }
})

const columns = [
    {
        title: 'Datum',
        key: 'date',
        width: 150,
        render(row, index) {
            // noinspection TypeScriptValidateTypes
            return h(ShowOrEdit, {
                value: row.date,
                onUpdateValue(v) {
                    emit("editTable", {index: index, name: "date", value: v.trim()})
                }
            })
        }
    },
    {
        title: 'Soll-Konto',
        key: 'debit',
        width: 100,
        render(row, index) {
            // noinspection TypeScriptValidateTypes
            return h(ShowOrEdit, {
                value: row.debit,
                isNumber: true,
                onUpdateValue(v) {
                    emit("editTable", {index: index, name: "debit", value: parseNumberIfPossible(v)})
                }
            })
        }
    },
    {
        title: 'Haben-Konto',
        key: 'credit',
        width: 100,
        render(row, index) {
            // noinspection TypeScriptValidateTypes
            return h(ShowOrEdit, {
                value: row.credit,
                isNumber: true,
                onUpdateValue(v) {
                    emit("editTable", {index: index, name: "credit", value: parseNumberIfPossible(v)})
                }
            })
        }
    },
    {
        title: 'Betrag',
        key: 'amount',
        width: 100,
        render(row, index) {
            // noinspection TypeScriptValidateTypes
            return h(ShowOrEdit, {
                value: row.amount,
                isMoney: true,
                onUpdateValue(v) {
                    emit("editTable", {index: index, name: "amount", value: parseNumberIfPossible(v)})
                }
            })
        }
    },
    {
        title: 'Referenz',
        key: 'reference',
        width: 100
    }
]

const paginationReactive = reactive({
    pageSize: 10,
})

/*
const pagination = ref({
    //pageSize: 20
    //"page-size": 10,
    //pageCount: 10,
    size: "medium",
    simple: false,
    defaultPage: 1,
    defaultPageSize: 10,
    itemCount: 100,
    pageSizes: [15, 25, 50, 100],
    showSizePicker: true
})*/

function parseNumberIfPossible(r) {
    if (Number.isNaN(Number.parseFloat(r))) {
        return r.trim();
    }
    return Number.parseFloat(r);
}
</script>

<style scoped>

</style>
